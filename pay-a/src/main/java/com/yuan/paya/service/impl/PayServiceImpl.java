package com.yuan.paya.service.impl;

import com.yuan.paya.entity.CustomerAccount;
import com.yuan.paya.mapper.CustomerAccountMapper;
import com.yuan.paya.response.CommonReturnType;
import com.yuan.paya.service.PayService;
import com.yuan.paya.service.producer.CallbackService;
import com.yuan.paya.service.producer.TransactionProducer;
import com.yuan.paya.utils.FastJsonConvertUtil;
import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.client.producer.TransactionSendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

@Service
public class PayServiceImpl implements PayService {

    public static final String TX_PAYA_TOPIC = "tx_paya_topic";
    public static final String TX_PAYA_TAGS = "paya";

    @Autowired
    private CustomerAccountMapper customerAccountMapper;

    @Autowired
    private TransactionProducer transactionProducer;

    @Autowired
    private CallbackService callbackService;

    /**
     * 支付操作
     *
     * @param userId    用户id
     * @param orderId   订单id
     * @param accountId 账户id
     * @param money     支付金额
     */
    @Override
    public CommonReturnType payment(String userId, String orderId, String accountId, double money) {
        try {
            //第一步：正常工作逻辑中，可以在提交订单时加上token参数
            //例如在每次点击下单按钮时，生成一个不同且唯一的token作为参数一起传入该接口，这样可以防止表单重复提交
            BigDecimal payMoney = new BigDecimal(money);

            //采用分布式锁时：加锁开始（获取锁）

            CustomerAccount old = customerAccountMapper.selectByPrimaryKey(accountId);
            BigDecimal oldMoney = old.getCurrentBalance();//当前账户余额
            int oldVersion = old.getVersion();//当前数据的版本号
            //第二步：正常工作逻辑中，在做下面支付的数据库操作前，可以用redis去重分布式锁，
            //看一下当前线程是否能够获得分布式锁，这样做可以防止在同一时间，不同的用户使用同一账号下单。
            //注意：这里即使分布式锁获取失败，也不可以返回支付失败。
            //继续往下走，进入第三步：用数据库的乐观锁方式来做最终的数据去重

            /**
             * 细节一 --- 支付：余额 - 支付金额
             * 注意：在此处扣钱而不是直接在数据库里操作的目的是为了节约损耗，
             * 因为可以通过下面的if判断余额是否会不足，直接决定后续是否需要做本地数据库操作以及发消息操作
             */
            BigDecimal newMoney = oldMoney.subtract(payMoney);

            //采用分布式锁时：加锁结束（释放锁）

            //支付后余额正常（如果有分布式锁的话，这里的if语句块里也要加上获取锁失败的情况下，也可以放行，
            // 因为最终要靠数据库去重）
            if (newMoney.doubleValue() > 0) {
                //1（同时进行）：组装消息。以便向payb系统中发送已支付的消息
                String keys = "payA$" + System.currentTimeMillis();
                Map<String, Object> params = new HashMap<>();//将要发送的消息内容都组装进map中
                params.put("userId", userId);
                params.put("orderId", orderId);
                params.put("accountId", accountId);
                params.put("money", payMoney);
                Message msg = new Message(TX_PAYA_TOPIC, TX_PAYA_TAGS, keys,
                        FastJsonConvertUtil.convertObjectToJSON(params).getBytes(RemotingHelper.DEFAULT_CHARSET));
                //可能需要用到的参数。注意：之前map中的4个属性已经加入到Message对象中，
                //这里加入的属性，并不在其中
                params.put("newMoney", newMoney);
                params.put("oldVersion", oldVersion);

                //1（同时进行）：执行本地事务操作：在paya系统的对应数据库中进行扣款的操作

                /**
                 * 细节二：同步阻塞 --- 同步阻塞开始
                 * 目的：同时进行的两件事情（发消息，本地事务），需要等待两件事情都做完才能继续进行order的回调服务
                 */
                CountDownLatch countDownLatch = new CountDownLatch(1);
                params.put("currentCountDown", countDownLatch);

                //用自己封装好的事务消息生产者发送消息并且执行本地事务。
                //第三步：保证代码健壮性，作为第一，二步的兜底，使用数据库的乐观锁方式来做最终的数据去重
                TransactionSendResult sendResult = transactionProducer.sendMsg(msg, params);
                System.err.println("PayA发出事务消息的状态：" + sendResult);

                /**
                 * 同步阻塞等待：在TransactionListenerImpl类中的本地事务处理完后结束
                 */
                countDownLatch.await();

                //消息发送成功&&本地事务成功
                if (sendResult.getSendStatus() == SendStatus.SEND_OK
                        && sendResult.getLocalTransactionState() == LocalTransactionState.COMMIT_MESSAGE) {
                    System.err.println("~~~回调order服务~~~通知order服务，支付整体流程已完毕~~~");
                    //回调order订单服务（向订单服务发消息），通知订单服务，payA的支付已经成功，
                    // 让订单服务继续下面的流程（通知物流服务）
                    callbackService.sendOKMessage(orderId, userId);
                    return CommonReturnType.create("支付成功！");
                } else {
                    return CommonReturnType.create("支付失败！", "Fail");
                }
            } else {
                return CommonReturnType.create("余额不足！", "Fail");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return CommonReturnType.create("支付失败！", "Fail");
        }
    }

}

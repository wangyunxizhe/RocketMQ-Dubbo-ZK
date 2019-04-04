package com.yuan.paya.service.impl;

import com.yuan.paya.entity.CustomerAccount;
import com.yuan.paya.mapper.CustomerAccountMapper;
import com.yuan.paya.response.CommonReturnType;
import com.yuan.paya.service.PayService;
import com.yuan.paya.service.producer.TransactionProducer;
import com.yuan.paya.utils.FastJsonConvertUtil;
import org.apache.rocketmq.client.producer.TransactionSendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public class PayServiceImpl implements PayService {

    public static final String TX_PAYA_TOPIC = "tx_paya_topic";
    public static final String TX_PAYA_TAGS = "paya";

    @Autowired
    private CustomerAccountMapper customerAccountMapper;

    @Autowired
    private TransactionProducer transactionProducer;

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
            BigDecimal payMoney = new BigDecimal(money);
            CustomerAccount old = customerAccountMapper.selectByPrimaryKey(accountId);
            BigDecimal oldMoney = old.getCurrentBalance();//当前账户余额
            int oldVersion = old.getVersion();//当前数据的版本号
            BigDecimal newMoney = oldMoney.subtract(payMoney);//支付：余额 - 支付金额
            if (newMoney.doubleValue() > 0) {//支付后余额正常
                //1（同时进行）：组装消息。以便向payb系统中发送已支付的消息
                String keys = "payA$" + System.currentTimeMillis();
                Map<String, Object> params = new HashMap<>();//将要发送的消息内容都组装进map中
                params.put("userId", userId);
                params.put("orderId", orderId);
                params.put("accountId", accountId);
                params.put("money", money);
                Message msg = new Message(TX_PAYA_TOPIC, TX_PAYA_TAGS, keys,
                        FastJsonConvertUtil.convertObjectToJSON(params).getBytes(RemotingHelper.DEFAULT_CHARSET));
                //用自己封装好的事务消息生产者发送消息
                //可能需要用到的参数。注意：之前map中的4个属性已经加入到Message对象中，
                //这里加入的属性，并不在其中
                params.put("newMoney", newMoney);
                params.put("oldVersion", oldVersion);
                //发出消息
                TransactionSendResult sendResult = transactionProducer.sendMsg(msg, params);
                //1（同时进行）：执行本地事务操作：在paya系统的对应数据库中进行扣款的操作
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return CommonReturnType.create("支付成功！");
    }

}

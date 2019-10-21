package com.yuan.paya.service.producer;

import com.yuan.paya.mapper.CustomerAccountMapper;
import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.client.producer.TransactionListener;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * 该类在分布式事务中负责2件事情：
 * 1，异步执行本地事务，也就是正常的数据库事务
 * 2，做消息的回查
 */
@Component
public class TransactionListenerImpl implements TransactionListener {

    @Autowired
    private CustomerAccountMapper accountMapper;

    /**
     * 该方法见名知意，负责本地事务
     */
    @Override
    public LocalTransactionState executeLocalTransaction(Message msg, Object arg) {
        System.err.println("---------执行本地事务单元---------");
        CountDownLatch currentCountDown = null;
        try {
            //PayServiceImpl在发送消息时，传入的参数是map类型，所以在这里直接强转回map
            Map<String, Object> params = (Map<String, Object>) arg;
            String userId = (String) params.get("userId");//用户id
            String orderId = (String) params.get("orderId");//订单id
            String accountId = (String) params.get("accountId");//账户id
            BigDecimal payMoney = (BigDecimal) params.get("money");//支付金额
            BigDecimal newMoney = (BigDecimal) params.get("newMoney");//支付成功后的余额
            int oldVersion = (int) params.get("oldVersion");//未扣款时的版本号
            currentCountDown = (CountDownLatch) params.get("currentCountDown");
            //进行本地落库操作
            int count = this.accountMapper.updateNewMoney(accountId, newMoney, oldVersion, new Date());
            if (count == 1) {//更新成功
                System.err.println("---------PayA本地更新落库成功---------");
                /**
                 * 同步阻塞结束：PayServiceImpl payment方法的同步阻塞被唤醒，可以继续往下执行，下同。
                 */
                currentCountDown.countDown();
                return LocalTransactionState.COMMIT_MESSAGE;
            } else {
                currentCountDown.countDown();
                return LocalTransactionState.ROLLBACK_MESSAGE;
            }
        } catch (Exception e) {
            e.printStackTrace();
            currentCountDown.countDown();
            return LocalTransactionState.ROLLBACK_MESSAGE;
        }
    }

    /**
     * 该方法负责回查
     * 如果上面的方法返回状态为UNKNOW后，会进入该方法
     */
    @Override
    public LocalTransactionState checkLocalTransaction(MessageExt msg) {
        return null;
    }

}

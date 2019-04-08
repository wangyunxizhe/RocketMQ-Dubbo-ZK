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

@Component
public class TransactionListenerImpl implements TransactionListener {

    @Autowired
    private CustomerAccountMapper accountMapper;

    @Override
    public LocalTransactionState executeLocalTransaction(Message msg, Object arg) {
        System.err.println("---------执行本地事务单元---------");
        //PayServiceImpl在发送消息时，传入的参数是map类型，所以在这里直接强转回map
        Map<String, Object> params = (Map<String, Object>) arg;
        String userId = (String) params.get("userId");//用户id
        String orderId = (String) params.get("orderId");//订单id
        String accountId = (String) params.get("accountId");//账户id
        BigDecimal money = (BigDecimal) params.get("money");//支付金额
        BigDecimal newMoney = (BigDecimal) params.get("newMoney");//支付成功后的余额
        int oldVersion = (int) params.get("oldVersion");//未扣款时的版本号
        try {
            int count = this.accountMapper.updateNewMoney(accountId, newMoney, oldVersion, new Date());
            if (count == 1) {//更新成功
                return LocalTransactionState.COMMIT_MESSAGE;
            } else {
                return LocalTransactionState.ROLLBACK_MESSAGE;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return LocalTransactionState.ROLLBACK_MESSAGE;
        }
    }

    @Override
    public LocalTransactionState checkLocalTransaction(MessageExt msg) {
        return LocalTransactionState.COMMIT_MESSAGE;
    }

}

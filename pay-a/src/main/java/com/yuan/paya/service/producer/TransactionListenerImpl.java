package com.yuan.paya.service.producer;

import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.client.producer.TransactionListener;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
public class TransactionListenerImpl implements TransactionListener {

    @Override
    public LocalTransactionState executeLocalTransaction(Message msg, Object arg) {
        //PayServiceImpl在发送消息时，传入的参数是map类型，所以在这里直接强转回map
        Map<String, Object> params = (Map<String, Object>) arg;
        params.get("userId");
        params.get("orderId");
        params.get("accountId");
        params.get("money");
        params.get("newMoney");
        params.get("oldVersion");
        return LocalTransactionState.COMMIT_MESSAGE;
    }

    @Override
    public LocalTransactionState checkLocalTransaction(MessageExt msg) {
        return LocalTransactionState.COMMIT_MESSAGE;
    }

}

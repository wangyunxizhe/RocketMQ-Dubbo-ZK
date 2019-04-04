package com.yuan.www.transaction;

import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.client.producer.TransactionListener;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;

public class TransactionListenerImpl implements TransactionListener {

    @Override
    public LocalTransactionState executeLocalTransaction(Message msg, Object arg) {
        System.err.println("-----TransactionListener异步执行本地事务的方法-----");
        String callArg = (String) arg;
        System.err.println("消息生产者发送的消息是：" + msg);
        System.err.println("消息生产者发送的消息回调参数是：" + callArg);
        /**
         * 可以在这个方法里执行类似本地的事务方法，如下：
         * tx.begin
         * 数据库的落库操作
         * tx.commit
         */
//        return LocalTransactionState.COMMIT_MESSAGE;
        //如果这里返回的是UNKNOW状态，那么这里会不断轮询checkLocalTransaction方法，继续处理消息
        return LocalTransactionState.UNKNOW;
    }

    @Override
    public LocalTransactionState checkLocalTransaction(MessageExt msg) {
        System.err.println("-----TransactionListener执行回调消息检查的方法-----");
        System.err.println("msg：" + msg);
        return LocalTransactionState.COMMIT_MESSAGE;
    }

}

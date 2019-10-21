package com.yuan.www.transaction;

import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.client.producer.TransactionListener;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;

/**
 * 该类在分布式事务中负责2件事情：
 * 1，异步执行本地事务，也就是正常的数据库事务
 * 2，做消息的回查
 */
public class TransactionListenerImpl implements TransactionListener {

    /**
     * 该方法见名知意，负责本地事务
     * 注意：该方法的第二个参数就是消息生产者再发送消息时放入的回调参数（就是 "~~~我是回调的参数~~~"）
     */
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
        //如果本地事务执行成功，可以返回成功的状态。相对也有失败的状态
//        return LocalTransactionState.COMMIT_MESSAGE;
        //如果这里返回的是UNKNOW状态，那么这里会不断轮询checkLocalTransaction方法，继续处理消息
        return LocalTransactionState.UNKNOW;
    }

    /**
     * 该方法负责回查
     * 在上面的方法返回状态为UNKNOW后，会进入该方法
     */
    @Override
    public LocalTransactionState checkLocalTransaction(MessageExt msg) {
        System.err.println("-----TransactionListener执行回调消息检查的方法-----");
        System.err.println("msg：" + msg);
        return LocalTransactionState.COMMIT_MESSAGE;
    }

}

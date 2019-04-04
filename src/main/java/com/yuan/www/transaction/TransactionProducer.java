package com.yuan.www.transaction;

import com.yuan.www.constants.Const;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.TransactionListener;
import org.apache.rocketmq.client.producer.TransactionMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.common.RemotingHelper;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.*;

/**
 * 事务消息--生产者
 */
public class TransactionProducer {

    public static void main(String[] args) throws MQClientException,
            UnsupportedEncodingException, InterruptedException {
        //1，生产者启动前的准备
        TransactionMQProducer producer = new TransactionMQProducer("tx_producer_group");
        ExecutorService executorService = new ThreadPoolExecutor(2, 5,
                100, TimeUnit.SECONDS,
                new ArrayBlockingQueue<Runnable>(2000),
                new ThreadFactory() {
                    @Override
                    public Thread newThread(Runnable r) {
                        Thread thread = new Thread(r);
                        thread.setName("tx_producer_group" + "-check-thread");
                        return thread;
                    }
                });
        producer.setNamesrvAddr(Const.NAMESRV_ADDR);
        producer.setExecutorService(executorService);
        //该Listener对象主要处理两件事情：1，异步执行本地事务。2，回查
        TransactionListener transactionListener = new TransactionListenerImpl();
        producer.setTransactionListener(transactionListener);
        producer.start();

        //2，准备发送消息
        Message msg = new Message("tx_topic", "TagA", "Key",
                ("rocketMQ for TX").getBytes(RemotingHelper.DEFAULT_CHARSET));
        //这两个参数在TransactionListenerImpl类中的executeLocalTransaction方法里都可以拿得到
        producer.sendMessageInTransaction(msg, "~~~我是回调的参数~~~");
        Thread.sleep(Integer.MAX_VALUE);
        producer.shutdown();
    }

}

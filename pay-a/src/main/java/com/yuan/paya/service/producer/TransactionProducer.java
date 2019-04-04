package com.yuan.paya.service.producer;

import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.TransactionMQProducer;
import org.apache.rocketmq.client.producer.TransactionSendResult;
import org.apache.rocketmq.common.message.Message;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.*;

@Component
public class TransactionProducer implements InitializingBean {

    private TransactionMQProducer producer;

    private ExecutorService executorService;

    @Autowired
    private TransactionListenerImpl transactionListener;

    private static final String NAMESERV_ADDR = "172.20.10.7:9876;172.20.10.8:9876;172.20.10.9:9876;172.20.10.10:9876";

    private static final String PRODUCER_GROUP_NAME = "tx_paya_producer_group_name";

    private TransactionProducer() {
        this.producer = new TransactionMQProducer(PRODUCER_GROUP_NAME);
        this.executorService = new ThreadPoolExecutor(2, 5,
                100, TimeUnit.SECONDS,
                new ArrayBlockingQueue<Runnable>(2000),
                new ThreadFactory() {
                    @Override
                    public Thread newThread(Runnable r) {
                        Thread thread = new Thread(r);
                        thread.setName(PRODUCER_GROUP_NAME + "-check-thread");
                        return thread;
                    }
                });
        this.producer.setExecutorService(executorService);
        this.producer.setNamesrvAddr(NAMESERV_ADDR);
    }

    /**
     * 实现InitializingBean接口重写该方法
     * 解决问题：防止在初始化该类的时候，而TransactionListenerImpl类却并没有初始化完毕，造成依赖报错
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        this.producer.setTransactionListener(transactionListener);
        start();
    }

    private void start() {
        try {
            this.producer.start();
        } catch (MQClientException e) {
            e.printStackTrace();
        }
    }

    public void shutdown() {
        this.producer.shutdown();
    }

    public TransactionSendResult sendMsg(Message message, Object arg) {
        TransactionSendResult sendResult = null;
        try {
            sendResult = this.producer.sendMessageInTransaction(message, arg);
        } catch (Exception e) {
        }
        return sendResult;
    }
}

package com.yuan.order.service.producer;

import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.MessageQueueSelector;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageQueue;
import org.springframework.stereotype.Component;
import java.util.List;

/**
 * 订单系统--顺序消息生产者
 */
@Component
public class OrderProducer {

    private DefaultMQProducer producer;

    public static final String NAMESERVER = "172.20.10.7:9876;172.20.10.8:9876;172.20.10.9:9876;172.20.10.10:9876";

    public static final String PRODUCER_GROUP_NAME = "orderly_producer_group_name";

    private OrderProducer() {
        this.producer = new DefaultMQProducer(PRODUCER_GROUP_NAME);
        this.producer.setNamesrvAddr(NAMESERVER);
        this.producer.setSendMsgTimeout(3000);
        start();
    }

    public void start() {
        try {
            this.producer.start();
        } catch (MQClientException e) {
            e.printStackTrace();
        }
    }

    public void shutdown() {
        this.producer.shutdown();
    }

    public void sendOrderlyMessages(List<Message> msgList, int messageQueueNumber) {
        for (Message msg : msgList) {
            try {
                this.producer.send(msg, new MessageQueueSelector() {
                    @Override
                    public MessageQueue select(List<MessageQueue> mqs, Message msg, Object arg) {
                        Integer id = (Integer) arg;
                        return mqs.get(id);
                    }
                }, messageQueueNumber);//将传过来的参数messageQueueNumber（supplier_id）作为指定队列的下标
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}

package com.yuan.www.transaction;

import com.yuan.www.constants.Const;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.remoting.common.RemotingHelper;

import java.util.List;

/**
 * 事务消息--消费者
 * 事务消息的消费者与其他普通的消费者没什么区别，正常写即可
 */
public class TransactionConsumer {

    public static void main(String[] args) throws MQClientException {
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("tx_consumer_group");
        consumer.setConsumeThreadMin(10);
        consumer.setConsumeThreadMax(20);
        consumer.setNamesrvAddr(Const.NAMESRV_ADDR);
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);
        //订阅指定主题，不进行topic的过滤
        consumer.subscribe("tx_topic", "*");

        consumer.registerMessageListener(new MessageListenerConcurrently() {

            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs,
                                                            ConsumeConcurrentlyContext context) {
                MessageExt me = msgs.get(0);
                try {
                    String topic = me.getTopic();
                    String tags = me.getTags();
                    String keys = me.getKeys();
                    String body = new String(me.getBody(), RemotingHelper.DEFAULT_CHARSET);
                    System.err.println("收到了事务消息：topic " + topic + "，tags " + tags +
                            "，keys " + keys + "，body " + body);
                } catch (Exception e) {
                    e.printStackTrace();
                    return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                }
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }

        });

        consumer.start();
        System.err.println("事务消息的消费者已启动。。。");
    }

}

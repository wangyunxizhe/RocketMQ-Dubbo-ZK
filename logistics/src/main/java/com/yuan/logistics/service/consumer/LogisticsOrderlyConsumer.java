package com.yuan.logistics.service.consumer;

import com.yuan.logistics.utils.FastJsonConvertUtil;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerOrderly;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageConst;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 物流系统--顺序消息消费者
 */
@Component
public class LogisticsOrderlyConsumer {

    private DefaultMQPushConsumer consumer;

    public static final String NAMESERVER = "172.20.10.7:9876;172.20.10.8:9876;172.20.10.9:9876;172.20.10.10:9876";

    public static final String CONSUMER_GROUP_NAME = "orderly_consumer_group_name";

    public static final String LOGISTICS_TOPIC = "logistics_topic";//订单-物流系统消息主题

    public static final String LOGISTICS_TAGS = "logistics_tags";//订单-物流系统消息标签

    private LogisticsOrderlyConsumer() throws MQClientException {
        this.consumer = new DefaultMQPushConsumer(CONSUMER_GROUP_NAME);
        this.consumer.setConsumeThreadMin(10);
        this.consumer.setConsumeThreadMax(30);
        this.consumer.setNamesrvAddr(NAMESERVER);
        this.consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);
        this.consumer.subscribe(LOGISTICS_TOPIC, LOGISTICS_TAGS);
        this.consumer.registerMessageListener(new LogisticsOrderlyListener());
        this.consumer.start();
    }

    class LogisticsOrderlyListener implements MessageListenerOrderly {

        @Override
        public ConsumeOrderlyStatus consumeMessage(List<MessageExt> msgs, ConsumeOrderlyContext context) {
            for (MessageExt msg : msgs) {
                try {
                    String topic = msg.getTopic();
                    String msgBody = new String(msg.getBody(), "utf-8");
                    String tags = msg.getTags();
                    String keys = msg.getKeys();
                    System.err.println("Logistics收到消息：" + "  topic :" + topic + "  ,tags : " + tags
                            + "  ,keys :" + keys + "  ,msg : " + msgBody);
                    String orignMsgId = msg.getProperties().get(MessageConst.PROPERTY_ORIGIN_MESSAGE_ID);
                    System.err.println("orignMsgId: " + orignMsgId);
                    //获取order中OrderServiceImpl发送的消息内容
                    Map<String, Object> body = FastJsonConvertUtil.convertJSONToObject(msgBody, Map.class);
                    String orderId = (String) body.get("orderId");//订单id
                    String userId = (String) body.get("userId");//用户id
                    String step = (String) body.get("step");//模拟的操作步骤
                    System.err.println("业务操作步骤: " + step);
                } catch (Exception e) {
                    e.printStackTrace();
                    return ConsumeOrderlyStatus.SUSPEND_CURRENT_QUEUE_A_MOMENT;
                }
            }
            return ConsumeOrderlyStatus.SUCCESS;
        }
    }

}

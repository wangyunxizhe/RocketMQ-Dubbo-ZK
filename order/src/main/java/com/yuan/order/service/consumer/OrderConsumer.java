package com.yuan.order.service.consumer;

import com.yuan.order.constants.OrderStauts;
import com.yuan.order.mapper.OrderMapper;
import com.yuan.order.service.OrderService;
import com.yuan.order.utils.FastJsonConvertUtil;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageConst;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Component
public class OrderConsumer {

    private DefaultMQPushConsumer consumer;

    public static final String CALLBACK_PAY_TOPIC = "callback_paya_topic";

    public static final String CALLBACK_PAY_TAGS = "callback_paya";

    public static final String NAMESERVER = "172.20.10.7:9876;172.20.10.8:9876;172.20.10.9:9876;172.20.10.10:9876";

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderService orderService;

    public OrderConsumer() throws MQClientException {
        consumer = new DefaultMQPushConsumer("callback_paya_consumer_group");
        consumer.setConsumeThreadMin(10);
        consumer.setConsumeThreadMax(50);
        consumer.setNamesrvAddr(NAMESERVER);
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);
        consumer.subscribe(CALLBACK_PAY_TOPIC, CALLBACK_PAY_TAGS);
        consumer.registerMessageListener(new MessageListenerConcurrently4Pay());
        consumer.start();
    }

    class MessageListenerConcurrently4Pay implements MessageListenerConcurrently {

        @Override
        public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
            MessageExt msg = msgs.get(0);
            try {
                String topic = msg.getTopic();
                String msgBody = new String(msg.getBody(), "utf-8");
                String tags = msg.getTags();
                String keys = msg.getKeys();
                System.err.println("Order收到消息：" + "  topic :" + topic + "  ,tags : " + tags
                        + "  ,keys :" + keys + "  ,msg : " + msgBody);
                String orignMsgId = msg.getProperties().get(MessageConst.PROPERTY_ORIGIN_MESSAGE_ID);
                System.err.println("orignMsgId: " + orignMsgId);
                //获取payA中CallbackService发送的消息体内容
                Map<String, Object> body = FastJsonConvertUtil.convertJSONToObject(msgBody, Map.class);
                String orderId = (String) body.get("orderId");//订单id
                String userId = (String) body.get("userId");//用户id
                String status = (String) body.get("status");//订单状态
                Date currentTime = new Date();
                if (status.equals(OrderStauts.ORDER_PAYED.getValue())) {
                    int count = orderMapper.updateOrderStatus(orderId, status, "wyuan", currentTime);
                    if (count == 1) {
                        System.err.println("---------Order本地更新落库成功---------");
//                        orderService.sendOrderlyMessage4Pkg(userId, orderId);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                return ConsumeConcurrentlyStatus.RECONSUME_LATER;
            }
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        }

    }

}

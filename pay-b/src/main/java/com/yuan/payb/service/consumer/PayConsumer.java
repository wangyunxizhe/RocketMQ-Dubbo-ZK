package com.yuan.payb.service.consumer;

import com.yuan.payb.entity.PlatformAccount;
import com.yuan.payb.mapper.PlatformAccountMapper;
import com.yuan.payb.utils.FastJsonConvertUtil;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Component
public class PayConsumer {

    private DefaultMQPushConsumer consumer;

    private static final String NAMESERVER = "172.20.10.7:9876;172.20.10.8:9876;172.20.10.9:9876;172.20.10.10:9876";

    private static final String CONSUMER_GROUP_NAME = "tx_pay_consumer_group_name";

    public static final String TX_PAY_TOPIC = "tx_paya_topic";

    public static final String TX_PAY_TAGS = "paya";

    @Autowired
    private PlatformAccountMapper platformAccountMapper;

    private PayConsumer() {
        try {
            this.consumer = new DefaultMQPushConsumer(CONSUMER_GROUP_NAME);
            this.consumer.setConsumeThreadMin(10);
            this.consumer.setConsumeThreadMax(30);
            this.consumer.setNamesrvAddr(NAMESERVER);
            this.consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);
            this.consumer.subscribe(TX_PAY_TOPIC, TX_PAY_TAGS);
            this.consumer.registerMessageListener(new MessageListenerConcurrently4Pay());
            this.consumer.start();
        } catch (MQClientException e) {
            e.printStackTrace();
        }
    }

    class MessageListenerConcurrently4Pay implements MessageListenerConcurrently {

        @Override
        public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
            MessageExt msg = msgs.get(0);
            try {
                String topic = msg.getTopic();
                String tags = msg.getTags();
                String keys = msg.getKeys();
                String body = new String(msg.getBody(), RemotingHelper.DEFAULT_CHARSET);
                System.err.println("PayB收到事务消息, topic: " + topic + ", tags: " + tags
                        + ", keys: " + keys + ", body: " + body);

                //消息一单过来的时候（去重 幂等操作）
                //数据库主键去重<去重表 keys>
                //insert table --> insert ok & primary key
                Map<String, Object> paramsBody = FastJsonConvertUtil.convertJSONToObject(body, Map.class);
                String userId = (String) paramsBody.get("userId");//用户id
                String accountId = (String) paramsBody.get("accountId");//账户id
                String orderId = (String) paramsBody.get("orderId");//统一的订单
                Integer money = (Integer) paramsBody.get("money");//当前的收益款
                BigDecimal payMoney = new BigDecimal(money);

                PlatformAccount pa = platformAccountMapper.selectByPrimaryKey("platform001");
                pa.setCurrentBalance(pa.getCurrentBalance().add(payMoney));
                Date currentTime = new Date();
                pa.setVersion(pa.getVersion() + 1);
                pa.setDateTime(currentTime);
                pa.setUpdateTime(currentTime);
                System.err.println("---------PayB本地更新落库成功---------");
                platformAccountMapper.updateByPrimaryKeySelective(pa);
            } catch (Exception e) {
                e.printStackTrace();
                //msg.getReconsumeTimes();
                //如果处理多次操作还是失败, 记录失败日志（做补偿 回顾 人工处理）
                return ConsumeConcurrentlyStatus.RECONSUME_LATER;
            }
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        }

    }


}

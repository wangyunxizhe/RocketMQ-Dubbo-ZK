package com.yuan.www.quickstart;

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
 * 单机MQ时使用的--消息消费者
 */
public class Consumer {

    public static void main(String[] args) throws MQClientException {
        //1，创建消费者群组
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("test_quick_consumer_name");
        //2，设置NameSRV地址
        consumer.setNamesrvAddr(Const.NAMESRV_ADDR);
        //3，从消息最后端开始消费
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);
        //4，消费指定Topic和Tag下的消息
        //注意：第二个参数也可以写*，代表只要是topic为test_quick_topic下的所有消息不分标签，都消费
        consumer.subscribe("test_quick_topic", "TagA");
        //5，对指定Topic进行监听
        consumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs,
                                                            ConsumeConcurrentlyContext context) {
                MessageExt me = msgs.get(0);//因为测试用的生产者中发送的都是单条消息，获取0即可
                try {
                    //获取测试用消费端定义的消息的一些信息
                    String topic = me.getTopic();
                    String tags = me.getTags();
                    String keys = me.getKeys();
                    //注意：测试在这里故意报错，进入catch语句块
                    if (keys.equals("key1")) {
                        System.err.println("故意报错。。。进入catch。。。");
                        int err = 1 / 0;
                    }
                    String msgBody = new String(me.getBody(), RemotingHelper.DEFAULT_CHARSET);
                    System.err.println("topic：" + topic + " tags：" + tags + " keys：" + keys + " body：" + msgBody);
                } catch (Exception e) {
                    e.printStackTrace();
                    //注意：报错后进入catch，return ConsumeConcurrentlyStatus.RECONSUME_LATER会让该消费端不断重试
                    int retryTimes = me.getReconsumeTimes();//获取重试的次数
                    System.err.println("重试了" + retryTimes + "次");
                    if (retryTimes == 3) {//重试3次，不处理了，记录到日志中做补偿处理
                        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                    }
                    return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                }
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });
        //5，启动消费端
        consumer.start();
        System.err.println("consumer start...");
    }
}

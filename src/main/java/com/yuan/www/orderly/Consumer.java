package com.yuan.www.orderly;

import com.yuan.www.constants.Const;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerOrderly;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;

import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 顺序消息--消费者
 */
public class Consumer {

    public Consumer() throws Exception {
        String group_name = "test_orderly_consumer_name";
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(group_name);
        consumer.setNamesrvAddr(Const.NAMESRV_ADDR_MASTER_SLAVE);
        //设置Consumer第一次启动是从队列头部开始消费还是队列尾部开始消费
        //如果非第一次启动，那么按照上次消费的位置继续消费
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);
        //订阅主题，标签
        consumer.subscribe("test_order_topic", "TagA");
        //注册监听
        consumer.registerMessageListener(new Listener());
        consumer.start();
        System.out.println("Consumer Start...");
    }

    class Listener implements MessageListenerOrderly {

        private Random random = new Random();

        @Override
        public ConsumeOrderlyStatus consumeMessage(List<MessageExt> msqs, ConsumeOrderlyContext context) {
            //设置自动提交
            context.setAutoCommit(true);
            for (MessageExt msg : msqs) {
                System.out.println(msg + ",context:" + new String(msg.getBody()));
                try {
                    //模拟业务逻辑处理中
                    TimeUnit.SECONDS.sleep(random.nextInt(4) + 1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return ConsumeOrderlyStatus.SUCCESS;
        }
    }

    public static void main(String[] args) throws Exception {
        Consumer consumer = new Consumer();
    }
}

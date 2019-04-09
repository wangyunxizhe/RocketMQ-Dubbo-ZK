package com.yuan.www.orderly;

import com.yuan.www.constants.Const;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.MessageQueueSelector;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.apache.rocketmq.remoting.exception.RemotingException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 顺序消息--生产者
 */
public class Producer {

    public static void main(String[] args) throws MQClientException, UnsupportedEncodingException,
            RemotingException, InterruptedException, MQBrokerException {
        String group_name = "test_orderly_producer_name";
        DefaultMQProducer producer = new DefaultMQProducer(group_name);
        producer.setNamesrvAddr(Const.NAMESRV_ADDR_MASTER_SLAVE);
        producer.start();

        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateStr = sdf.format(date);
        //模拟一个大业务中需要发5条消息，而这5条消息必须是顺序的
        for (int i = 0; i < 5; i++) {
            String body = dateStr + " Orderly MQ " + i;
            Message msg = new Message("test_order_topic", "TagA", "Key" + i,
                    body.getBytes(RemotingHelper.DEFAULT_CHARSET));
            //发送消息：如果要实现顺序消费，则必须实现自己的MessageQueueSelector，保证消息进入到同一个队列中
            SendResult sendResult = producer.send(msg, new MessageQueueSelector() {
                @Override
                public MessageQueue select(List<MessageQueue> mqs, Message msg, Object arg) {
                    Integer id = (Integer) arg;
                    System.out.println("id：" + id);
                    return mqs.get(id);
                }
            }, 1);//这里的参数1是队列的下标，也可以理解为将msg都发送到1号队列中
            System.out.println(sendResult + ",body:" + body);
        }
        //第二波顺序消息
        for (int i = 0; i < 5; i++) {
            String body = dateStr + " Orderly MQ " + i;
            Message msg = new Message("test_order_topic", "TagA", "Key" + i,
                    body.getBytes(RemotingHelper.DEFAULT_CHARSET));
            //发送消息：如果要实现顺序消费，则必须实现自己的MessageQueueSelector，保证消息进入到同一个队列中
            SendResult sendResult = producer.send(msg, new MessageQueueSelector() {
                @Override
                public MessageQueue select(List<MessageQueue> mqs, Message msg, Object arg) {
                    Integer id = (Integer) arg;
                    System.out.println("id：" + id);
                    return mqs.get(id);
                }
            }, 2);//发送到2号队列中
            System.out.println(sendResult + ",body:" + body);
        }
        //第三波顺序消息
        for (int i = 0; i < 5; i++) {
            String body = dateStr + " Orderly MQ " + i;
            Message msg = new Message("test_order_topic", "TagA", "Key" + i,
                    body.getBytes(RemotingHelper.DEFAULT_CHARSET));
            //发送消息：如果要实现顺序消费，则必须实现自己的MessageQueueSelector，保证消息进入到同一个队列中
            SendResult sendResult = producer.send(msg, new MessageQueueSelector() {
                @Override
                public MessageQueue select(List<MessageQueue> mqs, Message msg, Object arg) {
                    Integer id = (Integer) arg;
                    System.out.println("id：" + id);
                    return mqs.get(id);
                }
            }, 3);//发送到3号队列中
            System.out.println(sendResult + ",body:" + body);
        }
        producer.shutdown();
    }

}

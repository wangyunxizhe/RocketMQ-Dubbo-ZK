package com.yuan.www.quickstart;

import com.yuan.www.constants.Const;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.exception.RemotingException;

/**
 * 单机MQ时使用的--消息生产者
 */
public class Producer {

    public static void main(String[] args) throws MQClientException,
            RemotingException, InterruptedException, MQBrokerException {
        //1，创建一个生产者群组，组名在一个应用中必须唯一
        DefaultMQProducer producer = new DefaultMQProducer("test_quick_producer_name");
        //2，设置NameSRV地址
        producer.setNamesrvAddr(Const.NAMESRV_ADDR);
        //3，启动
        producer.start();
        //发送5次
        for (int i = 0; i < 5; i++) {
            //4，创建消息对象，参数分别为：topic（消息主题：方便消费端根据主题名称订阅），
            // tags（标签：便于用于根据标签来过滤消息），
            // keys（用户自定义key：用于消息的唯一标识），
            // body（消息体：消息的内容）
            Message msg = new Message("test_quick_topic", "TagA",
                    "key" + i, ("My First RocketMQ" + i).getBytes());
            //5，发送消息
            SendResult rs = producer.send(msg);//返回值可以看见消息发送后的接收状态
            System.err.println("消息发出：" + rs.getSendStatus());
            System.err.println("SendResult对象：" + rs);
        }
        //6，关闭消息
        producer.shutdown();
    }

}

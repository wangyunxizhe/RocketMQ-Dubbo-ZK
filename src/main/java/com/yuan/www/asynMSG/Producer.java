package com.yuan.www.asynMSG;

import com.yuan.www.constants.Const;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.exception.RemotingException;

/**
 * 一主一从异步--消息生产者
 */
public class Producer {

    public static void main(String[] args) throws MQClientException,
            RemotingException, InterruptedException, MQBrokerException {
        //1，创建一个生产者群组，组名在一个应用中必须唯一
        DefaultMQProducer producer = new DefaultMQProducer("test_quick_producer_name");
        //2，设置NameSRV地址
        producer.setNamesrvAddr(Const.NAMESRV_ADDR_MASTER_SLAVE);
        //3，启动
        producer.start();
        //发送1次
        for (int i = 0; i < 1; i++) {
            //4，创建消息对象，参数分别为：topic（消息主题：方便消费端根据主题名称订阅），
            // tags（标签：便于用于根据标签来过滤消息），
            // keys（用户自定义key：用于消息的唯一标识），
            // body（消息体：消息的内容）
            Message msg = new Message("test_quick_topic", "TagA",
                    "asynKey" + i, ("My asyn RocketMQ" + i).getBytes());
            //5，异步发送消息
            producer.send(msg, new SendCallback() {
                @Override
                public void onSuccess(SendResult sendResult) {
                    System.err.println("MsgId：" + sendResult.getMsgId() +
                            " SendStatus：" + sendResult.getSendStatus());
                }

                //如果需要消息百分百投递成功，则需要在onException方法中做业务处理
                @Override
                public void onException(Throwable e) {
                    e.printStackTrace();
                    System.err.println("XXXXX发送失败XXXXX");
                }
            });
        }
        //6，关闭消息
//        producer.shutdown();
    }

}

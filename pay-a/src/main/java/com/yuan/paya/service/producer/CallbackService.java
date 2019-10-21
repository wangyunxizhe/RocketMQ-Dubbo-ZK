package com.yuan.paya.service.producer;

import com.yuan.paya.utils.FastJsonConvertUtil;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

/**
 * 负责payA模块支付成功后，向order（订单）模块的消息回调
 */
@Service
public class CallbackService {

    public static final String CALLBACK_PAY_TOPIC = "callback_paya_topic";

    public static final String CALLBACK_PAY_TAGS = "callback_paya";

    @Autowired
    private SyncProducer syncProducer;//自建类，同步发送的消息生产者

    public void sendOKMessage(String orderId, String userId) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("orderId", orderId);
        params.put("status", "2");    //ok

        String keys = "CallBackPayA$" + System.currentTimeMillis();
        Message message = new Message(CALLBACK_PAY_TOPIC, CALLBACK_PAY_TAGS, keys,
                FastJsonConvertUtil.convertObjectToJSON(params).getBytes());
        /**
         * 使用同步消息发送可以拿到返回值，以便于当即重试
         * 也可以使用异步的消息生产者发送异步消息，
         * 如果使用异步消息可以使用BrokerMessageLog表记录发出的消息详情，以便在发送失败时做对应操作
         */
        SendResult ret = syncProducer.sendMessage(message);
        System.err.println("PayA发出普通消息：" + ret);
    }


}

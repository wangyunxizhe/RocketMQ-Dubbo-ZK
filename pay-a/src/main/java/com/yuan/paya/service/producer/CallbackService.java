package com.yuan.paya.service.producer;

import com.yuan.paya.utils.FastJsonConvertUtil;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class CallbackService {

    public static final String CALLBACK_PAY_TOPIC = "callback_paya_topic";

    public static final String CALLBACK_PAY_TAGS = "callback_paya";

    public static final String NAMESERVER = "172.20.10.7:9876;172.20.10.8:9876;172.20.10.9:9876;172.20.10.10:9876";

    @Autowired
    private SyncProducer syncProducer;

    public void sendOKMessage(String orderId, String userId) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("orderId", orderId);
        params.put("status", "2");    //ok

        String keys = "CallBackPayA$" + System.currentTimeMillis();
        Message message = new Message(CALLBACK_PAY_TOPIC, CALLBACK_PAY_TAGS, keys,
                FastJsonConvertUtil.convertObjectToJSON(params).getBytes());

        SendResult ret = syncProducer.sendMessage(message);
    }


}

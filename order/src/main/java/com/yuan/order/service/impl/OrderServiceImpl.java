package com.yuan.order.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.yuan.order.constants.OrderStauts;
import com.yuan.order.entity.Order;
import com.yuan.order.mapper.OrderMapper;
import com.yuan.order.response.CommonReturnType;
import com.yuan.order.service.OrderService;
import com.yuan.order.service.producer.OrderProducer;
import com.yuan.order.utils.FastJsonConvertUtil;
import com.yuan.store.service.api.StoreServiceApi;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    public static final String LOGISTICS_TOPIC = "logistics_topic";//订单-物流系统消息主题

    public static final String LOGISTICS_TAGS = "logistics_tags";//订单-物流系统消息标签

    @Reference(version = "1.0.0",
            application = "${dubbo.application.id}",
            interfaceName = "com.yuan.store.service.api.StoreServiceApi",
            check = false,
            timeout = 3000,
            retries = 0
    )
    private StoreServiceApi storeServiceApi;//引用库存模块的dubbo服务

    @Autowired
    private OrderProducer orderProducer;

    /**
     * 创建订单
     */
    @Override
    public CommonReturnType createOrder(String cityId, String platformId,
                                        String userId, String supplierId, String goodsId) {
        CommonReturnType rs = new CommonReturnType();
        try {
            Order order = new Order();
            order.setOrderId(UUID.randomUUID().toString());
            order.setOrderType("1");
            order.setCityId(cityId);
            order.setPlatformId(platformId);
            order.setUserId(userId);
            order.setSupplierId(supplierId);
            order.setGoodsId(goodsId);
            order.setOrderStatus(OrderStauts.ORDER_CREATE.getValue());
            order.setRemark("订单创建成功");
            order.setCreateBy("创建人");
            order.setCreateTime(new Date());
            order.setUpdateBy("更新人");
            order.setUpdateTime(new Date());
            //1，调用库存模块的dubbo服务：查询现有库存--返回当前数据版本号
            int nowVersion = storeServiceApi.selectVersion(supplierId, goodsId);
            //2，调用库存模块的dubbo服务：根据返回版本号更新库存
            int is_update = storeServiceApi.updateStoreCountByVersion(nowVersion, supplierId, goodsId,
                    "更新人", new Date());
            if (is_update == 1) {//有一条被更新了
                orderMapper.insertSelective(order);
                rs = CommonReturnType.create("下单成功！", "success");
            } else if (is_update == 0) {//没更新：1，可能是高并发时乐观锁成效了；2，可能是库存不足
                //调用库存模块的dubbo服务：查看当前库存分析未更新的原因
                int storeCount = storeServiceApi.selectStoreCount(supplierId, goodsId);
                if (storeCount == 0) {
                    rs = CommonReturnType.create("当前库存不足。。。", "fail");
                } else {
                    rs = CommonReturnType.create("并发导致乐观锁生效。。。", "fail");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            rs = CommonReturnType.create("下单服务异常！请检查", "fail");
        }
        return rs;
    }

    /**
     * 订单模块向物流模块发送顺序消息
     * 以业务中需要顺序处理的场景为例
     */
    @Override
    public void sendOrderlyMessage4Pkg(String userId, String orderId) {
        try {
            List<Message> msgList = new ArrayList<>();
            //业务中的第一步
            Map<String, Object> param1 = new HashMap<>();
            param1.put("userId", userId);
            param1.put("orderId", orderId);
            param1.put("step", "第一步（模拟操作）：创建包裹");

            String key1 = "Order$" + System.currentTimeMillis();

            Message msg1 = new Message(LOGISTICS_TOPIC, LOGISTICS_TAGS, key1,
                    FastJsonConvertUtil.convertObjectToJSON(param1).getBytes(RemotingHelper.DEFAULT_CHARSET));
            msgList.add(msg1);
            //业务中的第二步
            Map<String, Object> param2 = new HashMap<>();
            param2.put("userId", userId);
            param2.put("orderId", orderId);
            param2.put("step", "第二步（模拟操作）：发送物流通知");

            String key2 = "Order$" + System.currentTimeMillis();

            Message msg2 = new Message(LOGISTICS_TOPIC, LOGISTICS_TAGS, key2,
                    FastJsonConvertUtil.convertObjectToJSON(param2).getBytes(RemotingHelper.DEFAULT_CHARSET));
            msgList.add(msg2);

            //顺序消息投递 正常工作中的做法：供应商ID与topic messageQueueID 进行绑定对应的
            //在这里将表中查出的supplier_id作为messageQueueID，以便实现顺序消息的简单测试
            Order order = orderMapper.selectByPrimaryKey(orderId);
            int msgQueueNumber = Integer.parseInt(order.getSupplierId());

            //将组装好的消息数组msgList，使用自定义的顺序消息生产者发出
            orderProducer.sendOrderlyMessages(msgList, msgQueueNumber);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

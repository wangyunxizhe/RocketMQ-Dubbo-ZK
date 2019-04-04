package com.yuan.order.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.yuan.order.constants.OrderStauts;
import com.yuan.order.entity.Order;
import com.yuan.order.mapper.OrderMapper;
import com.yuan.order.response.CommonReturnType;
import com.yuan.order.service.OrderService;
import com.yuan.store.service.api.StoreServiceApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Reference(version = "1.0.0",
            application = "${dubbo.application.id}",
            interfaceName = "com.yuan.store.service.api.StoreServiceApi",
            check = false,
            timeout = 3000,
            retries = 0
    )
    private StoreServiceApi storeServiceApi;

    @Override
    public CommonReturnType createOrder(String cityId, String platformId, String userId, String supplierId, String goodsId) {
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

            //1，查询现有库存--返回当前数据版本号
            int nowVersion = storeServiceApi.selectVersion(supplierId, goodsId);
            //2，根据返回版本号更新库存
            int is_update = storeServiceApi.updateStoreCountByVersion(nowVersion, supplierId, goodsId,
                    "更新人", new Date());
            if (is_update == 1) {//有一条被更新了
                orderMapper.insertSelective(order);
                rs = CommonReturnType.create("下单成功！", "success");
            } else if (is_update == 0) {//没更新：1，可能是高并发时乐观锁成效了；2，可能是库存不足
                int storeCount = storeServiceApi.selectStoreCount(supplierId, goodsId);//当前库存
                if (storeCount == 0) {
                    rs = CommonReturnType.create("当前库存不足。。。", "fail");
                } else {
                    rs = CommonReturnType.create("乐观锁生效。。。", "fail");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            rs = CommonReturnType.create("下单服务异常！请检查", "fail");
        }
        return rs;
    }

}

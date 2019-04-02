package com.yuan.order.service;

import com.yuan.order.response.CommonReturnType;

public interface OrderService {

    CommonReturnType createOrder(String cityId, String platformId, String userId, String supplierId, String goodsId);
}

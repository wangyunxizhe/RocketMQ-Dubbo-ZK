package com.yuan.paya.service;

import com.yuan.paya.response.CommonReturnType;

public interface PayService {

    /**
     * 支付操作
     *
     * @param userId    用户id
     * @param orderId   订单id
     * @param accountId 账户id
     * @param money     支付金额
     */
    CommonReturnType payment(String userId, String orderId, String accountId, double money);

}

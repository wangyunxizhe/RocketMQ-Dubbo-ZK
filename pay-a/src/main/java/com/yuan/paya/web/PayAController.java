package com.yuan.paya.web;

import com.yuan.paya.response.CommonReturnType;
import com.yuan.paya.service.PayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PayAController {

    @Autowired
    private PayService payService;

    @RequestMapping("pay")
    public CommonReturnType pay(@RequestParam("userId") String userId,
                                @RequestParam("orderId") String orderId,
                                @RequestParam("accountId") String accountId,
                                @RequestParam("money") Double money) throws Exception {
        return payService.payment(userId, orderId, accountId, money);
    }

}

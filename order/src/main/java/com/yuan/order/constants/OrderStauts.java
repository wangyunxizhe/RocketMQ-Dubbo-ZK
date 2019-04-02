package com.yuan.order.constants;

/**
 * 订单状态
 */
public enum OrderStauts {

    ORDER_CREATE("1"),

    ORDER_PAYED("2"),

    ORDER_FAIL("3");

    private String status;

    private OrderStauts(String status) {
        this.status = status;
    }

    public String getValue() {
        return status;
    }
}

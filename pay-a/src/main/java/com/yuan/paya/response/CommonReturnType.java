package com.yuan.paya.response;

/**
 * 页面通用返回格式
 */
public class CommonReturnType {

    private String status;//请求回调状态：success或者fail
    private Object data;//请求回调数据：若status=success，则返回正确的json数据；若status=fail，则返回通用错误码格式

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    //定义通用的创建方法
    public static CommonReturnType create(Object result) {
        return CommonReturnType.create(result, "success");
    }

    public static CommonReturnType create(Object result, String status) {
        CommonReturnType type = new CommonReturnType();
        type.setStatus(status);
        type.setData(result);
        return type;
    }
}

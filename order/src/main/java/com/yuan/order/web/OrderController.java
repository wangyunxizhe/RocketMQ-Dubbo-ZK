package com.yuan.order.web;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import com.yuan.order.response.CommonReturnType;
import com.yuan.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 下订单业务-控制层
 */
@RestController
public class OrderController {

    @Autowired
    private OrderService orderService;

    @HystrixCommand(
            commandKey = "createOrder",//自定义，一般规范是跟方法的url全路径相同
            commandProperties = {
                    @HystrixProperty(name = "execution.timeout.enabled", value = "true"),
                    @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "3000")//设置超时时间
            },
            //设置超时后的降级方法（3秒钟后如果/createOrder请求仍不成功，就走降级方法 createOrderFallbackMethod4Timeout）
            fallbackMethod = "createOrderFallbackMethod4Timeout"
    )
    @RequestMapping("/createOrder")
    public CommonReturnType createOrder(@RequestParam("cityId") String cityId, @RequestParam("platformId") String platformId,
                                        @RequestParam("userId") String userId, @RequestParam("supplierId") String supplierId,
                                        @RequestParam("goodsId") String goodsId) throws Exception {
        //睡5秒，人工超时，强制走降级方法 createOrderFallbackMethod4Timeout
        //Thread.sleep(5000);
        return orderService.createOrder(cityId, platformId, userId, supplierId, goodsId);
    }

    /**
     * 注意：降级后的方法一定要和源方法保持参数一致，返回值一致，抛出的异常一致
     */
    public CommonReturnType createOrderFallbackMethod4Timeout(@RequestParam("cityId") String cityId,
                                                              @RequestParam("platformId") String platformId,
                                                              @RequestParam("userId") String userId,
                                                              @RequestParam("supplierId") String supplierId,
                                                              @RequestParam("goodsId") String goodsId) throws Exception {
        System.err.println("------------执行超时降级方法------------");
        return CommonReturnType.create("Hystrix Timeout!", "fail");
    }

    @HystrixCommand(
            commandKey = "createOrderTest1",//自定义，一般规范是跟方法的url全路径相同
            commandProperties = {
                    @HystrixProperty(name = "execution.isolation.strategy", value = "THREAD")//设置线程池方法（限流）
            },
            threadPoolKey = "createOrderTest1ThreadPool",
            threadPoolProperties = {
                    @HystrixProperty(name = "coreSize", value = "10"),//设置核心线程数：初始化时允许链接10个线程
                    @HystrixProperty(name = "maxQueueSize", value = "20000"),
                    @HystrixProperty(name = "queueSizeRejectionThreshold", value = "30")
            },
            //当请求线程过多时，执行降级方法 createOrderFallbackMethod4Thread
            fallbackMethod = "createOrderFallbackMethod4Thread"
    )
    @RequestMapping("/createOrderTest1")
    public String createOrderTest1(@RequestParam("cityId") String cityId, @RequestParam("platformId") String platformId,
                                   @RequestParam("userId") String userId, @RequestParam("supplierId") String supplierId,
                                   @RequestParam("goodsId") String goodsId) throws Exception {
        return "Hystrix下单测试1！";
    }

    public String createOrderFallbackMethod4Thread(@RequestParam("cityId") String cityId,
                                                   @RequestParam("platformId") String platformId,
                                                   @RequestParam("userId") String userId,
                                                   @RequestParam("supplierId") String supplierId,
                                                   @RequestParam("goodsId") String goodsId) throws Exception {
        System.err.println("------------执行线程池限流降级方法------------");
        return "Hystrix ThreadPool!";
    }

    //信号量限流降级：同一时间最多允许N条请求过来，超过就走降级方法
    @HystrixCommand(
            commandKey = "createOrderTest2",//自定义，一般规范是跟方法的url全路径相同
            commandProperties = {
                    @HystrixProperty(name = "execution.isolation.strategy", value = "SEMAPHORE"),//设置限流策略：信号量
                    //同一时间允许3个请求
                    @HystrixProperty(name = "execution.isolation.semaphore.maxConcurrentRequests", value = "3")
            },
            fallbackMethod = "createOrderFallbackMethod4Semaphore"
    )
    @RequestMapping("/createOrderTest2")
    public String createOrderTest2(@RequestParam("cityId") String cityId, @RequestParam("platformId") String platformId,
                                   @RequestParam("userId") String userId, @RequestParam("supplierId") String supplierId,
                                   @RequestParam("goodsId") String goodsId) throws Exception {
        return "Hystrix下单测试2！";
    }

    public String createOrderFallbackMethod4Semaphore(@RequestParam("cityId") String cityId,
                                                      @RequestParam("platformId") String platformId,
                                                      @RequestParam("userId") String userId,
                                                      @RequestParam("supplierId") String supplierId,
                                                      @RequestParam("goodsId") String goodsId) throws Exception {
        System.err.println("------------执行信号量限流降级方法------------");
        return "Hystrix Semaphore!";
    }

}

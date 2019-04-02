package com.yuan.order.config.hystrix;

import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.netflix.hystrix.contrib.javanica.aop.aspectj.HystrixCommandAspect;
import com.netflix.hystrix.contrib.metrics.eventstream.HystrixMetricsStreamServlet;

/**
 * Hystrix断路器配置：用于服务的降级
 * 常用业务场景：服务的降级；请求的限流；批量请求的合并
 */
@Configuration
public class HystrixConfig {

    //	用来拦截处理HystrixCommand注解（识别项目中关于的Hystrix的注解）
    @Bean
    public HystrixCommandAspect hystrixAspect() {
        return new HystrixCommandAspect();
    }

    //	用来像监控中心Dashboard发送stream信息
    @Bean
    public ServletRegistrationBean hystrixMetricsStreamServlet() {
        ServletRegistrationBean registration = new ServletRegistrationBean(new HystrixMetricsStreamServlet());
        registration.addUrlMappings("/hystrix.stream");
        return registration;
    }
}

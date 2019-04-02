package com.yuan.payb;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan(basePackages = "com.yuan.order.mapper")
public class PaybApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaybApplication.class, args);
    }

}

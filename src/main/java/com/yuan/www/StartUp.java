package com.yuan.www;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class StartUp {

    public static void main(String[] args) {
        System.out.println("~~~开始启动~~~");
        SpringApplication.run(StartUp.class, args);
    }
}

package com.mro.training;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableDubbo
@EnableAsync
public class VrArTrainingApplication {
    public static void main(String[] args) {
        SpringApplication.run(VrArTrainingApplication.class, args);
    }
}

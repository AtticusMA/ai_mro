package com.mro.ar;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableDubbo
@MapperScan("com.mro.ar.mapper")
public class ArMaintenanceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ArMaintenanceApplication.class, args);
    }
}

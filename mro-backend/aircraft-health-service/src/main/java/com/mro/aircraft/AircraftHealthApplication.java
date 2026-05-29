package com.mro.aircraft;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableDubbo
@EnableScheduling
@MapperScan("com.mro.aircraft.mapper")
public class AircraftHealthApplication {

    public static void main(String[] args) {
        SpringApplication.run(AircraftHealthApplication.class, args);
    }
}

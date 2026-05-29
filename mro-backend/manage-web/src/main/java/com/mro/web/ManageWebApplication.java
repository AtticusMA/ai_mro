package com.mro.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import com.mro.web.config.EncryptionProperties;

@SpringBootApplication
@EnableDiscoveryClient
@EnableConfigurationProperties(EncryptionProperties.class)
public class ManageWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(ManageWebApplication.class, args);
    }
}

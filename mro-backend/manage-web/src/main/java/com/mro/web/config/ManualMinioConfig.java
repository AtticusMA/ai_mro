package com.mro.web.config;

import io.minio.MinioClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ManualMinioConfig {

    @Bean
    @ConfigurationProperties(prefix = "minio")
    public MinioProperties manualMinioProperties() {
        return new MinioProperties();
    }

    @Bean
    public MinioClient manualMinioClient(MinioProperties manualMinioProperties) {
        return MinioClient.builder()
                .endpoint(manualMinioProperties.getEndpoint())
                .credentials(manualMinioProperties.getAccessKey(), manualMinioProperties.getSecretKey())
                .build();
    }

    @Data
    public static class MinioProperties {
        private String endpoint;
        private String accessKey;
        private String secretKey;
        private String bucketName;
    }
}

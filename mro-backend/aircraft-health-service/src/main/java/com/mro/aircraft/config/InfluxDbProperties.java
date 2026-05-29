package com.mro.aircraft.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "influxdb")
public class InfluxDbProperties {
    private String url;
    private String token;
    private String org;
    private String bucket;
}

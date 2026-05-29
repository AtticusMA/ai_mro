package com.mro.aircraft.config;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class InfluxDbConfig {

    private final InfluxDbProperties props;

    @Bean
    public InfluxDBClient influxDBClient() {
        return InfluxDBClientFactory.create(props.getUrl(), props.getToken().toCharArray(),
                props.getOrg(), props.getBucket());
    }
}

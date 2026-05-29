package com.mro.aircraft.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "mqtt")
public class MqttProperties {
    private String brokerUrl;
    private String clientId;
    private String username;
    private String password;
    private Topic topic = new Topic();

    @Data
    public static class Topic {
        private String flightData = "aircraft/+/data";
        private String qarData = "aircraft/+/qar";
    }
}

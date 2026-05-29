package com.mro.aircraft.config;

import com.mro.aircraft.pipeline.MqttMessageHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class MqttConfig {

    private final MqttProperties props;
    private final MqttMessageHandler messageHandler;

    @Bean
    public MqttClient mqttClient() throws MqttException {
        MqttClient client = new MqttClient(props.getBrokerUrl(), props.getClientId(), new MemoryPersistence());
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);
        options.setAutomaticReconnect(true);
        if (props.getUsername() != null && !props.getUsername().isBlank()) {
            options.setUserName(props.getUsername());
            options.setPassword(props.getPassword().toCharArray());
        }
        client.connect(options);
        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                log.warn("MQTT connection lost: {}", cause.getMessage());
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) {
                messageHandler.handle(topic, new String(message.getPayload()));
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
            }
        });
        client.subscribe(props.getTopic().getFlightData(), 1);
        client.subscribe(props.getTopic().getQarData(), 1);
        log.info("MQTT client connected to {} and subscribed", props.getBrokerUrl());
        return client;
    }
}

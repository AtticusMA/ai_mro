package com.mro.aircraft.pipeline;

import cn.hutool.json.JSONUtil;
import com.mro.aircraft.parser.FlightDataMessage;
import com.mro.aircraft.parser.FlightDataParserDispatcher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Receives raw MQTT messages, parses them, then forwards to Kafka topic raw-flight-data.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MqttMessageHandler {

    static final String TOPIC_RAW = "raw-flight-data";

    private final FlightDataParserDispatcher dispatcher;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public void handle(String topic, String payload) {
        try {
            FlightDataMessage msg = dispatcher.dispatch(topic, payload);
            String json = JSONUtil.toJsonStr(msg);
            kafkaTemplate.send(TOPIC_RAW, msg.aircraftId(), json);
            log.debug("Forwarded message for aircraft={} source={}", msg.aircraftId(), msg.dataSource());
        } catch (Exception e) {
            log.error("Failed to handle MQTT message from topic={}: {}", topic, e.getMessage(), e);
        }
    }
}

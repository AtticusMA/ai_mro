package com.mro.aircraft.pipeline;

import cn.hutool.json.JSONUtil;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.mro.aircraft.parser.FlightDataMessage;
import com.mro.aircraft.service.FaultDetectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Consumes raw-flight-data from Kafka, writes time-series to InfluxDB,
 * then triggers fault detection.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FlightDataConsumer {

    private final InfluxDBClient influxDBClient;
    private final FaultDetectionService faultDetectionService;

    @KafkaListener(topics = MqttMessageHandler.TOPIC_RAW, groupId = "aircraft-health-service")
    public void consume(String json) {
        try {
            FlightDataMessage msg = JSONUtil.toBean(json, FlightDataMessage.class);
            writeToInfluxDb(msg);
            faultDetectionService.analyze(msg);
        } catch (Exception e) {
            log.error("Failed to process flight data message: {}", e.getMessage(), e);
        }
    }

    private void writeToInfluxDb(FlightDataMessage msg) {
        WriteApiBlocking writeApi = influxDBClient.getWriteApiBlocking();
        Point point = Point.measurement("flight_data")
                .addTag("aircraft_id", msg.aircraftId())
                .addTag("data_source", msg.dataSource())
                .time(msg.collectedAt().toEpochMilli(), WritePrecision.MS);

        for (Map.Entry<String, Object> entry : msg.metrics().entrySet()) {
            Object value = entry.getValue();
            if (value instanceof Number n) {
                point.addField(entry.getKey(), n.doubleValue());
            } else if (value instanceof Boolean b) {
                point.addField(entry.getKey(), b);
            } else {
                point.addField(entry.getKey(), String.valueOf(value));
            }
        }

        writeApi.writePoint(point);
        log.debug("Written {} metrics to InfluxDB for aircraft={}", msg.metrics().size(), msg.aircraftId());
    }
}

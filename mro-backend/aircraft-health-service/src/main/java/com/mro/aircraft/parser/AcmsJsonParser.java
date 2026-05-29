package com.mro.aircraft.parser;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Parses ACMS JSON messages published to topic: aircraft/{aircraftId}/data
 *
 * Expected payload format:
 * {
 *   "aircraft_id": "B-1234",
 *   "ts": 1748304000000,
 *   "metrics": { "hydraulic_pressure_a": 3000.0, "engine_n1_1": 85.2, ... }
 * }
 */
@Slf4j
@Component
public class AcmsJsonParser implements FlightDataParser {

    @Override
    public boolean supports(String topic, String payload) {
        return topic.contains("/data") && JSONUtil.isTypeJSON(payload);
    }

    @Override
    public FlightDataMessage parse(String topic, String payload) {
        try {
            JSONObject json = JSONUtil.parseObj(payload);
            String aircraftId = extractAircraftId(topic, json);
            long tsMillis = json.getLong("ts", System.currentTimeMillis());
            Map<String, Object> metrics = extractMetrics(json);
            return new FlightDataMessage(aircraftId, "ACMS", Instant.ofEpochMilli(tsMillis), metrics, payload);
        } catch (Exception e) {
            log.warn("ACMS parse failed for topic={}: {}", topic, e.getMessage());
            throw new IllegalArgumentException("Invalid ACMS payload: " + e.getMessage(), e);
        }
    }

    private String extractAircraftId(String topic, JSONObject json) {
        if (json.containsKey("aircraft_id")) {
            return json.getStr("aircraft_id");
        }
        // fallback: extract from topic aircraft/{aircraftId}/data
        String[] parts = topic.split("/");
        if (parts.length >= 2) {
            return parts[1];
        }
        throw new IllegalArgumentException("Cannot determine aircraftId from topic: " + topic);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> extractMetrics(JSONObject json) {
        if (json.containsKey("metrics")) {
            Object raw = json.get("metrics");
            if (raw instanceof Map) {
                return new HashMap<>((Map<String, Object>) raw);
            }
        }
        // flat payload — treat all fields except reserved ones as metrics
        Map<String, Object> metrics = new HashMap<>(json.toBean(Map.class));
        metrics.remove("aircraft_id");
        metrics.remove("ts");
        return metrics;
    }
}

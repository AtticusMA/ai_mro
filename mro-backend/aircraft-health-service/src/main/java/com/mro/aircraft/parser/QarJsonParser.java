package com.mro.aircraft.parser;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Parses QAR JSON messages published to topic: aircraft/{aircraftId}/qar
 *
 * Expected payload format:
 * {
 *   "aircraft_id": "B-1234",
 *   "flight_no": "CA1234",
 *   "ts": 1748304000000,
 *   "parameters": { "altitude": 35000, "speed": 450, "fuel_qty": 12000, ... }
 * }
 */
@Slf4j
@Component
public class QarJsonParser implements FlightDataParser {

    @Override
    public boolean supports(String topic, String payload) {
        return topic.contains("/qar") && JSONUtil.isTypeJSON(payload);
    }

    @Override
    public FlightDataMessage parse(String topic, String payload) {
        try {
            JSONObject json = JSONUtil.parseObj(payload);
            String aircraftId = extractAircraftId(topic, json);
            long tsMillis = json.getLong("ts", System.currentTimeMillis());
            Map<String, Object> metrics = extractParameters(json);
            // prefix QAR params to distinguish from ACMS metrics
            Map<String, Object> prefixed = new HashMap<>();
            metrics.forEach((k, v) -> prefixed.put("qar." + k, v));
            return new FlightDataMessage(aircraftId, "QAR", Instant.ofEpochMilli(tsMillis), prefixed, payload);
        } catch (Exception e) {
            log.warn("QAR parse failed for topic={}: {}", topic, e.getMessage());
            throw new IllegalArgumentException("Invalid QAR payload: " + e.getMessage(), e);
        }
    }

    private String extractAircraftId(String topic, JSONObject json) {
        if (json.containsKey("aircraft_id")) {
            return json.getStr("aircraft_id");
        }
        String[] parts = topic.split("/");
        if (parts.length >= 2) {
            return parts[1];
        }
        throw new IllegalArgumentException("Cannot determine aircraftId from topic: " + topic);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> extractParameters(JSONObject json) {
        if (json.containsKey("parameters")) {
            Object raw = json.get("parameters");
            if (raw instanceof Map) {
                return new HashMap<>((Map<String, Object>) raw);
            }
        }
        Map<String, Object> params = new HashMap<>(json.toBean(Map.class));
        params.remove("aircraft_id");
        params.remove("flight_no");
        params.remove("ts");
        return params;
    }
}

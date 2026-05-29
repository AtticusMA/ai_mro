package com.mro.dtwin.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class DtwinEventPublisher {

    private static final String CHANNEL_WORKSTATION = "dtwin:workstation:change";
    private static final String CHANNEL_ORDER = "dtwin:order:progress";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public void publishWorkstationChange(Long hangarId, Long workstationId, String status, String aircraftId) {
        try {
            String payload = objectMapper.writeValueAsString(Map.of(
                    "type", "WORKSTATION_STATUS",
                    "hangarId", hangarId,
                    "workstationId", workstationId,
                    "status", status,
                    "aircraftId", aircraftId != null ? aircraftId : ""
            ));
            redisTemplate.convertAndSend(CHANNEL_WORKSTATION, payload);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize workstation event", e);
        }
    }

    public void publishOrderProgress(Long planId, Long orderId, int progress, String status) {
        try {
            String payload = objectMapper.writeValueAsString(Map.of(
                    "type", "ORDER_PROGRESS",
                    "planId", planId,
                    "orderId", orderId,
                    "progress", progress,
                    "status", status != null ? status : ""
            ));
            redisTemplate.convertAndSend(CHANNEL_ORDER, payload);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize order event", e);
        }
    }
}

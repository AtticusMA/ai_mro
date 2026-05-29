package com.mro.dtwin.mqtt;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mro.dtwin.service.WorkstationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WorkstationMqttConsumer {

    private final WorkstationService workstationService;
    private final ObjectMapper objectMapper;

    /**
     * Receives MQTT messages from the mro/workstation/status topic.
     * Expected payload: {"workstationId":101,"status":"occupied","aircraftId":"B-1234"}
     */
    @ServiceActivator(inputChannel = "mqttInputChannel")
    public void handleMessage(Message<String> message) {
        try {
            JsonNode node = objectMapper.readTree(message.getPayload());
            Long workstationId = node.get("workstationId").asLong();
            String status = node.get("status").asText();
            String aircraftId = node.has("aircraftId") ? node.get("aircraftId").asText(null) : null;
            workstationService.updateStatus(workstationId, status, aircraftId);
            log.debug("MQTT workstation update: id={} status={}", workstationId, status);
        } catch (Exception e) {
            log.error("Failed to process MQTT workstation message: {}", message.getPayload(), e);
        }
    }
}

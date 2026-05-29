package com.mro.tool.mqtt;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mro.tool.service.ToolService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CabinetMqttConsumer {

    private final ToolService toolService;
    private final ObjectMapper objectMapper;

    /**
     * Receives MQTT events from tool/cabinet/{id}/event topic.
     * Expected payload: {"cabinetId":1,"event":"closed","rfidScanResult":["RFID-001","RFID-002"]}
     */
    @ServiceActivator(inputChannel = "mqttInputChannel")
    public void handleMessage(Message<String> message) {
        try {
            JsonNode node = objectMapper.readTree(message.getPayload());
            String event = node.has("event") ? node.get("event").asText() : "";
            if ("closed".equals(event) && node.has("cabinetId")) {
                Long cabinetId = node.get("cabinetId").asLong();
                toolService.triggerInventory(cabinetId);
                log.debug("MQTT cabinet closed event: cabinetId={}", cabinetId);
            }
        } catch (Exception e) {
            log.error("Failed to process MQTT cabinet message: {}", message.getPayload(), e);
        }
    }
}

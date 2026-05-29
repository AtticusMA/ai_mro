package com.mro.aircraft.ws;

import cn.hutool.json.JSONUtil;
import com.mro.aircraft.domain.entity.HealthAlert;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class HealthAlertPushService {

    private final HealthWebSocketHandler handler;

    public void pushAlert(HealthAlert alert) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("type", "ALERT");
        payload.put("id", alert.getId());
        payload.put("aircraftId", alert.getAircraftId());
        payload.put("alertLevel", alert.getAlertLevel());
        payload.put("message", alert.getMessage());
        payload.put("acknowledged", alert.getAcknowledged());
        payload.put("createdAt", alert.getCreatedAt() != null
                ? alert.getCreatedAt().toInstant(ZoneOffset.UTC).toEpochMilli() : null);
        handler.broadcast(JSONUtil.toJsonStr(payload));
    }
}

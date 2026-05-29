package com.mro.aircraft.service;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mro.aircraft.domain.entity.AlertRule;
import com.mro.aircraft.domain.entity.HealthAlert;
import com.mro.aircraft.mapper.AlertRuleMapper;
import com.mro.aircraft.mapper.HealthAlertMapper;
import com.mro.aircraft.ws.HealthAlertPushService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Consumes fault-events from Kafka, evaluates alert rules,
 * persists HealthAlert records, and pushes via WebSocket.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AlertService {

    private final AlertRuleMapper alertRuleMapper;
    private final HealthAlertMapper healthAlertMapper;
    private final HealthAlertPushService pushService;

    @KafkaListener(topics = FaultDetectionService.TOPIC_FAULT_EVENTS, groupId = "aircraft-health-alert")
    public void onFaultEvent(String json) {
        try {
            Map<String, Object> event = JSONUtil.parseObj(json).toBean(Map.class);
            String aircraftId = (String) event.get("aircraftId");
            String severity = (String) event.get("severity");
            String alertLevel = mapSeverityToAlertLevel(severity);
            String message = buildAlertMessage(event);
            createAlert(aircraftId, alertLevel, message);
        } catch (Exception e) {
            log.error("Failed to process fault event: {}", e.getMessage(), e);
        }
    }

    public void evaluateMetricAlert(String aircraftId, String aircraftType,
                                     String metricName, double metricValue) {
        List<AlertRule> rules = alertRuleMapper.selectList(
                new LambdaQueryWrapper<AlertRule>()
                        .eq(AlertRule::getEnabled, true)
                        .eq(AlertRule::getMetricName, metricName)
                        .and(w -> w.isNull(AlertRule::getAircraftType)
                                .or().eq(AlertRule::getAircraftType, aircraftType)));

        for (AlertRule rule : rules) {
            if (matches(rule, metricValue)) {
                String msg = String.format("指标 %s 触发规则 [%s]: 当前值=%.2f, 阈值=%.2f",
                        metricName, rule.getRuleName(), metricValue, rule.getThreshold());
                createAlert(aircraftId, rule.getAlertLevel(), msg);
            }
        }
    }

    private boolean matches(AlertRule rule, double value) {
        return switch (rule.getOperator()) {
            case "lt"  -> value < rule.getThreshold();
            case "lte" -> value <= rule.getThreshold();
            case "gt"  -> value > rule.getThreshold();
            case "gte" -> value >= rule.getThreshold();
            case "eq"  -> Math.abs(value - rule.getThreshold()) < 1e-9;
            default    -> false;
        };
    }

    private void createAlert(String aircraftId, String alertLevel, String message) {
        HealthAlert alert = new HealthAlert();
        alert.setAircraftId(aircraftId);
        alert.setAlertLevel(alertLevel);
        alert.setMessage(message);
        alert.setAcknowledged(false);
        alert.setCreatedAt(LocalDateTime.now());
        healthAlertMapper.insert(alert);
        pushService.pushAlert(alert);
        log.info("Alert created: aircraft={} level={}", aircraftId, alertLevel);
    }

    private String mapSeverityToAlertLevel(String severity) {
        return switch (severity) {
            case "critical" -> "red";
            case "major"    -> "orange";
            default         -> "yellow";
        };
    }

    private String buildAlertMessage(Map<String, Object> event) {
        return String.format("检测到故障: 部件=%s, 故障码=%s, 严重程度=%s",
                event.get("component"), event.get("faultCode"), event.get("severity"));
    }
}

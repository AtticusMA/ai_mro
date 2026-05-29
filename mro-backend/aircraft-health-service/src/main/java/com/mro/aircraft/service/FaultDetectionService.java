package com.mro.aircraft.service;

import com.mro.aircraft.domain.entity.FaultRecord;
import com.mro.aircraft.engine.FaultRule;
import com.mro.aircraft.engine.FaultRuleResult;
import com.mro.aircraft.mapper.FaultRecordMapper;
import com.mro.aircraft.parser.FlightDataMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FaultDetectionService {

    static final String TOPIC_FAULT_EVENTS = "fault-events";

    private final List<FaultRule> rules;
    private final FaultRecordMapper faultRecordMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public void analyze(FlightDataMessage message) {
        for (FaultRule rule : rules) {
            try {
                FaultRuleResult result = rule.evaluate(message);
                if (result != null) {
                    FaultRecord record = buildRecord(message, result);
                    faultRecordMapper.insert(record);
                    publishFaultEvent(record);
                    log.info("Fault detected: aircraft={} code={} severity={}",
                            message.aircraftId(), result.faultCode(), result.severity());
                }
            } catch (Exception e) {
                log.error("Rule {} failed for aircraft={}: {}",
                        rule.getClass().getSimpleName(), message.aircraftId(), e.getMessage(), e);
            }
        }
    }

    private FaultRecord buildRecord(FlightDataMessage message, FaultRuleResult result) {
        FaultRecord record = new FaultRecord();
        record.setAircraftId(message.aircraftId());
        record.setFaultCode(result.faultCode());
        record.setSeverity(result.severity());
        record.setComponent(result.component());
        record.setDetectedAt(LocalDateTime.ofInstant(message.collectedAt(), ZoneOffset.UTC));
        record.setStatus("open");
        record.setRawData(message.rawPayload());
        record.setCreatedAt(LocalDateTime.now());
        record.setUpdatedAt(LocalDateTime.now());
        return record;
    }

    private void publishFaultEvent(FaultRecord record) {
        String payload = String.format(
                "{\"aircraftId\":\"%s\",\"faultCode\":\"%s\",\"severity\":\"%s\",\"component\":\"%s\",\"faultRecordId\":%d}",
                record.getAircraftId(), record.getFaultCode(), record.getSeverity(),
                record.getComponent(), record.getId());
        kafkaTemplate.send(TOPIC_FAULT_EVENTS, record.getAircraftId(), payload);
    }
}

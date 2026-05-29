package com.mro.aircraft.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.QueryApi;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import com.mro.aircraft.config.InfluxDbProperties;
import com.mro.aircraft.domain.entity.FaultRecord;
import com.mro.aircraft.domain.entity.PredictionReport;
import com.mro.aircraft.mapper.FaultRecordMapper;
import com.mro.aircraft.mapper.PredictionReportMapper;
import com.mro.common.dubbo.health.request.HealthStatQueryParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

/**
 * Rule-engine-based prediction service.
 * Runs hourly, queries recent fault history from InfluxDB,
 * and generates prediction reports using trend rules.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PredictionService {

    private static final String MODEL_VERSION = "rule-engine-v1.0";

    private final InfluxDBClient influxDBClient;
    private final InfluxDbProperties influxDbProps;
    private final FaultRecordMapper faultRecordMapper;
    private final PredictionReportMapper predictionReportMapper;

    @Scheduled(cron = "0 0 * * * *")
    public void generatePredictions() {
        log.info("Starting hourly prediction run");
        Set<String> aircraftIds = getActiveAircraftIds();
        for (String aircraftId : aircraftIds) {
            try {
                generateForAircraft(aircraftId);
            } catch (Exception e) {
                log.error("Prediction failed for aircraft={}: {}", aircraftId, e.getMessage(), e);
            }
        }
    }

    private Set<String> getActiveAircraftIds() {
        // Aircraft that had data in last 24 hours
        String flux = String.format(
                "from(bucket:\"%s\") |> range(start: -24h) |> filter(fn: (r) => r._measurement == \"flight_data\") |> group(columns: [\"aircraft_id\"]) |> distinct(column: \"aircraft_id\")",
                influxDbProps.getBucket());
        Set<String> ids = new LinkedHashSet<>();
        try {
            QueryApi queryApi = influxDBClient.getQueryApi();
            List<FluxTable> tables = queryApi.query(flux, influxDbProps.getOrg());
            for (FluxTable table : tables) {
                for (FluxRecord record : table.getRecords()) {
                    Object val = record.getValueByKey("aircraft_id");
                    if (val != null) ids.add(val.toString());
                }
            }
        } catch (Exception e) {
            log.warn("InfluxDB query for active aircraft failed: {}", e.getMessage());
        }
        return ids;
    }

    private void generateForAircraft(String aircraftId) {
        // Rule: count open faults in last 7 days to determine fault probability
        long openFaultCount = faultRecordMapper.selectCount(
                new LambdaQueryWrapper<FaultRecord>()
                        .eq(FaultRecord::getAircraftId, aircraftId)
                        .eq(FaultRecord::getStatus, "open")
                        .ge(FaultRecord::getDetectedAt, LocalDateTime.now().minusDays(7)));

        long criticalCount = faultRecordMapper.selectCount(
                new LambdaQueryWrapper<FaultRecord>()
                        .eq(FaultRecord::getAircraftId, aircraftId)
                        .eq(FaultRecord::getSeverity, "critical")
                        .eq(FaultRecord::getStatus, "open"));

        double faultProbability = Math.min(1.0, openFaultCount * 0.1 + criticalCount * 0.3);
        double confidence = openFaultCount > 0 ? 0.75 : 0.5;

        // Predict fault time: sooner if more/critical faults
        long daysUntilFault = criticalCount > 0 ? 2 : (openFaultCount > 3 ? 5 : 14);
        LocalDateTime predictedFaultTime = LocalDateTime.now().plusDays(daysUntilFault);

        String faultLocation = determineFaultLocation(aircraftId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("faultProbability", faultProbability);
        result.put("predictedFaultTime", predictedFaultTime.toInstant(ZoneOffset.UTC).toString());
        result.put("faultLocation", faultLocation);
        result.put("confidence", confidence);
        result.put("openFaultCount", openFaultCount);
        result.put("criticalFaultCount", criticalCount);

        PredictionReport report = new PredictionReport();
        report.setAircraftId(aircraftId);
        report.setModelVersion(MODEL_VERSION);
        report.setPredictedAt(LocalDateTime.now());
        report.setResult(result);
        report.setCreatedAt(LocalDateTime.now());
        predictionReportMapper.insert(report);

        log.info("Prediction generated: aircraft={} probability={} predictedFaultTime={}",
                aircraftId, faultProbability, predictedFaultTime);
    }

    private String determineFaultLocation(String aircraftId) {
        // Return the component with most open faults as the predicted fault location
        List<FaultRecord> openFaults = faultRecordMapper.selectList(
                new LambdaQueryWrapper<FaultRecord>()
                        .eq(FaultRecord::getAircraftId, aircraftId)
                        .eq(FaultRecord::getStatus, "open")
                        .orderByDesc(FaultRecord::getDetectedAt)
                        .last("LIMIT 10"));

        return openFaults.stream()
                .map(FaultRecord::getComponent)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse("未知");
    }
}

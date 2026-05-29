package com.mro.aircraft.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mro.aircraft.domain.entity.*;
import com.mro.aircraft.mapper.*;
import com.mro.common.core.exception.BizException;
import com.mro.common.core.response.PageResult;
import com.mro.common.dubbo.common.request.*;
import com.mro.common.dubbo.health.request.*;
import com.mro.common.dubbo.health.response.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HealthService {

    private final FaultRecordMapper faultRecordMapper;
    private final HealthAlertMapper healthAlertMapper;
    private final PredictionReportMapper predictionReportMapper;
    private final AlertRuleMapper alertRuleMapper;

    // ---- Fleet health ----

    public PageResult<AircraftHealthDTO> listAircraftHealth(HealthQueryParam param) {
        List<String> allIds = getDistinctAircraftIds();
        int total = allIds.size();
        int from = (param.pageNum() - 1) * param.pageSize();
        int to = Math.min(from + param.pageSize(), total);
        List<String> pageIds = from < total ? allIds.subList(from, to) : List.of();

        List<AircraftHealthDTO> list = pageIds.stream()
                .map(id -> buildAircraftHealthDTO(id, param))
                .collect(Collectors.toList());

        return PageResult.of(list, total, param.pageNum(), param.pageSize());
    }

    private List<String> getDistinctAircraftIds() {
        return faultRecordMapper.selectList(
                        new LambdaQueryWrapper<FaultRecord>()
                                .select(FaultRecord::getAircraftId)
                                .groupBy(FaultRecord::getAircraftId))
                .stream().map(FaultRecord::getAircraftId).distinct().collect(Collectors.toList());
    }

    private AircraftHealthDTO buildAircraftHealthDTO(String aircraftId, HealthQueryParam param) {
        long activeFaults = faultRecordMapper.selectCount(
                new LambdaQueryWrapper<FaultRecord>()
                        .eq(FaultRecord::getAircraftId, aircraftId)
                        .eq(FaultRecord::getStatus, "open")
                        .eq(StringUtils.hasText(param.severity()), FaultRecord::getSeverity, param.severity()));

        long activeAlerts = healthAlertMapper.selectCount(
                new LambdaQueryWrapper<HealthAlert>()
                        .eq(HealthAlert::getAircraftId, aircraftId)
                        .eq(HealthAlert::getAcknowledged, false));

        String overallHealth = computeOverallHealth(aircraftId);

        FaultRecord latest = faultRecordMapper.selectOne(
                new LambdaQueryWrapper<FaultRecord>()
                        .eq(FaultRecord::getAircraftId, aircraftId)
                        .orderByDesc(FaultRecord::getDetectedAt)
                        .last("LIMIT 1"));

        return new AircraftHealthDTO(
                aircraftId, aircraftId, "UNKNOWN",
                overallHealth, (int) activeAlerts, (int) activeFaults,
                latest != null ? latest.getDetectedAt().toInstant(ZoneOffset.UTC) : null);
    }

    private String computeOverallHealth(String aircraftId) {
        boolean hasCritical = faultRecordMapper.selectCount(
                new LambdaQueryWrapper<FaultRecord>()
                        .eq(FaultRecord::getAircraftId, aircraftId)
                        .eq(FaultRecord::getStatus, "open")
                        .eq(FaultRecord::getSeverity, "critical")) > 0;
        if (hasCritical) return "critical";
        boolean hasMajor = faultRecordMapper.selectCount(
                new LambdaQueryWrapper<FaultRecord>()
                        .eq(FaultRecord::getAircraftId, aircraftId)
                        .eq(FaultRecord::getStatus, "open")
                        .eq(FaultRecord::getSeverity, "major")) > 0;
        return hasMajor ? "warning" : "healthy";
    }

    public AircraftDetailDTO getAircraftDetail(String aircraftId) {
        List<FaultRecord> faults = faultRecordMapper.selectList(
                new LambdaQueryWrapper<FaultRecord>()
                        .eq(FaultRecord::getAircraftId, aircraftId)
                        .eq(FaultRecord::getStatus, "open")
                        .orderByDesc(FaultRecord::getDetectedAt)
                        .last("LIMIT 20"));

        List<HealthAlert> alerts = healthAlertMapper.selectList(
                new LambdaQueryWrapper<HealthAlert>()
                        .eq(HealthAlert::getAircraftId, aircraftId)
                        .eq(HealthAlert::getAcknowledged, false)
                        .orderByDesc(HealthAlert::getCreatedAt)
                        .last("LIMIT 10"));

        String overallHealth = computeOverallHealth(aircraftId);
        LocalDateTime latestData = faults.isEmpty() ? null : faults.get(0).getDetectedAt();

        return new AircraftDetailDTO(
                aircraftId, "UNKNOWN", overallHealth,
                latestData != null ? latestData.toInstant(ZoneOffset.UTC) : null,
                faults.stream().map(this::toFaultRecordDTO).collect(Collectors.toList()),
                alerts.stream().map(this::toHealthAlertDTO).collect(Collectors.toList()));
    }

    // ---- Faults ----

    public PageResult<FaultRecordDTO> listFaults(String aircraftId, FaultQueryParam param) {
        Page<FaultRecord> page = new Page<>(param.pageNum(), param.pageSize());
        LambdaQueryWrapper<FaultRecord> wrapper = new LambdaQueryWrapper<FaultRecord>()
                .eq(FaultRecord::getAircraftId, aircraftId)
                .eq(StringUtils.hasText(param.severity()), FaultRecord::getSeverity, param.severity())
                .eq(StringUtils.hasText(param.status()), FaultRecord::getStatus, param.status())
                .orderByDesc(FaultRecord::getDetectedAt);
        Page<FaultRecord> result = faultRecordMapper.selectPage(page, wrapper);
        List<FaultRecordDTO> list = result.getRecords().stream()
                .map(this::toFaultRecordDTO).collect(Collectors.toList());
        return PageResult.of(list, result.getTotal(), param.pageNum(), param.pageSize());
    }

    // ---- Predictions ----

    public PageResult<PredictionReportDTO> listPredictions(String aircraftId, HealthPageParam param) {
        Page<PredictionReport> page = new Page<>(param.pageNum(), param.pageSize());
        Page<PredictionReport> result = predictionReportMapper.selectPage(page,
                new LambdaQueryWrapper<PredictionReport>()
                        .eq(PredictionReport::getAircraftId, aircraftId)
                        .orderByDesc(PredictionReport::getPredictedAt));
        List<PredictionReportDTO> list = result.getRecords().stream()
                .map(this::toPredictionReportDTO).collect(Collectors.toList());
        return PageResult.of(list, result.getTotal(), param.pageNum(), param.pageSize());
    }

    // ---- Alerts ----

    public PageResult<HealthAlertDTO> listAlerts(AlertQueryParam param) {
        Page<HealthAlert> page = new Page<>(param.pageNum(), param.pageSize());
        LambdaQueryWrapper<HealthAlert> wrapper = new LambdaQueryWrapper<HealthAlert>()
                .eq(StringUtils.hasText(param.alertLevel()), HealthAlert::getAlertLevel, param.alertLevel())
                .eq(param.acknowledged() != null, HealthAlert::getAcknowledged, param.acknowledged())
                .eq(StringUtils.hasText(param.aircraftId()), HealthAlert::getAircraftId, param.aircraftId())
                .orderByDesc(HealthAlert::getCreatedAt);
        Page<HealthAlert> result = healthAlertMapper.selectPage(page, wrapper);
        List<HealthAlertDTO> list = result.getRecords().stream()
                .map(this::toHealthAlertDTO).collect(Collectors.toList());
        return PageResult.of(list, result.getTotal(), param.pageNum(), param.pageSize());
    }

    public void acknowledgeAlert(Long alertId, Long operatorId) {
        HealthAlert alert = healthAlertMapper.selectById(alertId);
        if (alert == null) throw new BizException(4200, "预警不存在");
        if (Boolean.TRUE.equals(alert.getAcknowledged())) throw new BizException(4202, "预警已确认，不可重复操作");
        alert.setAcknowledged(true);
        alert.setAcknowledgedBy(operatorId);
        alert.setAcknowledgedAt(LocalDateTime.now());
        healthAlertMapper.updateById(alert);
    }

    // ---- Alert Rules ----

    public Long createAlertRule(CreateAlertRuleCommand cmd) {
        if (cmd.threshold() == null || cmd.threshold().isNaN()) throw new BizException(4203, "预警规则阈值配置无效");
        AlertRule rule = new AlertRule();
        rule.setRuleName(cmd.ruleName());
        rule.setAircraftType(cmd.aircraftType());
        rule.setMetricName(cmd.metricName());
        rule.setOperator(cmd.operator());
        rule.setThreshold(cmd.threshold());
        rule.setAlertLevel(cmd.alertLevel());
        rule.setNotifyUserIds(cmd.notifyUserIds());
        rule.setEnabled(true);
        rule.setCreatedAt(LocalDateTime.now());
        rule.setUpdatedAt(LocalDateTime.now());
        alertRuleMapper.insert(rule);
        return rule.getId();
    }

    public void updateAlertRule(UpdateAlertRuleCommand cmd) {
        AlertRule rule = alertRuleMapper.selectById(cmd.id());
        if (rule == null) throw new BizException(4201, "预警规则不存在");
        if (cmd.ruleName() != null) rule.setRuleName(cmd.ruleName());
        if (cmd.aircraftType() != null) rule.setAircraftType(cmd.aircraftType());
        if (cmd.metricName() != null) rule.setMetricName(cmd.metricName());
        if (cmd.operator() != null) rule.setOperator(cmd.operator());
        if (cmd.threshold() != null) rule.setThreshold(cmd.threshold());
        if (cmd.alertLevel() != null) rule.setAlertLevel(cmd.alertLevel());
        if (cmd.notifyUserIds() != null) rule.setNotifyUserIds(cmd.notifyUserIds());
        if (cmd.enabled() != null) rule.setEnabled(cmd.enabled());
        rule.setUpdatedAt(LocalDateTime.now());
        alertRuleMapper.updateById(rule);
    }

    public void deleteAlertRule(Long ruleId) {
        if (alertRuleMapper.selectById(ruleId) == null) throw new BizException(4201, "预警规则不存在");
        alertRuleMapper.deleteById(ruleId);
    }

    // ---- Statistics ----

    public HealthStatisticsDTO getStatistics(HealthStatQueryParam param) {
        long totalFaults = faultRecordMapper.selectCount(
                new LambdaQueryWrapper<FaultRecord>()
                        .ge(param.startDate() != null, FaultRecord::getDetectedAt,
                                param.startDate() != null ? param.startDate().atStartOfDay() : null)
                        .le(param.endDate() != null, FaultRecord::getDetectedAt,
                                param.endDate() != null ? param.endDate().atTime(23, 59, 59) : null));
        long openFaults = faultRecordMapper.selectCount(
                new LambdaQueryWrapper<FaultRecord>().eq(FaultRecord::getStatus, "open"));
        long resolvedFaults = faultRecordMapper.selectCount(
                new LambdaQueryWrapper<FaultRecord>().eq(FaultRecord::getStatus, "resolved"));
        long totalAlerts = healthAlertMapper.selectCount(null);
        long unacknowledgedAlerts = healthAlertMapper.selectCount(
                new LambdaQueryWrapper<HealthAlert>().eq(HealthAlert::getAcknowledged, false));

        return new HealthStatisticsDTO(totalFaults, openFaults, resolvedFaults,
                totalAlerts, unacknowledgedAlerts, Map.of(), Map.of(), Map.of());
    }

    // ---- Converters ----

    private FaultRecordDTO toFaultRecordDTO(FaultRecord r) {
        return new FaultRecordDTO(r.getId(), r.getFaultCode(), r.getSeverity(),
                r.getComponent(),
                r.getDetectedAt() != null ? r.getDetectedAt().toInstant(ZoneOffset.UTC) : null,
                r.getStatus());
    }

    private HealthAlertDTO toHealthAlertDTO(HealthAlert a) {
        return new HealthAlertDTO(a.getId(), a.getAircraftId(), a.getAlertLevel(), a.getMessage(),
                a.getPredictedFaultTime() != null ? a.getPredictedFaultTime().toInstant(ZoneOffset.UTC) : null,
                Boolean.TRUE.equals(a.getAcknowledged()),
                a.getCreatedAt() != null ? a.getCreatedAt().toInstant(ZoneOffset.UTC) : null);
    }

    private PredictionReportDTO toPredictionReportDTO(PredictionReport r) {
        return new PredictionReportDTO(r.getId(), r.getAircraftId(), r.getModelVersion(),
                r.getPredictedAt() != null ? r.getPredictedAt().toInstant(ZoneOffset.UTC) : null,
                r.getResult());
    }
}

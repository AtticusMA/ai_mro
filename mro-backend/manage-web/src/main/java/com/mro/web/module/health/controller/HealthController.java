package com.mro.web.module.health.controller;

import com.mro.common.core.response.PageResult;
import com.mro.common.core.response.R;
import com.mro.common.dubbo.common.request.*;
import com.mro.common.dubbo.health.request.*;
import com.mro.common.dubbo.health.response.*;
import com.mro.web.module.health.app.HealthAppService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/health")
@Validated
@RequiredArgsConstructor
public class HealthController {

    private final HealthAppService healthAppService;

    // ---- Fleet health ----

    @GetMapping("/aircraft")
    public R<PageResult<AircraftHealthDTO>> listAircraftHealth(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String severity,
            @RequestParam(required = false) String aircraftType) {
        return R.ok(healthAppService.listAircraftHealth(
                new HealthQueryParam(pageNum, pageSize, status, severity, aircraftType)));
    }

    @GetMapping("/aircraft/{id}")
    public R<AircraftDetailDTO> getAircraftDetail(@PathVariable String id) {
        return R.ok(healthAppService.getAircraftDetail(id));
    }

    // ---- Faults ----

    @GetMapping("/aircraft/{id}/faults")
    public R<PageResult<FaultRecordDTO>> listFaults(
            @PathVariable String id,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String severity,
            @RequestParam(required = false) String status) {
        return R.ok(healthAppService.listFaults(id, new FaultQueryParam(pageNum, pageSize, severity, status)));
    }

    // ---- Predictions ----

    @GetMapping("/aircraft/{id}/predictions")
    public R<PageResult<PredictionReportDTO>> listPredictions(
            @PathVariable String id,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return R.ok(healthAppService.listPredictions(id, pageNum, pageSize));
    }

    // ---- Alerts ----

    @GetMapping("/alerts")
    public R<PageResult<HealthAlertDTO>> listAlerts(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String alertLevel,
            @RequestParam(required = false) Boolean acknowledged,
            @RequestParam(required = false) String aircraftId) {
        return R.ok(healthAppService.listAlerts(
                new AlertQueryParam(pageNum, pageSize, alertLevel, acknowledged, aircraftId)));
    }

    @PutMapping("/alerts/{id}/acknowledge")
    public R<Void> acknowledgeAlert(@PathVariable Long id) {
        healthAppService.acknowledgeAlert(id);
        return R.ok();
    }

    // ---- Alert Rules ----

    @PostMapping("/alert-rules")
    public R<Long> createAlertRule(@RequestBody CreateAlertRuleCommand cmd) {
        return R.ok(healthAppService.createAlertRule(cmd));
    }

    @PutMapping("/alert-rules/{id}")
    public R<Void> updateAlertRule(@PathVariable Long id, @RequestBody UpdateAlertRuleCommand cmd) {
        // ensure id consistency
        healthAppService.updateAlertRule(new UpdateAlertRuleCommand(
                id, cmd.ruleName(), cmd.aircraftType(), cmd.metricName(),
                cmd.operator(), cmd.threshold(), cmd.alertLevel(),
                cmd.notifyUserIds(), cmd.enabled()));
        return R.ok();
    }

    @DeleteMapping("/alert-rules/{id}")
    public R<Void> deleteAlertRule(@PathVariable Long id) {
        healthAppService.deleteAlertRule(id);
        return R.ok();
    }

    // ---- Statistics ----

    @GetMapping("/statistics")
    public R<HealthStatisticsDTO> getStatistics(
            @RequestParam(required = false) String aircraftType,
            @RequestParam(required = false) String component,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {
        return R.ok(healthAppService.getStatistics(
                new HealthStatQueryParam(aircraftType, component, startDate, endDate)));
    }

    // ---- Export ----

    @GetMapping("/export/faults")
    public ResponseEntity<byte[]> exportFaults(@RequestParam(required = false) String aircraftId) throws Exception {
        byte[] data = healthAppService.exportFaults(aircraftId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=fault-records.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(data);
    }

    @GetMapping("/export/alerts")
    public ResponseEntity<byte[]> exportAlerts(@RequestParam(required = false) String aircraftId) throws Exception {
        byte[] data = healthAppService.exportAlerts(aircraftId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=health-alerts.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(data);
    }
}

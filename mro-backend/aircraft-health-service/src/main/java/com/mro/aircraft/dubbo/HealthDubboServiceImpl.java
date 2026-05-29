package com.mro.aircraft.dubbo;

import com.mro.aircraft.service.HealthService;
import com.mro.common.core.response.PageResult;
import com.mro.common.dubbo.common.request.*;
import com.mro.common.dubbo.health.request.*;
import com.mro.common.dubbo.health.response.*;
import com.mro.common.dubbo.health.request.*;
import com.mro.common.dubbo.health.response.*;
import com.mro.common.dubbo.health.service.HealthDubboService;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;

@DubboService(version = "1.0.0")
@RequiredArgsConstructor
public class HealthDubboServiceImpl implements HealthDubboService {

    private final HealthService healthService;

    @Override
    public PageResult<AircraftHealthDTO> listAircraftHealth(HealthQueryParam param, UserContextDTO ctx) {
        return healthService.listAircraftHealth(param);
    }

    @Override
    public AircraftDetailDTO getAircraftDetail(String aircraftId) {
        return healthService.getAircraftDetail(aircraftId);
    }

    @Override
    public PageResult<FaultRecordDTO> listFaults(String aircraftId, FaultQueryParam param) {
        return healthService.listFaults(aircraftId, param);
    }

    @Override
    public PageResult<PredictionReportDTO> listPredictions(String aircraftId, HealthPageParam param) {
        return healthService.listPredictions(aircraftId, param);
    }

    @Override
    public PageResult<HealthAlertDTO> listAlerts(AlertQueryParam param, UserContextDTO ctx) {
        return healthService.listAlerts(param);
    }

    @Override
    public void acknowledgeAlert(Long alertId, Long operatorId) {
        healthService.acknowledgeAlert(alertId, operatorId);
    }

    @Override
    public Long createAlertRule(CreateAlertRuleCommand cmd) {
        return healthService.createAlertRule(cmd);
    }

    @Override
    public void updateAlertRule(UpdateAlertRuleCommand cmd) {
        healthService.updateAlertRule(cmd);
    }

    @Override
    public void deleteAlertRule(Long ruleId) {
        healthService.deleteAlertRule(ruleId);
    }

    @Override
    public HealthStatisticsDTO getStatistics(HealthStatQueryParam param) {
        return healthService.getStatistics(param);
    }
}

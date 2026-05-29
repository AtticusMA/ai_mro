package com.mro.web.module.health.app;

import com.mro.common.core.response.PageResult;
import com.mro.common.dubbo.common.request.*;
import com.mro.common.dubbo.health.request.*;
import com.mro.common.dubbo.health.response.*;
import com.mro.common.dubbo.health.service.HealthDubboService;
import com.mro.web.context.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class HealthAppService {

    private final HealthExportAppService exportAppService;

    @DubboReference(version = "1.0.0", timeout = 5000, retries = 0)
    private HealthDubboService healthDubboService;

    public PageResult<AircraftHealthDTO> listAircraftHealth(HealthQueryParam param) {
        return healthDubboService.listAircraftHealth(param, buildCtx());
    }

    public AircraftDetailDTO getAircraftDetail(String aircraftId) {
        return healthDubboService.getAircraftDetail(aircraftId);
    }

    public PageResult<FaultRecordDTO> listFaults(String aircraftId, FaultQueryParam param) {
        return healthDubboService.listFaults(aircraftId, param);
    }

    public PageResult<PredictionReportDTO> listPredictions(String aircraftId, int pageNum, int pageSize) {
        return healthDubboService.listPredictions(aircraftId, new HealthPageParam(pageNum, pageSize));
    }

    public PageResult<HealthAlertDTO> listAlerts(AlertQueryParam param) {
        return healthDubboService.listAlerts(param, buildCtx());
    }

    public void acknowledgeAlert(Long alertId) {
        healthDubboService.acknowledgeAlert(alertId, UserContext.getUserId());
    }

    public Long createAlertRule(CreateAlertRuleCommand cmd) {
        return healthDubboService.createAlertRule(cmd);
    }

    public void updateAlertRule(UpdateAlertRuleCommand cmd) {
        healthDubboService.updateAlertRule(cmd);
    }

    public void deleteAlertRule(Long ruleId) {
        healthDubboService.deleteAlertRule(ruleId);
    }

    public HealthStatisticsDTO getStatistics(HealthStatQueryParam param) {
        return healthDubboService.getStatistics(param);
    }

    public byte[] exportFaults(String aircraftId) throws IOException {
        return exportAppService.exportFaults(aircraftId);
    }

    public byte[] exportAlerts(String aircraftId) throws IOException {
        return exportAppService.exportAlerts(aircraftId);
    }

    private UserContextDTO buildCtx() {
        return new UserContextDTO(UserContext.getUserId(), UserContext.getDeptId(),
                UserContext.getRoles(), UserContext.getPermissions());
    }
}

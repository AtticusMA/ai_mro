package com.mro.common.dubbo.health.service;

import com.mro.common.core.response.PageResult;
import com.mro.common.dubbo.common.request.HealthPageParam;
import com.mro.common.dubbo.common.request.UserContextDTO;
import com.mro.common.dubbo.health.request.*;
import com.mro.common.dubbo.health.response.*;

public interface HealthDubboService {

    PageResult<AircraftHealthDTO> listAircraftHealth(HealthQueryParam param, UserContextDTO ctx);

    AircraftDetailDTO getAircraftDetail(String aircraftId);

    PageResult<FaultRecordDTO> listFaults(String aircraftId, FaultQueryParam param);

    PageResult<PredictionReportDTO> listPredictions(String aircraftId, HealthPageParam param);

    PageResult<HealthAlertDTO> listAlerts(AlertQueryParam param, UserContextDTO ctx);

    void acknowledgeAlert(Long alertId, Long operatorId);

    Long createAlertRule(CreateAlertRuleCommand cmd);

    void updateAlertRule(UpdateAlertRuleCommand cmd);

    void deleteAlertRule(Long ruleId);

    HealthStatisticsDTO getStatistics(HealthStatQueryParam param);
}

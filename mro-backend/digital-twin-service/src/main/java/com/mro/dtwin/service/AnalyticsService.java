package com.mro.dtwin.service;

import com.mro.common.core.response.PageResult;
import com.mro.common.dubbo.dtwin.request.*;
import com.mro.common.dubbo.dtwin.response.*;
import com.mro.dtwin.mapper.MaintenanceOrderMapper;
import com.mro.dtwin.mapper.WorkstationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final WorkstationMapper workstationMapper;
    private final MaintenanceOrderMapper orderMapper;

    public WorkloadAnalyticsDTO analyzeWorkload(AnalyticsParam param) {
        List<WorkstationLoadDTO> loads = workstationMapper.selectWorkstationLoad(
                param.hangarId(), param.startDate(), param.endDate());
        double avg = loads.isEmpty() ? 0.0
                : loads.stream().mapToDouble(WorkstationLoadDTO::utilizationRate).average().orElse(0.0);
        return new WorkloadAnalyticsDTO(loads, avg);
    }

    public EfficiencyAnalyticsDTO analyzeEfficiency(AnalyticsParam param) {
        double avgCompletionDays = orderMapper.selectAvgCompletionDays(
                param.hangarId(), param.startDate(), param.endDate());
        long totalCompleted = orderMapper.countCompleted(
                param.hangarId(), param.startDate(), param.endDate());
        long totalOrders = orderMapper.countTotal(
                param.hangarId(), param.startDate(), param.endDate());
        double completionRate = totalOrders == 0 ? 0.0 : (double) totalCompleted / totalOrders;
        return new EfficiencyAnalyticsDTO(avgCompletionDays, completionRate, totalCompleted, totalOrders);
    }
}

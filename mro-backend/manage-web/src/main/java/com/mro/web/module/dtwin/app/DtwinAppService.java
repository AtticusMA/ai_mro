package com.mro.web.module.dtwin.app;

import com.mro.common.core.response.PageResult;
import com.mro.common.dubbo.dtwin.request.*;
import com.mro.common.dubbo.dtwin.response.*;
import com.mro.common.dubbo.dtwin.service.HangarDubboService;
import com.mro.common.dubbo.dtwin.service.ProductionPlanDubboService;
import com.mro.web.context.UserContext;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * 数字孪生应用服务（机库 / 计划 / 工单 / 分析）
 * Refs: MRO-005
 */
@Service
@RequiredArgsConstructor
public class DtwinAppService {

    @DubboReference(version = "1.0.0")
    private HangarDubboService hangarDubboService;

    @DubboReference(version = "1.0.0")
    private ProductionPlanDubboService productionPlanDubboService;

    public PageResult<HangarDTO> listHangars() {
        return hangarDubboService.listHangars(UserContext.get());
    }

    public HangarModelDTO getHangarModel(Long hangarId) {
        return hangarDubboService.getHangarModel(hangarId);
    }

    public PageResult<WorkstationDTO> listWorkstations(Long hangarId, int pageNum, int pageSize) {
        return hangarDubboService.listWorkstations(hangarId, new PageParam(pageNum, pageSize));
    }

    public PageResult<ProductionPlanDTO> listPlans(Long hangarId, String status, String planType,
                                                    int pageNum, int pageSize) {
        return productionPlanDubboService.listPlans(
                new PlanQueryParam(hangarId, status, planType, pageNum, pageSize),
                UserContext.get());
    }

    public Long createPlan(CreatePlanCommand cmd) {
        return productionPlanDubboService.createPlan(cmd);
    }

    public void updatePlan(UpdatePlanCommand cmd) {
        productionPlanDubboService.updatePlan(cmd);
    }

    public PageResult<MaintenanceOrderDTO> listOrders(Long planId, Long workstationId,
                                                       Long assigneeId, String status,
                                                       int pageNum, int pageSize) {
        return productionPlanDubboService.listOrders(
                new OrderQueryParam(planId, workstationId, assigneeId, status, pageNum, pageSize),
                UserContext.get());
    }

    public Long createOrder(CreateOrderCommand cmd) {
        return productionPlanDubboService.createOrder(cmd);
    }

    public void updateOrderProgress(Long orderId, int progress, String status) {
        productionPlanDubboService.updateOrderProgress(orderId, progress, status, UserContext.getUserId());
    }

    public WorkloadAnalyticsDTO analyzeWorkload(Long hangarId, LocalDate startDate, LocalDate endDate) {
        return hangarDubboService.analyzeWorkload(new AnalyticsParam(hangarId, startDate, endDate));
    }

    public EfficiencyAnalyticsDTO analyzeEfficiency(Long hangarId, LocalDate startDate, LocalDate endDate) {
        return hangarDubboService.analyzeEfficiency(new AnalyticsParam(hangarId, startDate, endDate));
    }
}

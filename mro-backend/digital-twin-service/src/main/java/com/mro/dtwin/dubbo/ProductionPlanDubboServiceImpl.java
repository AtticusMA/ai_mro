package com.mro.dtwin.dubbo;

import com.mro.common.core.response.PageResult;
import com.mro.common.dubbo.common.request.UserContextDTO;
import com.mro.common.dubbo.dtwin.request.CreateOrderCommand;
import com.mro.common.dubbo.dtwin.request.CreatePlanCommand;
import com.mro.common.dubbo.dtwin.request.OrderQueryParam;
import com.mro.common.dubbo.dtwin.request.PlanQueryParam;
import com.mro.common.dubbo.dtwin.request.UpdatePlanCommand;
import com.mro.common.dubbo.dtwin.response.MaintenanceOrderDTO;
import com.mro.common.dubbo.dtwin.response.ProductionPlanDTO;
import com.mro.common.dubbo.dtwin.service.ProductionPlanDubboService;
import com.mro.dtwin.service.ProductionService;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;

/**
 * 生产计划 / 维修指令 Dubbo 实现
 * Refs: MRO-005
 */
@DubboService(version = "1.0.0")
@RequiredArgsConstructor
public class ProductionPlanDubboServiceImpl implements ProductionPlanDubboService {

    private final ProductionService productionService;

    @Override
    public PageResult<ProductionPlanDTO> listPlans(PlanQueryParam param, UserContextDTO ctx) {
        return productionService.listPlans(param);
    }

    @Override
    public Long createPlan(CreatePlanCommand cmd) {
        return productionService.createPlan(cmd);
    }

    @Override
    public void updatePlan(UpdatePlanCommand cmd) {
        productionService.updatePlan(cmd);
    }

    @Override
    public PageResult<MaintenanceOrderDTO> listOrders(OrderQueryParam param, UserContextDTO ctx) {
        return productionService.listOrders(param);
    }

    @Override
    public Long createOrder(CreateOrderCommand cmd) {
        return productionService.createOrder(cmd);
    }

    @Override
    public void updateOrderProgress(Long orderId, int progress, String status, Long operatorId) {
        productionService.updateOrderProgress(orderId, progress, status, operatorId);
    }
}

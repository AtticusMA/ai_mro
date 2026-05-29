package com.mro.common.dubbo.dtwin.service;

import com.mro.common.core.response.PageResult;
import com.mro.common.dubbo.common.request.UserContextDTO;
import com.mro.common.dubbo.dtwin.request.CreateOrderCommand;
import com.mro.common.dubbo.dtwin.request.CreatePlanCommand;
import com.mro.common.dubbo.dtwin.request.OrderQueryParam;
import com.mro.common.dubbo.dtwin.request.PlanQueryParam;
import com.mro.common.dubbo.dtwin.request.UpdatePlanCommand;
import com.mro.common.dubbo.dtwin.response.MaintenanceOrderDTO;
import com.mro.common.dubbo.dtwin.response.ProductionPlanDTO;

/**
 * 生产计划 / 维修指令 Dubbo 接口
 * Refs: MRO-005
 */
public interface ProductionPlanDubboService {

    PageResult<ProductionPlanDTO> listPlans(PlanQueryParam param, UserContextDTO ctx);

    Long createPlan(CreatePlanCommand cmd);

    void updatePlan(UpdatePlanCommand cmd);

    PageResult<MaintenanceOrderDTO> listOrders(OrderQueryParam param, UserContextDTO ctx);

    Long createOrder(CreateOrderCommand cmd);

    void updateOrderProgress(Long orderId, int progress, String status, Long operatorId);
}

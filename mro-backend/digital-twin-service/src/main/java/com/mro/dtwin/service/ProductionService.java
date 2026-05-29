package com.mro.dtwin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mro.common.core.exception.BizException;
import com.mro.common.core.response.PageResult;
import com.mro.common.dubbo.dtwin.request.*;
import com.mro.common.dubbo.dtwin.response.*;
import com.mro.dtwin.domain.entity.MaintenanceOrder;
import com.mro.dtwin.domain.entity.ProductionPlan;
import com.mro.dtwin.domain.entity.ResourceUsage;
import com.mro.dtwin.mapper.MaintenanceOrderMapper;
import com.mro.dtwin.mapper.ProductionPlanMapper;
import com.mro.dtwin.mapper.ResourceUsageMapper;
import com.mro.dtwin.mapper.WorkstationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductionService {

    private static final int ERR_PLAN_NOT_FOUND  = 4602;
    private static final int ERR_ORDER_NOT_FOUND = 4603;
    private static final int ERR_PROGRESS_INVALID = 4604;
    private static final int ERR_WS_OCCUPIED     = 4605;
    private static final int ERR_PLAN_COMPLETED  = 4606;

    private final ProductionPlanMapper planMapper;
    private final MaintenanceOrderMapper orderMapper;
    private final ResourceUsageMapper resourceUsageMapper;
    private final WorkstationMapper workstationMapper;
    private final DtwinEventPublisher eventPublisher;

    public PageResult<ProductionPlanDTO> listPlans(PlanQueryParam param) {
        LambdaQueryWrapper<ProductionPlan> wrapper = new LambdaQueryWrapper<ProductionPlan>()
                .eq(param.hangarId() != null, ProductionPlan::getHangarId, param.hangarId())
                .eq(param.status() != null, ProductionPlan::getStatus, param.status())
                .orderByDesc(ProductionPlan::getCreateTime);
        Page<ProductionPlan> page = planMapper.selectPage(new Page<>(param.pageNum(), param.pageSize()), wrapper);
        List<ProductionPlanDTO> dtos = page.getRecords().stream().map(this::toPlanDTO).toList();
        return PageResult.of(dtos, page.getTotal(), param.pageNum(), param.pageSize());
    }

    @Transactional
    public Long createPlan(CreatePlanCommand cmd) {
        ProductionPlan plan = new ProductionPlan();
        plan.setHangarId(cmd.hangarId());
        plan.setAircraftId(cmd.aircraftId());
        plan.setPlanType(cmd.planType());
        plan.setScheduledStart(LocalDateTime.ofInstant(cmd.scheduledStart(), ZoneOffset.UTC));
        plan.setScheduledEnd(LocalDateTime.ofInstant(cmd.scheduledEnd(), ZoneOffset.UTC));
        plan.setStatus("draft");
        plan.setCreatedBy(cmd.operatorId());
        planMapper.insert(plan);
        return plan.getId();
    }

    @Transactional
    public void updatePlan(UpdatePlanCommand cmd) {
        ProductionPlan plan = planMapper.selectById(cmd.id());
        if (plan == null) throw new BizException(ERR_PLAN_NOT_FOUND, "生产计划不存在");
        if ("completed".equals(plan.getStatus())) throw new BizException(ERR_PLAN_COMPLETED, "已完成计划不可编辑");
        if (cmd.status() != null) plan.setStatus(cmd.status());
        if (cmd.scheduledStart() != null) plan.setScheduledStart(LocalDateTime.ofInstant(cmd.scheduledStart(), ZoneOffset.UTC));
        if (cmd.scheduledEnd() != null) plan.setScheduledEnd(LocalDateTime.ofInstant(cmd.scheduledEnd(), ZoneOffset.UTC));
        planMapper.updateById(plan);
    }

    public PageResult<MaintenanceOrderDTO> listOrders(OrderQueryParam param) {
        LambdaQueryWrapper<MaintenanceOrder> wrapper = new LambdaQueryWrapper<MaintenanceOrder>()
                .eq(param.planId() != null, MaintenanceOrder::getPlanId, param.planId())
                .eq(param.status() != null, MaintenanceOrder::getStatus, param.status())
                .orderByDesc(MaintenanceOrder::getCreateTime);
        Page<MaintenanceOrder> page = orderMapper.selectPage(new Page<>(param.pageNum(), param.pageSize()), wrapper);
        List<MaintenanceOrderDTO> dtos = page.getRecords().stream().map(o -> toOrderDTO(o, null, null)).toList();
        return PageResult.of(dtos, page.getTotal(), param.pageNum(), param.pageSize());
    }

    @Transactional
    public Long createOrder(CreateOrderCommand cmd) {
        ProductionPlan plan = planMapper.selectById(cmd.planId());
        if (plan == null) throw new BizException(ERR_PLAN_NOT_FOUND, "生产计划不存在");

        MaintenanceOrder order = new MaintenanceOrder();
        order.setPlanId(cmd.planId());
        order.setWorkstationId(cmd.workstationId());
        order.setAssigneeId(cmd.assigneeId());
        order.setDescription(cmd.description());
        order.setProgress(0);
        order.setStatus("pending");
        orderMapper.insert(order);
        return order.getId();
    }

    @Transactional
    public void updateOrderProgress(Long orderId, int progress, String status, Long operatorId) {
        if (progress < 0 || progress > 100) throw new BizException(ERR_PROGRESS_INVALID, "进度值必须为0-100");
        MaintenanceOrder order = orderMapper.selectById(orderId);
        if (order == null) throw new BizException(ERR_ORDER_NOT_FOUND, "维修指令不存在");
        order.setProgress(progress);
        if (status != null) order.setStatus(status);
        orderMapper.updateById(order);
        eventPublisher.publishOrderProgress(order.getPlanId(), orderId, progress, status);
    }

    public void allocateResource(Long workstationId, Long orderId, String resourceType, Long resourceId) {
        ResourceUsage usage = new ResourceUsage();
        usage.setWorkstationId(workstationId);
        usage.setOrderId(orderId);
        usage.setResourceType(resourceType);
        usage.setResourceId(resourceId);
        usage.setAllocatedAt(LocalDateTime.now());
        resourceUsageMapper.insert(usage);
    }

    public void releaseResource(Long usageId) {
        ResourceUsage usage = resourceUsageMapper.selectById(usageId);
        if (usage != null) {
            usage.setReleasedAt(LocalDateTime.now());
            resourceUsageMapper.updateById(usage);
        }
    }

    private ProductionPlanDTO toPlanDTO(ProductionPlan p) {
        return new ProductionPlanDTO(
                p.getId(), p.getHangarId(), p.getAircraftId(), p.getPlanType(),
                p.getScheduledStart().toInstant(ZoneOffset.UTC),
                p.getScheduledEnd().toInstant(ZoneOffset.UTC),
                p.getStatus());
    }

    private MaintenanceOrderDTO toOrderDTO(MaintenanceOrder o, String workstationName, String assigneeName) {
        return new MaintenanceOrderDTO(
                o.getId(), o.getPlanId(), o.getWorkstationId(),
                workstationName, o.getAssigneeId(), assigneeName,
                o.getDescription(), o.getProgress(), o.getStatus());
    }
}

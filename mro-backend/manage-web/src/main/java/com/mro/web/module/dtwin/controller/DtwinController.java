package com.mro.web.module.dtwin.controller;

import com.mro.common.core.response.PageResult;
import com.mro.common.core.response.R;
import com.mro.common.dubbo.dtwin.request.*;
import com.mro.common.dubbo.dtwin.response.*;
import com.mro.web.module.dtwin.app.DtwinAppService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * 数字孪生（机库 / 计划 / 工单 / 分析）接口
 * Refs: MRO-005
 */
@RestController
@RequestMapping("/api/dtwin")
@Validated
@RequiredArgsConstructor
public class DtwinController {

    private final DtwinAppService dtwinAppService;

    // ---- Hangars ----

    @GetMapping("/hangars")
    public R<PageResult<HangarDTO>> listHangars() {
        return R.ok(dtwinAppService.listHangars());
    }

    @GetMapping("/hangars/{id}/model")
    public R<HangarModelDTO> getHangarModel(@PathVariable Long id) {
        return R.ok(dtwinAppService.getHangarModel(id));
    }

    @GetMapping("/hangars/{id}/workstations")
    public R<PageResult<WorkstationDTO>> listWorkstations(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "50") int pageSize) {
        return R.ok(dtwinAppService.listWorkstations(id, pageNum, pageSize));
    }

    // ---- Production Plans ----

    @GetMapping("/plans")
    public R<PageResult<ProductionPlanDTO>> listPlans(
            @RequestParam(required = false) Long hangarId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String planType,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return R.ok(dtwinAppService.listPlans(hangarId, status, planType, pageNum, pageSize));
    }

    @PostMapping("/plans")
    public R<Long> createPlan(@RequestBody CreatePlanCommand cmd) {
        return R.ok(dtwinAppService.createPlan(cmd));
    }

    @PutMapping("/plans")
    public R<Void> updatePlan(@RequestBody UpdatePlanCommand cmd) {
        dtwinAppService.updatePlan(cmd);
        return R.ok();
    }

    // ---- Maintenance Orders ----

    @GetMapping("/orders")
    public R<PageResult<MaintenanceOrderDTO>> listOrders(
            @RequestParam(required = false) Long planId,
            @RequestParam(required = false) Long workstationId,
            @RequestParam(required = false) Long assigneeId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return R.ok(dtwinAppService.listOrders(planId, workstationId, assigneeId, status, pageNum, pageSize));
    }

    @PostMapping("/orders")
    public R<Long> createOrder(@RequestBody CreateOrderCommand cmd) {
        return R.ok(dtwinAppService.createOrder(cmd));
    }

    @PutMapping("/orders/{id}/progress")
    public R<Void> updateProgress(
            @PathVariable Long id,
            @RequestParam int progress,
            @RequestParam(required = false) String status) {
        dtwinAppService.updateOrderProgress(id, progress, status);
        return R.ok();
    }

    // ---- Analytics ----

    @GetMapping("/analytics/workload")
    public R<WorkloadAnalyticsDTO> analyzeWorkload(
            @RequestParam(required = false) Long hangarId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return R.ok(dtwinAppService.analyzeWorkload(hangarId, startDate, endDate));
    }

    @GetMapping("/analytics/efficiency")
    public R<EfficiencyAnalyticsDTO> analyzeEfficiency(
            @RequestParam(required = false) Long hangarId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return R.ok(dtwinAppService.analyzeEfficiency(hangarId, startDate, endDate));
    }
}

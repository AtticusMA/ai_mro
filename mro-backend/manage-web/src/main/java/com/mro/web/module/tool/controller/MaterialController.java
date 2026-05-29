package com.mro.web.module.tool.controller;

import com.mro.common.core.response.PageResult;
import com.mro.common.core.response.R;
import com.mro.common.dubbo.tool.request.*;
import com.mro.common.dubbo.material.request.*;
import com.mro.common.dubbo.material.response.*;
import com.mro.common.dubbo.tool.response.*;
import com.mro.web.module.tool.app.MaterialAppService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/material")
@Validated
@RequiredArgsConstructor
public class MaterialController {

    private final MaterialAppService materialAppService;

    @GetMapping("/items")
    public R<PageResult<MaterialItemDTO>> listMaterials(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean lowStock,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return R.ok(materialAppService.listMaterials(category, lowStock, pageNum, pageSize));
    }

    @PostMapping("/items")
    public R<Long> createMaterial(@RequestBody CreateMaterialCommand cmd) {
        return R.ok(materialAppService.createMaterial(cmd));
    }

    @PutMapping("/items/{id}")
    public R<Void> updateMaterial(@PathVariable Long id, @RequestBody UpdateMaterialCommand cmd) {
        materialAppService.updateMaterial(new UpdateMaterialCommand(
                id, cmd.partNo(), cmd.name(), cmd.category(), cmd.stockQty(), cmd.minStock(),
                cmd.location(), cmd.expiryDate(), cmd.unitPrice(), cmd.updatedBy()));
        return R.ok();
    }

    @GetMapping("/alerts")
    public R<PageResult<MaterialAlertDTO>> listRestockAlerts(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return R.ok(materialAppService.listRestockAlerts(pageNum, pageSize));
    }

    @PostMapping("/repair-orders")
    public R<Long> createRepairOrder(@RequestBody CreateRepairOrderCommand cmd) {
        return R.ok(materialAppService.createRepairOrder(cmd));
    }

    @GetMapping("/repair-orders")
    public R<PageResult<RepairOrderDTO>> listRepairOrders(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return R.ok(materialAppService.listRepairOrders(pageNum, pageSize));
    }
}

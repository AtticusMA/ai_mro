package com.mro.web.module.tool.controller;

import com.mro.common.core.response.PageResult;
import com.mro.common.core.response.R;
import com.mro.common.dubbo.tool.request.*;
import com.mro.common.dubbo.material.request.*;
import com.mro.common.dubbo.material.response.*;
import com.mro.common.dubbo.tool.response.*;
import com.mro.web.module.tool.app.MaterialRequestAppService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@Validated
@RequiredArgsConstructor
public class MaterialRequestController {

    private final MaterialRequestAppService materialRequestAppService;

    // GET /api/workcards/{workcardId}/bom
    @GetMapping("/workcards/{workcardId}/bom")
    public R<WorkcardBomDTO> getWorkcardBom(@PathVariable Long workcardId) {
        return R.ok(materialRequestAppService.getWorkcardBom(workcardId));
    }

    // POST /api/material-requests
    @PostMapping("/material-requests")
    public R<Long> createMaterialRequest(@RequestBody CreateMaterialRequestCommand cmd) {
        return R.ok(materialRequestAppService.createMaterialRequest(cmd));
    }

    // GET /api/material-requests
    @GetMapping("/material-requests")
    public R<PageResult<MaterialRequestDTO>> listMaterialRequests(
            @RequestParam(required = false) Long workcardId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return R.ok(materialRequestAppService.listMaterialRequests(workcardId, status, pageNum, pageSize));
    }

    // GET /api/material-requests/{id}
    @GetMapping("/material-requests/{id}")
    public R<MaterialRequestDetailDTO> getMaterialRequest(@PathVariable Long id) {
        return R.ok(materialRequestAppService.getMaterialRequest(id));
    }

    // POST /api/material-requests/{id}/approve
    @PostMapping("/material-requests/{id}/approve")
    public R<Void> approveMaterialRequest(@PathVariable Long id) {
        materialRequestAppService.approveMaterialRequest(id);
        return R.ok();
    }

    // POST /api/material-requests/{id}/reject
    @PostMapping("/material-requests/{id}/reject")
    public R<Void> rejectMaterialRequest(@PathVariable Long id, @RequestBody Map<String, String> body) {
        materialRequestAppService.rejectMaterialRequest(id, body.get("rejectReason"));
        return R.ok();
    }

    // POST /api/material-requests/{id}/receive
    @PostMapping("/material-requests/{id}/receive")
    public R<Void> receiveMaterialRequest(@PathVariable Long id) {
        materialRequestAppService.receiveMaterialRequest(id);
        return R.ok();
    }
}

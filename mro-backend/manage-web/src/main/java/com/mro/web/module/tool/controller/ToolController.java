package com.mro.web.module.tool.controller;

import com.mro.common.core.response.PageResult;
import com.mro.common.core.response.R;
import com.mro.common.dubbo.tool.request.*;
import com.mro.common.dubbo.tool.response.*;
import com.mro.web.module.tool.app.ToolAppService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tool")
@Validated
@RequiredArgsConstructor
public class ToolController {

    private final ToolAppService toolAppService;

    @PostMapping("/borrow")
    public R<BorrowResultDTO> borrowTools(@RequestBody BorrowCommand cmd) {
        return R.ok(toolAppService.borrowTools(cmd));
    }

    @PostMapping("/return")
    public R<ReturnResultDTO> returnTools(@RequestBody ReturnCommand cmd) {
        return R.ok(toolAppService.returnTools(cmd));
    }

    @GetMapping("/cabinets")
    public R<PageResult<ToolCabinetDTO>> listCabinets() {
        return R.ok(toolAppService.listCabinets());
    }

    @GetMapping("/cabinets/{id}/slots")
    public R<List<SlotStatusDTO>> getCabinetSlots(@PathVariable Long id) {
        return R.ok(toolAppService.getCabinetSlots(id));
    }

    @PostMapping("/cabinets/{id}/inventory")
    public R<Void> triggerInventory(@PathVariable Long id) {
        toolAppService.triggerInventory(id);
        return R.ok();
    }

    @GetMapping("/tools")
    public R<PageResult<ToolDTO>> listTools(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Long cabinetId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return R.ok(toolAppService.listTools(status, category, cabinetId, pageNum, pageSize));
    }

    @GetMapping("/tools/{id}/lifecycle")
    public R<ToolLifecycleDTO> getToolLifecycle(@PathVariable Long id) {
        return R.ok(toolAppService.getToolLifecycle(id));
    }

    @GetMapping("/borrow-records")
    public R<PageResult<BorrowRecordDTO>> listBorrowRecords(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return R.ok(toolAppService.listBorrowRecords(userId, status, pageNum, pageSize));
    }

    @GetMapping("/alerts")
    public R<PageResult<ToolAlertDTO>> listAlerts(
            @RequestParam(required = false) String alertType,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return R.ok(toolAppService.listAlerts(alertType, pageNum, pageSize));
    }
}

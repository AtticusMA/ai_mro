package com.mro.web.module.dtwin.controller;

import com.mro.common.core.response.PageResult;
import com.mro.common.core.response.R;
import com.mro.common.dubbo.dtwin.request.CreateTaskPackageCommand;
import com.mro.common.dubbo.dtwin.response.TaskPackageDTO;
import com.mro.web.module.dtwin.app.TaskPackageAppService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 任务包接口
 * Refs: MRO-005
 */
@RestController
@RequestMapping("/api/dtwin/tasks")
@RequiredArgsConstructor
public class TaskPackageController {

    private final TaskPackageAppService taskPackageAppService;

    @GetMapping
    public R<PageResult<TaskPackageDTO>> listTaskPackages(
            @RequestParam(required = false) Long hangarId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String aircraftType,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return R.ok(taskPackageAppService.listTaskPackages(hangarId, status, aircraftType, pageNum, pageSize));
    }

    @PostMapping
    public R<Long> createTaskPackage(@RequestBody CreateTaskPackageCommand cmd) {
        return R.ok(taskPackageAppService.createTaskPackage(cmd));
    }

    @PutMapping("/{id}/status")
    public R<Void> updateTaskPackageStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        taskPackageAppService.updateTaskPackageStatus(id, body.get("status"));
        return R.ok();
    }
}

package com.mro.web.module.sys.controller;

import com.mro.common.core.response.PageResult;
import com.mro.common.core.response.R;
import com.mro.common.dubbo.system.response.OperationLogDTO;
import com.mro.web.module.sys.app.OperationLogAppService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/system/operation-log")
public class OperationLogController {

    @Autowired
    private OperationLogAppService operationLogAppService;

    @GetMapping
    public R<PageResult<OperationLogDTO>> listPage(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String operatorName,
            @RequestParam(required = false) String requestUri,
            @RequestParam(required = false) Long deptId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {

        return R.ok(operationLogAppService.listPage(
                pageNum, pageSize, operatorName, requestUri, deptId, startTime, endTime));
    }

    @GetMapping("/{id}")
    public R<OperationLogDTO> getById(@PathVariable Long id) {
        return R.ok(operationLogAppService.getById(id));
    }
}

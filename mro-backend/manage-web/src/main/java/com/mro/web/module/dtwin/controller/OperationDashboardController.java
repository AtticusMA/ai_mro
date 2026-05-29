package com.mro.web.module.dtwin.controller;

import com.mro.common.core.response.R;
import com.mro.common.dubbo.dtwin.response.OperationDashboardDTO;
import com.mro.web.module.dtwin.app.OperationDashboardAppService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 运维看板接口
 * Refs: MRO-005
 */
@RestController
@RequestMapping("/api/dtwin/dashboard")
@RequiredArgsConstructor
public class OperationDashboardController {

    private final OperationDashboardAppService operationDashboardAppService;

    @GetMapping("/operation")
    public R<OperationDashboardDTO> getOperationDashboard(@RequestParam(required = false) Long hangarId) {
        return R.ok(operationDashboardAppService.getOperationDashboard(hangarId));
    }
}

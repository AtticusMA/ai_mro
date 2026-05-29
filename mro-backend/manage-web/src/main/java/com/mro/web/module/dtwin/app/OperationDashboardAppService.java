package com.mro.web.module.dtwin.app;

import com.mro.common.dubbo.dtwin.response.OperationDashboardDTO;
import com.mro.common.dubbo.dtwin.service.TaskPackageDubboService;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

/**
 * 运维看板应用服务
 * Refs: MRO-005
 */
@Service
@RequiredArgsConstructor
public class OperationDashboardAppService {

    @DubboReference(version = "1.0.0")
    private TaskPackageDubboService taskPackageDubboService;

    public OperationDashboardDTO getOperationDashboard(Long hangarId) {
        return taskPackageDubboService.getOperationDashboard(hangarId);
    }
}

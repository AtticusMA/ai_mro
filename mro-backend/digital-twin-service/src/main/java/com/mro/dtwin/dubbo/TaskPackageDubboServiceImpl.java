package com.mro.dtwin.dubbo;

import com.mro.common.core.response.PageResult;
import com.mro.common.dubbo.dtwin.request.CreateTaskPackageCommand;
import com.mro.common.dubbo.dtwin.request.TaskPackageQueryParam;
import com.mro.common.dubbo.dtwin.response.OperationDashboardDTO;
import com.mro.common.dubbo.dtwin.response.TaskPackageDTO;
import com.mro.common.dubbo.dtwin.service.TaskPackageDubboService;
import com.mro.dtwin.service.TaskPackageService;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;

/**
 * 任务包 / 运营看板 Dubbo 实现
 * Refs: MRO-005
 */
@DubboService(version = "1.0.0")
@RequiredArgsConstructor
public class TaskPackageDubboServiceImpl implements TaskPackageDubboService {

    private final TaskPackageService taskPackageService;

    @Override
    public PageResult<TaskPackageDTO> listTaskPackages(TaskPackageQueryParam param) {
        return taskPackageService.listTaskPackages(param);
    }

    @Override
    public Long createTaskPackage(CreateTaskPackageCommand cmd) {
        return taskPackageService.createTaskPackage(cmd);
    }

    @Override
    public void updateTaskPackageStatus(Long id, String newStatus, Long operatorId) {
        taskPackageService.updateTaskPackageStatus(id, newStatus, operatorId);
    }

    @Override
    public OperationDashboardDTO getOperationDashboard(Long hangarId) {
        return taskPackageService.getOperationDashboard(hangarId);
    }
}

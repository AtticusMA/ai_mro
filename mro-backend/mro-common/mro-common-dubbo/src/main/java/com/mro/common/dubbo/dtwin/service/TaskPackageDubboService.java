package com.mro.common.dubbo.dtwin.service;

import com.mro.common.core.response.PageResult;
import com.mro.common.dubbo.dtwin.request.CreateTaskPackageCommand;
import com.mro.common.dubbo.dtwin.request.TaskPackageQueryParam;
import com.mro.common.dubbo.dtwin.response.OperationDashboardDTO;
import com.mro.common.dubbo.dtwin.response.TaskPackageDTO;

/**
 * 任务包 / 运营看板 Dubbo 接口
 * Refs: MRO-005
 */
public interface TaskPackageDubboService {

    PageResult<TaskPackageDTO> listTaskPackages(TaskPackageQueryParam param);

    Long createTaskPackage(CreateTaskPackageCommand cmd);

    void updateTaskPackageStatus(Long id, String newStatus, Long operatorId);

    OperationDashboardDTO getOperationDashboard(Long hangarId);
}

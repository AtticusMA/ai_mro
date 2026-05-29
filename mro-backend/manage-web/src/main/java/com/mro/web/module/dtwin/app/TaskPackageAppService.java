package com.mro.web.module.dtwin.app;

import com.mro.common.core.response.PageResult;
import com.mro.common.dubbo.dtwin.request.CreateTaskPackageCommand;
import com.mro.common.dubbo.dtwin.request.TaskPackageQueryParam;
import com.mro.common.dubbo.dtwin.response.TaskPackageDTO;
import com.mro.common.dubbo.dtwin.service.TaskPackageDubboService;
import com.mro.web.context.UserContext;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

/**
 * 任务包应用服务
 * Refs: MRO-005
 */
@Service
@RequiredArgsConstructor
public class TaskPackageAppService {

    @DubboReference(version = "1.0.0")
    private TaskPackageDubboService taskPackageDubboService;

    public PageResult<TaskPackageDTO> listTaskPackages(Long hangarId, String status,
                                                       String aircraftType, int pageNum, int pageSize) {
        return taskPackageDubboService.listTaskPackages(
                new TaskPackageQueryParam(pageNum, pageSize, hangarId, status, aircraftType));
    }

    public Long createTaskPackage(CreateTaskPackageCommand cmd) {
        Long userId = UserContext.getUserId();
        return taskPackageDubboService.createTaskPackage(new CreateTaskPackageCommand(
                cmd.title(), cmd.hangarId(), cmd.workstationId(), cmd.aircraftType(),
                cmd.registration(), cmd.planStart(), cmd.planEnd(), cmd.priority(),
                cmd.orderIds(), userId));
    }

    public void updateTaskPackageStatus(Long id, String newStatus) {
        taskPackageDubboService.updateTaskPackageStatus(id, newStatus, UserContext.getUserId());
    }
}

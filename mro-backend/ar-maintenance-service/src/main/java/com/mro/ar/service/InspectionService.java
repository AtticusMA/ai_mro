package com.mro.ar.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mro.ar.context.DataScopeContext;
import com.mro.ar.domain.entity.InspectionTask;
import com.mro.ar.mapper.InspectionTaskMapper;
import com.mro.common.core.exception.BizException;
import com.mro.common.core.response.PageResult;
import com.mro.common.dubbo.common.request.*;
import com.mro.common.dubbo.ar.request.*;
import com.mro.common.dubbo.ar.response.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Slf4j
@Service
@RequiredArgsConstructor
public class InspectionService {

    private final InspectionTaskMapper inspectionTaskMapper;

    public PageResult<InspectionTaskDTO> listInspections(InspectionQueryParam param, UserContextDTO ctx) {
        LambdaQueryWrapper<InspectionTask> wrapper = new LambdaQueryWrapper<InspectionTask>()
                .eq(StringUtils.hasText(param.status()), InspectionTask::getStatus, param.status())
                .eq(StringUtils.hasText(param.aircraftId()), InspectionTask::getAircraftId, param.aircraftId())
                .orderByDesc(InspectionTask::getCreatedAt);

        // 数据权限过滤：巡检任务无 deptId 字段，仅支持 selfOnly（按 inspectorId）
        if (DataScopeContext.isSelfOnly()) {
            Long userId = DataScopeContext.getUserId();
            if (userId != null) {
                wrapper.eq(InspectionTask::getInspectorId, userId);
            }
        }

        Page<InspectionTask> page = inspectionTaskMapper.selectPage(
                new Page<>(param.pageNum(), param.pageSize()), wrapper);

        return PageResult.of(page.getRecords().stream()
                .map(this::toDTO).toList(), page.getTotal(), (int) page.getCurrent(), (int) page.getSize());
    }

    @Transactional
    public Long createInspection(CreateInspectionCommand cmd) {
        InspectionTask task = new InspectionTask();
        task.setAircraftId(cmd.aircraftId());
        task.setRouteTemplate(cmd.routeTemplate());
        task.setInspectorId(cmd.inspectorId());
        task.setStatus("pending");
        inspectionTaskMapper.insert(task);
        return task.getId();
    }

    @Transactional
    public void startInspection(Long taskId, Long operatorId) {
        InspectionTask task = getTaskOrThrow(taskId);
        if (!"pending".equals(task.getStatus())) {
            throw new BizException(4301, "巡检任务状态不允许该操作");
        }
        task.setStatus("in_progress");
        task.setStartedAt(LocalDateTime.now());
        inspectionTaskMapper.updateById(task);
    }

    @Transactional
    public void completeInspection(Long taskId, Long operatorId) {
        InspectionTask task = getTaskOrThrow(taskId);
        if (!"in_progress".equals(task.getStatus())) {
            throw new BizException(4301, "巡检任务状态不允许该操作");
        }
        task.setStatus("completed");
        task.setCompletedAt(LocalDateTime.now());
        inspectionTaskMapper.updateById(task);
    }

    public InspectionTask getTaskOrThrow(Long taskId) {
        InspectionTask task = inspectionTaskMapper.selectById(taskId);
        if (task == null) {
            throw new BizException(4300, "巡检任务不存在");
        }
        return task;
    }

    private InspectionTaskDTO toDTO(InspectionTask t) {
        return new InspectionTaskDTO(
                t.getId(),
                t.getAircraftId(),
                t.getInspectorId(),
                null,
                t.getRouteTemplate(),
                t.getStatus(),
                t.getStartedAt() != null ? t.getStartedAt().toInstant(ZoneOffset.UTC) : null,
                t.getCompletedAt() != null ? t.getCompletedAt().toInstant(ZoneOffset.UTC) : null
        );
    }
}

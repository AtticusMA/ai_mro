package com.mro.dtwin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mro.common.core.exception.BizException;
import com.mro.common.core.response.PageResult;
import com.mro.common.dubbo.dtwin.request.*;
import com.mro.common.dubbo.dtwin.response.*;
import com.mro.dtwin.domain.entity.PersonnelAssignment;
import com.mro.dtwin.domain.entity.TaskPackage;
import com.mro.dtwin.domain.entity.TaskPackageOrder;
import com.mro.dtwin.mapper.PersonnelAssignmentMapper;
import com.mro.dtwin.mapper.TaskPackageMapper;
import com.mro.dtwin.mapper.TaskPackageOrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskPackageService {

    private static final int ERR_PACKAGE_NOT_FOUND   = 4607;
    private static final int ERR_STATUS_INVALID      = 4608;
    private static final int ERR_PLAN_TIME_INVALID   = 4609;

    private final TaskPackageMapper taskPackageMapper;
    private final TaskPackageOrderMapper taskPackageOrderMapper;
    private final PersonnelAssignmentMapper personnelAssignmentMapper;

    @Transactional
    public Long createTaskPackage(CreateTaskPackageCommand cmd) {
        if (cmd.planEnd() != null && cmd.planStart() != null
                && cmd.planEnd().isBefore(cmd.planStart())) {
            throw new BizException(ERR_PLAN_TIME_INVALID, "计划结束时间不能早于开始时间");
        }

        String packageNo = String.format("TP-%s-%04d",
                LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE),
                ThreadLocalRandom.current().nextInt(1000, 9999));

        TaskPackage pkg = new TaskPackage();
        pkg.setPackageNo(packageNo);
        pkg.setTitle(cmd.title());
        pkg.setHangarId(cmd.hangarId());
        pkg.setWorkstationId(cmd.workstationId());
        pkg.setAircraftType(cmd.aircraftType());
        pkg.setRegistration(cmd.registration());
        pkg.setPlanStart(cmd.planStart());
        pkg.setPlanEnd(cmd.planEnd());
        pkg.setPriority(cmd.priority());
        pkg.setStatus("draft");
        pkg.setCreatedBy(cmd.createdBy());
        taskPackageMapper.insert(pkg);

        if (cmd.orderIds() != null) {
            for (int i = 0; i < cmd.orderIds().size(); i++) {
                TaskPackageOrder tpo = new TaskPackageOrder();
                tpo.setPackageId(pkg.getId());
                tpo.setOrderId(cmd.orderIds().get(i));
                tpo.setSeqNo(i);
                taskPackageOrderMapper.insert(tpo);
            }
        }

        return pkg.getId();
    }

    @Transactional
    public void updateTaskPackageStatus(Long id, String newStatus, Long operatorId) {
        TaskPackage pkg = taskPackageMapper.selectById(id);
        if (pkg == null) throw new BizException(ERR_PACKAGE_NOT_FOUND, "任务包不存在");

        if (!isValidTransition(pkg.getStatus(), newStatus)) {
            throw new BizException(ERR_STATUS_INVALID, "当前状态不允许切换到: " + newStatus);
        }

        pkg.setStatus(newStatus);
        taskPackageMapper.updateById(pkg);
    }

    public PageResult<TaskPackageDTO> listTaskPackages(TaskPackageQueryParam param) {
        LambdaQueryWrapper<TaskPackage> wrapper = new LambdaQueryWrapper<TaskPackage>()
                .eq(param.hangarId() != null, TaskPackage::getHangarId, param.hangarId())
                .eq(param.status() != null, TaskPackage::getStatus, param.status())
                .eq(param.aircraftType() != null, TaskPackage::getAircraftType, param.aircraftType())
                .orderByDesc(TaskPackage::getCreateTime);
        Page<TaskPackage> page = taskPackageMapper.selectPage(
                new Page<>(param.pageNum(), param.pageSize()), wrapper);
        List<TaskPackageDTO> dtos = page.getRecords().stream().map(this::toDTO).toList();
        return PageResult.of(dtos, page.getTotal(), param.pageNum(), param.pageSize());
    }

    public TaskPackageDTO getTaskPackage(Long id) {
        TaskPackage pkg = taskPackageMapper.selectById(id);
        if (pkg == null) throw new BizException(ERR_PACKAGE_NOT_FOUND, "任务包不存在");
        return toDTO(pkg);
    }

    public OperationDashboardDTO getOperationDashboard(Long hangarId) {
        // totalPackages
        long totalPackages = taskPackageMapper.selectCount(
                new LambdaQueryWrapper<TaskPackage>()
                        .eq(hangarId != null, TaskPackage::getHangarId, hangarId));

        // inProgressPackages
        long inProgressPackages = taskPackageMapper.selectCount(
                new LambdaQueryWrapper<TaskPackage>()
                        .eq(hangarId != null, TaskPackage::getHangarId, hangarId)
                        .eq(TaskPackage::getStatus, "in_progress"));

        // completedToday
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = todayStart.plusDays(1);
        long completedToday = taskPackageMapper.selectCount(
                new LambdaQueryWrapper<TaskPackage>()
                        .eq(hangarId != null, TaskPackage::getHangarId, hangarId)
                        .eq(TaskPackage::getStatus, "completed")
                        .ge(TaskPackage::getUpdateTime, todayStart)
                        .lt(TaskPackage::getUpdateTime, todayEnd));

        // active packages (top 5 in_progress)
        List<TaskPackage> activeList = taskPackageMapper.selectList(
                new LambdaQueryWrapper<TaskPackage>()
                        .eq(hangarId != null, TaskPackage::getHangarId, hangarId)
                        .eq(TaskPackage::getStatus, "in_progress")
                        .orderByDesc(TaskPackage::getCreateTime)
                        .last("LIMIT 5"));
        List<TaskPackageDTO> activePackages = activeList.stream().map(this::toDTO).toList();

        // totalPersonnel: distinct user_id in personnel_assignment for active package ids
        int totalPersonnel = 0;
        if (!activeList.isEmpty()) {
            Set<Long> activeIds = activeList.stream()
                    .map(TaskPackage::getId)
                    .collect(Collectors.toSet());
            List<PersonnelAssignment> assignments = personnelAssignmentMapper.selectList(
                    new LambdaQueryWrapper<PersonnelAssignment>()
                            .in(PersonnelAssignment::getPackageId, activeIds));
            totalPersonnel = (int) assignments.stream()
                    .map(PersonnelAssignment::getUserId)
                    .distinct()
                    .count();
        }

        return new OperationDashboardDTO(
                (int) totalPackages,
                (int) inProgressPackages,
                (int) completedToday,
                totalPersonnel,
                activePackages);
    }

    // -------------------------------------------------------------------------
    // helpers
    // -------------------------------------------------------------------------

    private boolean isValidTransition(String current, String next) {
        if ("cancelled".equals(next)) return true;
        return switch (current) {
            case "draft"       -> "submitted".equals(next);
            case "submitted"   -> "in_progress".equals(next);
            case "in_progress" -> "completed".equals(next);
            default            -> false;
        };
    }

    private TaskPackageDTO toDTO(TaskPackage p) {
        return new TaskPackageDTO(
                p.getId(),
                p.getPackageNo(),
                p.getTitle(),
                p.getHangarId(),
                p.getWorkstationId(),
                p.getAircraftType(),
                p.getRegistration(),
                p.getPlanStart(),
                p.getPlanEnd(),
                p.getStatus(),
                p.getPriority(),
                p.getCreateTime());
    }
}

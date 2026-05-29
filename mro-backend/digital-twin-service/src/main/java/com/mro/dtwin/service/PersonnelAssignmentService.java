package com.mro.dtwin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mro.common.core.exception.BizException;
import com.mro.common.dubbo.dtwin.response.PersonnelAssignmentDTO;
import com.mro.common.dubbo.dtwin.request.SaveAssignmentCommand;
import com.mro.dtwin.domain.entity.PersonnelAssignment;
import com.mro.dtwin.domain.entity.TaskPackage;
import com.mro.dtwin.mapper.PersonnelAssignmentMapper;
import com.mro.dtwin.mapper.TaskPackageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PersonnelAssignmentService {

    private static final int ERR_DUPLICATE_ASSIGNMENT = 4611;
    private static final int ERR_PACKAGE_NOT_FOUND    = 4612;

    private final TaskPackageMapper taskPackageMapper;
    private final PersonnelAssignmentMapper assignmentMapper;

    @Transactional
    public Long saveAssignment(SaveAssignmentCommand cmd) {
        TaskPackage pkg = taskPackageMapper.selectById(cmd.packageId());
        if (pkg == null) throw new BizException(ERR_PACKAGE_NOT_FOUND, "任务包不存在");

        long count = assignmentMapper.selectCount(
                new LambdaQueryWrapper<PersonnelAssignment>()
                        .eq(PersonnelAssignment::getPackageId, cmd.packageId())
                        .eq(PersonnelAssignment::getUserId, cmd.userId())
                        .eq(PersonnelAssignment::getWorkDate, cmd.workDate()));
        if (count > 0) throw new BizException(ERR_DUPLICATE_ASSIGNMENT, "人员当日已有排班");

        PersonnelAssignment assignment = new PersonnelAssignment();
        assignment.setPackageId(cmd.packageId());
        assignment.setUserId(cmd.userId());
        assignment.setRole(cmd.role());
        assignment.setWorkDate(cmd.workDate());
        assignment.setShift(cmd.shift());
        assignmentMapper.insert(assignment);
        return assignment.getId();
    }

    public List<PersonnelAssignmentDTO> listAssignmentsByPackage(Long packageId) {
        List<PersonnelAssignment> list = assignmentMapper.selectList(
                new LambdaQueryWrapper<PersonnelAssignment>()
                        .eq(PersonnelAssignment::getPackageId, packageId));
        return list.stream().map(this::toDTO).toList();
    }

    public void deleteAssignment(Long id) {
        assignmentMapper.deleteById(id);
    }

    // -------------------------------------------------------------------------
    // helpers
    // -------------------------------------------------------------------------

    private PersonnelAssignmentDTO toDTO(PersonnelAssignment a) {
        return new PersonnelAssignmentDTO(
                a.getId(),
                a.getPackageId(),
                a.getUserId(),
                a.getRole(),
                a.getWorkDate(),
                a.getShift());
    }
}

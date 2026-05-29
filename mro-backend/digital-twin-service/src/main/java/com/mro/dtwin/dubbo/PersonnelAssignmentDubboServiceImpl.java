package com.mro.dtwin.dubbo;

import com.mro.common.dubbo.dtwin.request.SaveAssignmentCommand;
import com.mro.common.dubbo.dtwin.response.PersonnelAssignmentDTO;
import com.mro.common.dubbo.dtwin.service.PersonnelAssignmentDubboService;
import com.mro.dtwin.service.PersonnelAssignmentService;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;

import java.util.List;

/**
 * 人员排班 Dubbo 实现
 * Refs: MRO-005
 */
@DubboService(version = "1.0.0")
@RequiredArgsConstructor
public class PersonnelAssignmentDubboServiceImpl implements PersonnelAssignmentDubboService {

    private final PersonnelAssignmentService personnelAssignmentService;

    @Override
    public Long saveAssignment(SaveAssignmentCommand cmd) {
        return personnelAssignmentService.saveAssignment(cmd);
    }

    @Override
    public List<PersonnelAssignmentDTO> listAssignmentsByPackage(Long packageId) {
        return personnelAssignmentService.listAssignmentsByPackage(packageId);
    }
}

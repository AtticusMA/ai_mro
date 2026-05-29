package com.mro.web.module.dtwin.app;

import com.mro.common.dubbo.dtwin.request.SaveAssignmentCommand;
import com.mro.common.dubbo.dtwin.response.PersonnelAssignmentDTO;
import com.mro.common.dubbo.dtwin.service.PersonnelAssignmentDubboService;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 人员排班应用服务
 * Refs: MRO-005
 */
@Service
@RequiredArgsConstructor
public class PersonnelAssignmentAppService {

    @DubboReference(version = "1.0.0")
    private PersonnelAssignmentDubboService personnelAssignmentDubboService;

    public List<PersonnelAssignmentDTO> listAssignmentsByPackage(Long packageId) {
        return personnelAssignmentDubboService.listAssignmentsByPackage(packageId);
    }

    public Long saveAssignment(SaveAssignmentCommand cmd) {
        return personnelAssignmentDubboService.saveAssignment(cmd);
    }
}

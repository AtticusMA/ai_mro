package com.mro.common.dubbo.dtwin.service;

import com.mro.common.dubbo.dtwin.request.SaveAssignmentCommand;
import com.mro.common.dubbo.dtwin.response.PersonnelAssignmentDTO;

import java.util.List;

/**
 * 人员排班 Dubbo 接口
 * Refs: MRO-005
 */
public interface PersonnelAssignmentDubboService {

    Long saveAssignment(SaveAssignmentCommand cmd);

    List<PersonnelAssignmentDTO> listAssignmentsByPackage(Long packageId);
}

package com.mro.common.dubbo.system.service;

import com.mro.common.dubbo.system.request.CreateDeptCommand;
import com.mro.common.dubbo.system.request.UpdateDeptCommand;
import com.mro.common.dubbo.system.response.DeptDTO;
import com.mro.common.dubbo.system.response.DeptTreeDTO;

import java.util.List;

public interface DeptDubboService {

    List<DeptTreeDTO> getDeptTree();

    DeptDTO getDeptById(Long deptId);

    Long createDept(CreateDeptCommand cmd);

    void updateDept(UpdateDeptCommand cmd);

    void deleteDept(Long deptId);

    List<DeptDTO> getDeptsByIds(List<Long> deptIds);

    List<Long> getDescendantDeptIds(Long deptId);
}

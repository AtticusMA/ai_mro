package com.mro.common.dubbo.system.service;

import com.mro.common.core.response.PageResult;
import com.mro.common.dubbo.system.request.CreateRoleCommand;
import com.mro.common.dubbo.system.request.UpdateRoleCommand;
import com.mro.common.dubbo.system.request.RoleQueryParam;
import com.mro.common.dubbo.system.response.RoleDTO;
import com.mro.common.dubbo.system.response.RoleDetailDTO;
import com.mro.common.dubbo.system.response.UserDataScopeDTO;

import java.util.List;

public interface RoleDubboService {

    PageResult<RoleDTO> listRoles(RoleQueryParam param);

    RoleDetailDTO getRoleById(Long roleId);

    Long createRole(CreateRoleCommand cmd);

    void updateRole(UpdateRoleCommand cmd);

    void deleteRole(Long roleId);

    void assignMenus(Long roleId, List<Long> menuIds);

    void assignDepts(Long roleId, List<Long> deptIds);

    List<RoleDTO> getRolesByUserId(Long userId);

    UserDataScopeDTO getDataScopeByUserId(Long userId);
}

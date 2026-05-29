package com.mro.system.dubbo;

import com.mro.common.core.response.PageResult;
import com.mro.common.dubbo.system.request.CreateRoleCommand;
import com.mro.common.dubbo.system.response.RoleDTO;
import com.mro.common.dubbo.system.response.RoleDetailDTO;
import com.mro.common.dubbo.system.request.RoleQueryParam;
import com.mro.common.dubbo.system.request.UpdateRoleCommand;
import com.mro.common.dubbo.system.response.UserDataScopeDTO;
import com.mro.common.dubbo.system.request.*;
import com.mro.common.dubbo.system.response.*;
import com.mro.common.dubbo.system.service.RoleDubboService;
import com.mro.system.service.RoleService;
import org.apache.dubbo.config.annotation.DubboService;
import org.apache.dubbo.rpc.RpcContext;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@DubboService
public class RoleDubboServiceImpl implements RoleDubboService {

    @Autowired
    private RoleService roleService;

    @Override
    public PageResult<RoleDTO> listRoles(RoleQueryParam param) {
        return roleService.listRoles(param);
    }

    @Override
    public RoleDetailDTO getRoleById(Long roleId) {
        return roleService.getRoleById(roleId);
    }

    @Override
    public Long createRole(CreateRoleCommand cmd) {
        Long operatorId = getOperatorId();
        Long operatorDeptId = getOperatorDeptId();
        return roleService.createRole(cmd, operatorId, operatorDeptId);
    }

    @Override
    public void updateRole(UpdateRoleCommand cmd) {
        Long operatorId = getOperatorId();
        roleService.updateRole(cmd, operatorId);
    }

    @Override
    public void deleteRole(Long roleId) {
        roleService.deleteRole(roleId);
    }

    @Override
    public void assignMenus(Long roleId, List<Long> menuIds) {
        roleService.assignMenus(roleId, menuIds);
    }

    @Override
    public void assignDepts(Long roleId, List<Long> deptIds) {
        roleService.assignDepts(roleId, deptIds);
    }

    @Override
    public List<RoleDTO> getRolesByUserId(Long userId) {
        return roleService.getRolesByUserId(userId);
    }

    @Override
    public UserDataScopeDTO getDataScopeByUserId(Long userId) {
        return roleService.getDataScopeByUserId(userId);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private Long getOperatorId() {
        String v = RpcContext.getServiceContext().getAttachment("userId");
        return v != null ? Long.parseLong(v) : 1L;
    }

    private Long getOperatorDeptId() {
        String v = RpcContext.getServiceContext().getAttachment("deptId");
        return v != null ? Long.parseLong(v) : 1L;
    }
}

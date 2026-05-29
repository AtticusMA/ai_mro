package com.mro.web.module.sys.app;

import com.mro.common.core.response.PageResult;
import com.mro.common.dubbo.common.request.*;
import com.mro.common.dubbo.system.request.*;
import com.mro.common.dubbo.system.response.*;
import com.mro.common.dubbo.system.service.RoleDubboService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleAppService {

    @DubboReference(version = "1.0.0", timeout = 5000, retries = 0)
    private RoleDubboService roleDubboService;

    public PageResult<RoleDTO> listRoles(RoleQueryParam param) {
        return roleDubboService.listRoles(param);
    }

    public RoleDetailDTO getRoleById(Long id) {
        return roleDubboService.getRoleById(id);
    }

    public void createRole(CreateRoleCommand cmd) {
        roleDubboService.createRole(cmd);
    }

    public void updateRole(UpdateRoleCommand cmd) {
        roleDubboService.updateRole(cmd);
    }

    public void deleteRole(Long id) {
        roleDubboService.deleteRole(id);
    }

    public void assignMenus(Long id, List<Long> menuIds) {
        roleDubboService.assignMenus(id, menuIds);
    }

    public void assignDepts(Long id, List<Long> deptIds) {
        roleDubboService.assignDepts(id, deptIds);
    }
}

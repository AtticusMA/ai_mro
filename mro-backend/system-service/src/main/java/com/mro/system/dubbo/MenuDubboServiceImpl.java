package com.mro.system.dubbo;

import com.mro.common.dubbo.system.request.CreateMenuCommand;
import com.mro.common.dubbo.system.response.MenuTreeDTO;
import com.mro.common.dubbo.system.request.UpdateMenuCommand;
import com.mro.common.dubbo.system.response.UserMenuResult;
import com.mro.common.dubbo.system.request.*;
import com.mro.common.dubbo.system.response.*;
import com.mro.common.dubbo.system.service.MenuDubboService;
import com.mro.system.mapper.SysRoleMapper;
import com.mro.system.service.MenuService;
import org.apache.dubbo.config.annotation.DubboService;
import org.apache.dubbo.rpc.RpcContext;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@DubboService
public class MenuDubboServiceImpl implements MenuDubboService {

    @Autowired
    private MenuService menuService;

    @Autowired
    private SysRoleMapper roleMapper;

    @Override
    public List<MenuTreeDTO> getMenuTree() {
        return menuService.getAllMenuTree();
    }

    @Override
    public UserMenuResult getUserMenuAndPermissions(Long userId) {
        boolean isAdmin = roleMapper.selectRolesByUserId(userId).stream()
                .anyMatch(r -> "admin".equals(r.getRoleKey()));
        return menuService.getUserMenus(userId, isAdmin);
    }

    @Override
    public Long createMenu(CreateMenuCommand cmd) {
        Long operatorId = getOperatorId();
        return menuService.createMenu(cmd, operatorId);
    }

    @Override
    public void updateMenu(UpdateMenuCommand cmd) {
        Long operatorId = getOperatorId();
        menuService.updateMenu(cmd, operatorId);
    }

    @Override
    public void deleteMenu(Long menuId) {
        menuService.deleteMenu(menuId);
    }

    @Override
    public List<Long> getMenuIdsByRoleId(Long roleId) {
        return menuService.getMenuIdsByRoleId(roleId);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private Long getOperatorId() {
        String v = RpcContext.getServiceContext().getAttachment("userId");
        return v != null ? Long.parseLong(v) : 1L;
    }
}

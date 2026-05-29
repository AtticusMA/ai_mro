package com.mro.common.dubbo.system.service;

import com.mro.common.dubbo.system.request.CreateMenuCommand;
import com.mro.common.dubbo.system.request.UpdateMenuCommand;
import com.mro.common.dubbo.system.response.MenuTreeDTO;
import com.mro.common.dubbo.system.response.UserMenuResult;

import java.util.List;

public interface MenuDubboService {

    List<MenuTreeDTO> getMenuTree();

    UserMenuResult getUserMenuAndPermissions(Long userId);

    Long createMenu(CreateMenuCommand cmd);

    void updateMenu(UpdateMenuCommand cmd);

    void deleteMenu(Long menuId);

    List<Long> getMenuIdsByRoleId(Long roleId);
}

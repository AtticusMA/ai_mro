package com.mro.web.module.sys.app;

import com.mro.common.dubbo.common.request.*;
import com.mro.common.dubbo.system.request.*;
import com.mro.common.dubbo.system.response.*;
import com.mro.common.dubbo.system.service.MenuDubboService;
import com.mro.web.context.UserContext;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MenuAppService {

    @DubboReference(version = "1.0.0", timeout = 5000, retries = 0)
    private MenuDubboService menuDubboService;

    public List<MenuTreeDTO> getMenuTree() {
        return menuDubboService.getMenuTree();
    }

    public UserMenuResult getUserMenuAndPermissions() {
        return menuDubboService.getUserMenuAndPermissions(UserContext.getUserId());
    }

    public void createMenu(CreateMenuCommand cmd) {
        menuDubboService.createMenu(cmd);
    }

    public void updateMenu(UpdateMenuCommand cmd) {
        menuDubboService.updateMenu(cmd);
    }

    public void deleteMenu(Long id) {
        menuDubboService.deleteMenu(id);
    }
}

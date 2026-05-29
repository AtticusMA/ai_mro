package com.mro.web.module.sys.controller;

import com.mro.common.core.response.R;
import com.mro.common.dubbo.common.request.*;
import com.mro.common.dubbo.system.request.*;
import com.mro.common.dubbo.system.response.*;
import com.mro.web.module.sys.app.MenuAppService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/system/menu")
@Validated
public class MenuController {

    @Autowired
    private MenuAppService menuAppService;

    @GetMapping("/tree")
    public R<List<MenuTreeDTO>> getMenuTree() {
        return R.ok(menuAppService.getMenuTree());
    }

    @GetMapping("/user")
    public R<UserMenuResult> getUserMenuAndPermissions() {
        return R.ok(menuAppService.getUserMenuAndPermissions());
    }

    @PostMapping("/")
    public R<Void> createMenu(@Valid @RequestBody CreateMenuCommand cmd) {
        menuAppService.createMenu(cmd);
        return R.ok();
    }

    @PutMapping("/")
    public R<Void> updateMenu(@Valid @RequestBody UpdateMenuCommand cmd) {
        menuAppService.updateMenu(cmd);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    public R<Void> deleteMenu(@PathVariable Long id) {
        menuAppService.deleteMenu(id);
        return R.ok();
    }
}

package com.mro.web.module.sys.controller;

import com.mro.common.core.response.PageResult;
import com.mro.common.core.response.R;
import com.mro.common.dubbo.common.request.*;
import com.mro.common.dubbo.system.request.*;
import com.mro.common.dubbo.system.response.*;
import com.mro.web.module.sys.app.RoleAppService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/system/role")
@Validated
public class RoleController {

    @Autowired
    private RoleAppService roleAppService;

    @GetMapping("/page")
    public R<PageResult<RoleDTO>> listRoles(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {

        RoleQueryParam param = new RoleQueryParam(pageNum, pageSize, keyword, status);
        return R.ok(roleAppService.listRoles(param));
    }

    @GetMapping("/{id}")
    public R<RoleDetailDTO> getRoleById(@PathVariable Long id) {
        return R.ok(roleAppService.getRoleById(id));
    }

    @PostMapping("/")
    public R<Void> createRole(@Valid @RequestBody CreateRoleCommand cmd) {
        roleAppService.createRole(cmd);
        return R.ok();
    }

    @PutMapping("/")
    public R<Void> updateRole(@Valid @RequestBody UpdateRoleCommand cmd) {
        roleAppService.updateRole(cmd);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    public R<Void> deleteRole(@PathVariable Long id) {
        roleAppService.deleteRole(id);
        return R.ok();
    }

    @PutMapping("/{id}/menus")
    public R<Void> assignMenus(@PathVariable Long id, @RequestBody List<Long> menuIds) {
        roleAppService.assignMenus(id, menuIds);
        return R.ok();
    }

    @PutMapping("/{id}/depts")
    public R<Void> assignDepts(@PathVariable Long id, @RequestBody List<Long> deptIds) {
        roleAppService.assignDepts(id, deptIds);
        return R.ok();
    }
}

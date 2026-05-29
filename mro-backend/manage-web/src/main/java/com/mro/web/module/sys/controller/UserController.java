package com.mro.web.module.sys.controller;

import com.mro.common.core.response.PageResult;
import com.mro.common.core.response.R;
import com.mro.common.dubbo.common.request.*;
import com.mro.common.dubbo.system.request.*;
import com.mro.common.dubbo.system.response.*;
import com.mro.web.module.sys.app.UserAppService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/system/user")
@Validated
public class UserController {

    @Autowired
    private UserAppService userAppService;

    @GetMapping("/page")
    public R<PageResult<UserDTO>> listUsers(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long deptId,
            @RequestParam(required = false) Integer status) {

        UserQueryParam param = new UserQueryParam(pageNum, pageSize, keyword, deptId, status);
        return R.ok(userAppService.listUsers(param));
    }

    @GetMapping("/{id}")
    public R<UserDTO> getUserById(@PathVariable Long id) {
        return R.ok(userAppService.getUserById(id));
    }

    @PostMapping("/")
    public R<Void> createUser(@Valid @RequestBody CreateUserCommand cmd) {
        userAppService.createUser(cmd);
        return R.ok();
    }

    @PutMapping("/")
    public R<Void> updateUser(@Valid @RequestBody UpdateUserCommand cmd) {
        userAppService.updateUser(cmd);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    public R<Void> deleteUser(@PathVariable Long id) {
        userAppService.deleteUser(id);
        return R.ok();
    }

    @PutMapping("/{id}/password")
    public R<Void> resetPassword(@PathVariable Long id, @RequestParam String newPassword) {
        userAppService.resetPassword(id, newPassword);
        return R.ok();
    }

    @PutMapping("/{id}/roles")
    public R<Void> assignRoles(@PathVariable Long id, @RequestBody List<Long> roleIds) {
        userAppService.assignRoles(id, roleIds);
        return R.ok();
    }
}

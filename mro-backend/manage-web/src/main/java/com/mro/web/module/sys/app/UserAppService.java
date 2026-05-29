package com.mro.web.module.sys.app;

import com.mro.common.core.response.PageResult;
import com.mro.common.dubbo.common.request.*;
import com.mro.common.dubbo.system.request.*;
import com.mro.common.dubbo.system.response.*;
import com.mro.common.dubbo.system.service.UserDubboService;
import com.mro.web.annotation.DataScope;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserAppService {

    @DubboReference(version = "1.0.0", timeout = 5000, retries = 0)
    private UserDubboService userDubboService;

    @DataScope
    public PageResult<UserDTO> listUsers(UserQueryParam param) {
        return userDubboService.listUsers(param, null);
    }

    public UserDTO getUserById(Long id) {
        return userDubboService.getUserById(id);
    }

    public void createUser(CreateUserCommand cmd) {
        userDubboService.createUser(cmd);
    }

    public void updateUser(UpdateUserCommand cmd) {
        userDubboService.updateUser(cmd);
    }

    public void deleteUser(Long id) {
        userDubboService.deleteUser(id);
    }

    public void resetPassword(Long id, String newPassword) {
        userDubboService.resetPassword(id, newPassword);
    }

    public void assignRoles(Long id, List<Long> roleIds) {
        userDubboService.assignRoles(id, roleIds);
    }
}

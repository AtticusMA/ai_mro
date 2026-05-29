package com.mro.system.dubbo;

import com.mro.common.core.response.PageResult;
import com.mro.common.dubbo.system.request.CreateUserCommand;
import com.mro.common.dubbo.system.request.UpdateUserCommand;
import com.mro.common.dubbo.common.request.UserContextDTO;
import com.mro.common.dubbo.system.response.UserDTO;
import com.mro.common.dubbo.system.request.UserQueryParam;
import com.mro.common.dubbo.system.request.*;
import com.mro.common.dubbo.system.response.*;
import com.mro.common.dubbo.system.service.UserDubboService;
import com.mro.system.service.UserService;
import org.apache.dubbo.config.annotation.DubboService;
import org.apache.dubbo.rpc.RpcContext;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@DubboService
public class UserDubboServiceImpl implements UserDubboService {

    @Autowired
    private UserService userService;

    @Override
    public PageResult<UserDTO> listUsers(UserQueryParam param, UserContextDTO ctx) {
        return userService.listUsers(param, ctx);
    }

    @Override
    public UserDTO getUserById(Long userId) {
        return userService.getUserById(userId);
    }

    @Override
    public UserDTO getUserByUsername(String username) {
        return userService.getUserByUsername(username);
    }

    @Override
    public Long createUser(CreateUserCommand cmd) {
        Long operatorId = getOperatorId();
        Long operatorDeptId = getOperatorDeptId();
        userService.createUser(cmd, operatorId, operatorDeptId);
        return 0L;
    }

    @Override
    public void updateUser(UpdateUserCommand cmd) {
        Long operatorId = getOperatorId();
        userService.updateUser(cmd, operatorId);
    }

    @Override
    public void deleteUser(Long userId) {
        Long operatorId = getOperatorId();
        userService.deleteUser(userId, operatorId);
    }

    @Override
    public void resetPassword(Long userId, String newPassword) {
        Long operatorId = getOperatorId();
        userService.resetPassword(userId, newPassword, operatorId);
    }

    @Override
    public void assignRoles(Long userId, List<Long> roleIds) {
        userService.assignRoles(userId, roleIds);
    }

    @Override
    public List<String> getUserPermissions(Long userId) {
        return userService.getUserPermissions(userId);
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

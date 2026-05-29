package com.mro.common.dubbo.system.service;

import com.mro.common.core.response.PageResult;
import com.mro.common.dubbo.common.request.UserContextDTO;
import com.mro.common.dubbo.system.request.CreateUserCommand;
import com.mro.common.dubbo.system.request.UpdateUserCommand;
import com.mro.common.dubbo.system.request.UserQueryParam;
import com.mro.common.dubbo.system.response.UserDTO;

import java.util.List;

public interface UserDubboService {

    PageResult<UserDTO> listUsers(UserQueryParam param, UserContextDTO ctx);

    UserDTO getUserById(Long userId);

    UserDTO getUserByUsername(String username);

    Long createUser(CreateUserCommand cmd);

    void updateUser(UpdateUserCommand cmd);

    void deleteUser(Long userId);

    void resetPassword(Long userId, String newPassword);

    void assignRoles(Long userId, List<Long> roleIds);

    List<String> getUserPermissions(Long userId);
}

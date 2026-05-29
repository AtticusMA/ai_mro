package com.mro.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mro.common.core.constant.ErrorCode;
import com.mro.common.core.exception.BizException;
import com.mro.common.core.response.PageResult;
import com.mro.common.dubbo.system.request.CreateUserCommand;
import com.mro.common.dubbo.system.request.UpdateUserCommand;
import com.mro.common.dubbo.common.request.UserContextDTO;
import com.mro.common.dubbo.system.response.UserDTO;
import com.mro.common.dubbo.system.request.UserQueryParam;
import com.mro.system.context.DataScopeContext;
import com.mro.system.entity.SysRole;
import com.mro.system.entity.SysRoleDept;
import com.mro.system.entity.SysUser;
import com.mro.system.entity.SysUserRole;
import com.mro.system.mapper.SysDeptMapper;
import com.mro.system.mapper.SysMenuMapper;
import com.mro.system.mapper.SysRoleDeptMapper;
import com.mro.system.mapper.SysRoleMapper;
import com.mro.system.mapper.SysUserMapper;
import com.mro.system.mapper.SysUserRoleMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private static final String PERM_CACHE_PREFIX = "sys:user:permissions:";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private SysUserMapper userMapper;

    @Autowired
    private SysRoleMapper roleMapper;

    @Autowired
    private SysUserRoleMapper userRoleMapper;

    @Autowired
    private SysDeptMapper deptMapper;

    @Autowired
    private SysRoleDeptMapper roleDeptMapper;

    @Autowired
    private SysMenuMapper menuMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Paginate users with data-scope filtering and optional keyword / deptId / status filters.
     */
    public PageResult<UserDTO> listUsers(UserQueryParam param, UserContextDTO ctx) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getIsDeleted, 0);

        if (StringUtils.hasText(param.keyword())) {
            wrapper.and(w -> w
                    .like(SysUser::getUsername, param.keyword())
                    .or()
                    .like(SysUser::getRealName, param.keyword()));
        }
        if (param.deptId() != null) {
            wrapper.eq(SysUser::getDeptId, param.deptId());
        }
        if (param.status() != null) {
            wrapper.eq(SysUser::getStatus, param.status());
        }

        // 从 DataScopeContext 读取数据权限过滤条件（由 DataScopeDubboFilter 注入）
        applyDataScopeFromContext(wrapper);

        wrapper.orderByDesc(SysUser::getCreateTime);

        Page<SysUser> page = new Page<>(param.pageNum(), param.pageSize());
        Page<SysUser> result = userMapper.selectPage(page, wrapper);
        List<UserDTO> records = result.getRecords().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        return new PageResult<>(records, result.getTotal(), param.pageNum(), param.pageSize());
    }

    /**
     * Fetch a single user by id. Throws BizException(4100) if not found.
     */
    public UserDTO getUserById(Long id) {
        SysUser user = userMapper.selectById(id);
        if (user == null || user.getIsDeleted() == 1) {
            throw new BizException(ErrorCode.SYS_USER_NOT_FOUND, "用户不存在");
        }
        return mapToDTO(user);
    }

    /**
     * Create a new user. Validates username / phone / employeeNo uniqueness,
     * encodes password, inserts user record and role bindings.
     */
    @Transactional(rollbackFor = Exception.class)
    public void createUser(CreateUserCommand cmd, Long operatorId, Long operatorDeptId) {
        // Username uniqueness
        Long usernameCount = userMapper.selectCount(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, cmd.username())
                        .eq(SysUser::getIsDeleted, 0));
        if (usernameCount > 0) {
            throw new BizException(ErrorCode.SYS_USERNAME_EXISTS, "用户名已存在");
        }

        // Phone uniqueness
        if (StringUtils.hasText(cmd.phone())) {
            Long phoneCount = userMapper.selectCount(
                    new LambdaQueryWrapper<SysUser>()
                            .eq(SysUser::getPhone, cmd.phone())
                            .eq(SysUser::getIsDeleted, 0));
            if (phoneCount > 0) {
                throw new BizException(ErrorCode.SYS_USER_PHONE_EXISTS, "手机号已存在");
            }
        }

        // Employee number uniqueness
        if (StringUtils.hasText(cmd.employeeNo())) {
            Long empCount = userMapper.selectCount(
                    new LambdaQueryWrapper<SysUser>()
                            .eq(SysUser::getEmployeeNo, cmd.employeeNo())
                            .eq(SysUser::getIsDeleted, 0));
            if (empCount > 0) {
                throw new BizException(ErrorCode.SYS_USER_EMPNO_EXISTS, "工号已存在");
            }
        }

        SysUser user = new SysUser();
        user.setUsername(cmd.username());
        user.setPassword(passwordEncoder.encode(cmd.password()));
        user.setRealName(cmd.realName());
        user.setEmployeeNo(cmd.employeeNo());
        user.setGender(cmd.gender());
        user.setPhone(cmd.phone());
        user.setEmail(cmd.email());
        user.setDeptId(cmd.deptId());
        user.setStatus(cmd.status() == null ? 0 : cmd.status());
        user.setIsDeleted(0);
        user.setCreateUserId(operatorId);
        user.setCreateDeptId(operatorDeptId);
        user.setCreateTime(LocalDateTime.now());
        userMapper.insert(user);

        insertUserRoles(user.getId(), cmd.roleIds());
    }

    /**
     * Update an existing user's profile fields and role bindings.
     * Evicts the permission cache for the affected user.
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(UpdateUserCommand cmd, Long operatorId) {
        SysUser user = userMapper.selectById(cmd.id());
        if (user == null || user.getIsDeleted() == 1) {
            throw new BizException(ErrorCode.SYS_USER_NOT_FOUND, "用户不存在");
        }

        user.setRealName(cmd.realName());
        user.setEmployeeNo(cmd.employeeNo());
        user.setGender(cmd.gender());
        user.setPhone(cmd.phone());
        user.setEmail(cmd.email());
        user.setAvatar(cmd.avatar());
        user.setDeptId(cmd.deptId());
        if (cmd.status() != null) {
            user.setStatus(cmd.status());
        }
        user.setUpdateUserId(operatorId);
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);

        // Replace role bindings
        userRoleMapper.delete(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, cmd.id()));
        insertUserRoles(cmd.id(), cmd.roleIds());

        evictPermissionCache(cmd.id());
    }

    /**
     * Soft-delete a user. Operators cannot delete themselves.
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long id, Long operatorId) {
        if (id.equals(operatorId)) {
            throw new BizException(ErrorCode.SYS_USER_CANNOT_DEL_SELF, "不能删除自己");
        }

        SysUser user = userMapper.selectById(id);
        if (user == null || user.getIsDeleted() == 1) {
            throw new BizException(ErrorCode.SYS_USER_NOT_FOUND, "用户不存在");
        }

        user.setIsDeleted(1);
        user.setUpdateUserId(operatorId);
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);

        evictPermissionCache(id);
    }

    /**
     * Reset a user's password to the provided plain-text value.
     */
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(Long id, String newPassword, Long operatorId) {
        SysUser user = userMapper.selectById(id);
        if (user == null || user.getIsDeleted() == 1) {
            throw new BizException(ErrorCode.SYS_USER_NOT_FOUND, "用户不存在");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdateUserId(operatorId);
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);

        evictPermissionCache(id);
    }

    /**
     * Toggle a user's enabled/disabled status.
     */
    @Transactional(rollbackFor = Exception.class)
    public void toggleStatus(Long id, Integer status, Long operatorId) {
        SysUser user = userMapper.selectById(id);
        if (user == null || user.getIsDeleted() == 1) {
            throw new BizException(ErrorCode.SYS_USER_NOT_FOUND, "用户不存在");
        }

        user.setStatus(status);
        user.setUpdateUserId(operatorId);
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
    }

    /**
     * Full user info lookup — intended for Dubbo consumers. Same as getUserById.
     */
    public UserDTO getUserFullInfo(Long userId) {
        return getUserById(userId);
    }

    public UserDTO getUserByUsername(String username) {
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username).eq(SysUser::getIsDeleted, 0));
        if (user == null) {
            throw new BizException(ErrorCode.SYS_USER_NOT_FOUND, "用户不存在");
        }
        return mapToDTO(user);
    }

    @Transactional(rollbackFor = Exception.class)
    public void assignRoles(Long userId, List<Long> roleIds) {
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
        insertUserRoles(userId, roleIds);
        evictPermissionCache(userId);
    }

    public List<String> getUserPermissions(Long userId) {
        String cacheKey = PERM_CACHE_PREFIX + userId;
        String cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            try {
                return objectMapper.readValue(cached, new com.fasterxml.jackson.core.type.TypeReference<List<String>>() {});
            } catch (Exception ignored) {}
        }
        List<SysRole> roles = roleMapper.selectRolesByUserId(userId);
        boolean isAdmin = roles.stream().anyMatch(r -> "admin".equals(r.getRoleKey()));
        List<String> perms;
        if (isAdmin) {
            perms = List.of("*:*:*");
        } else {
            perms = menuMapper.selectPermsByUserId(userId);
        }
        try {
            redisTemplate.opsForValue().set(cacheKey,
                    objectMapper.writeValueAsString(perms), 30, java.util.concurrent.TimeUnit.MINUTES);
        } catch (Exception ignored) {}
        return perms;
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private void evictPermissionCache(Long userId) {
        redisTemplate.delete(PERM_CACHE_PREFIX + userId);
    }

    /**
     * Map a SysUser entity to UserDTO, fetching deptName and role keys.
     */
    private UserDTO mapToDTO(SysUser user) {
        String deptName = deptMapper.selectById(user.getDeptId()) != null
                ? deptMapper.selectById(user.getDeptId()).getDeptName()
                : null;

        List<String> roles = roleMapper.selectRolesByUserId(user.getId())
                .stream()
                .map(SysRole::getRoleKey)
                .collect(Collectors.toList());

        String createTime = user.getCreateTime() != null
                ? user.getCreateTime().format(DATE_FMT)
                : null;

        return new UserDTO(
                user.getId(),
                user.getUsername(),
                user.getRealName(),
                user.getEmployeeNo(),
                user.getGender(),
                user.getPhone(),
                user.getEmail(),
                user.getAvatar(),
                user.getDeptId(),
                deptName,
                user.getStatus(),
                createTime,
                roles);
    }

    private void applyDataScopeFromContext(LambdaQueryWrapper<SysUser> wrapper) {
        if (DataScopeContext.isSelfOnly()) {
            Long userId = DataScopeContext.getUserId();
            if (userId != null) {
                wrapper.eq(SysUser::getCreateUserId, userId);
            }
            return;
        }
        List<Long> deptIds = DataScopeContext.getDeptIds();
        if (deptIds != null) {
            if (deptIds.isEmpty()) {
                // 无任何可见部门，强制返回空
                wrapper.eq(SysUser::getId, -1L);
            } else {
                wrapper.in(SysUser::getDeptId, deptIds);
            }
        }
        // deptIds == null → dataScope=1，不加条件
    }

    /**
     * Batch-insert user-role binding records.
     */
    private void insertUserRoles(Long userId, List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return;
        }
        for (Long roleId : roleIds) {
            SysUserRole ur = new SysUserRole();
            ur.setUserId(userId);
            ur.setRoleId(roleId);
            userRoleMapper.insert(ur);
        }
    }
}

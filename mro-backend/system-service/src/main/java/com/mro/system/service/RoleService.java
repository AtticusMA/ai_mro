package com.mro.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mro.common.core.constant.ErrorCode;
import com.mro.common.core.exception.BizException;
import com.mro.common.core.response.PageResult;
import com.mro.common.dubbo.system.request.CreateRoleCommand;
import com.mro.common.dubbo.system.response.RoleDTO;
import com.mro.common.dubbo.system.response.RoleDetailDTO;
import com.mro.common.dubbo.system.request.RoleQueryParam;
import com.mro.common.dubbo.system.request.UpdateRoleCommand;
import com.mro.common.dubbo.system.response.UserDataScopeDTO;
import com.mro.system.entity.SysRole;
import com.mro.system.entity.SysRoleDept;
import com.mro.system.entity.SysRoleMenu;
import com.mro.system.entity.SysUserRole;
import com.mro.system.mapper.SysRoleDeptMapper;
import com.mro.system.mapper.SysRoleMapper;
import com.mro.system.mapper.SysRoleMenuMapper;
import com.mro.system.mapper.SysUserRoleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoleService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired private SysRoleMapper roleMapper;
    @Autowired private SysUserRoleMapper userRoleMapper;
    @Autowired private SysRoleMenuMapper roleMenuMapper;
    @Autowired private SysRoleDeptMapper roleDeptMapper;
    @Autowired private StringRedisTemplate redisTemplate;

    public PageResult<RoleDTO> listRoles(RoleQueryParam param) {
        Page<SysRole> page = new Page<>(param.pageNum(), param.pageSize());
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getIsDeleted, 0);
        if (StringUtils.hasText(param.keyword())) {
            wrapper.and(w -> w.like(SysRole::getRoleName, param.keyword())
                    .or().like(SysRole::getRoleKey, param.keyword()));
        }
        if (param.status() != null) {
            wrapper.eq(SysRole::getStatus, param.status());
        }
        wrapper.orderByAsc(SysRole::getRoleSort);

        Page<SysRole> result = roleMapper.selectPage(page, wrapper);
        List<RoleDTO> records = result.getRecords().stream()
                .map(this::toRoleDTO).collect(Collectors.toList());
        return new PageResult<>(records, result.getTotal(), param.pageNum(), param.pageSize());
    }

    public RoleDetailDTO getRoleById(Long id) {
        SysRole role = roleMapper.selectById(id);
        if (role == null || role.getIsDeleted() == 1) {
            throw new BizException(ErrorCode.SYS_ROLE_NOT_FOUND, "角色不存在");
        }
        List<Long> menuIds = roleMenuMapper.selectList(
                new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, id))
                .stream().map(SysRoleMenu::getMenuId).collect(Collectors.toList());
        return new RoleDetailDTO(role.getId(), role.getRoleName(), role.getRoleKey(),
                role.getRoleSort(), role.getDataScope(), role.getStatus(), menuIds);
    }

    @Transactional(rollbackFor = Exception.class)
    public Long createRole(CreateRoleCommand cmd, Long operatorId, Long operatorDeptId) {
        Long count = roleMapper.selectCount(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getRoleKey, cmd.roleKey()).eq(SysRole::getIsDeleted, 0));
        if (count > 0) {
            throw new BizException(ErrorCode.SYS_ROLE_KEY_DUPLICATE, "角色标识已存在");
        }
        SysRole role = new SysRole();
        role.setRoleName(cmd.roleName());
        role.setRoleKey(cmd.roleKey());
        role.setRoleSort(cmd.roleSort());
        role.setDataScope(cmd.dataScope() == null ? 1 : cmd.dataScope());
        role.setStatus(cmd.status() == null ? 0 : cmd.status());
        role.setIsDeleted(0);
        role.setCreateUserId(operatorId);
        role.setCreateDeptId(operatorDeptId);
        role.setCreateTime(LocalDateTime.now());
        roleMapper.insert(role);
        insertRoleMenus(role.getId(), cmd.menuIds());
        return role.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateRole(UpdateRoleCommand cmd, Long operatorId) {
        SysRole role = roleMapper.selectById(cmd.id());
        if (role == null || role.getIsDeleted() == 1) {
            throw new BizException(ErrorCode.SYS_ROLE_NOT_FOUND, "角色不存在");
        }
        role.setRoleName(cmd.roleName());
        role.setRoleSort(cmd.roleSort());
        if (cmd.dataScope() != null) role.setDataScope(cmd.dataScope());
        if (cmd.status() != null) role.setStatus(cmd.status());
        role.setUpdateUserId(operatorId);
        role.setUpdateTime(LocalDateTime.now());
        roleMapper.updateById(role);

        roleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, cmd.id()));
        insertRoleMenus(cmd.id(), cmd.menuIds());

        evictRoleUsersPermCache(cmd.id());
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteRole(Long id) {
        Long userCount = userRoleMapper.selectCount(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getRoleId, id));
        if (userCount > 0) {
            throw new BizException(ErrorCode.SYS_ROLE_HAS_USER, "角色下存在用户，不能删除");
        }
        roleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, id));
        roleDeptMapper.delete(new LambdaQueryWrapper<SysRoleDept>().eq(SysRoleDept::getRoleId, id));
        SysRole role = new SysRole();
        role.setId(id);
        role.setIsDeleted(1);
        roleMapper.updateById(role);
    }

    public void assignMenus(Long roleId, List<Long> menuIds) {
        SysRole role = roleMapper.selectById(roleId);
        if (role == null || role.getIsDeleted() == 1) {
            throw new BizException(ErrorCode.SYS_ROLE_NOT_FOUND, "角色不存在");
        }
        roleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, roleId));
        insertRoleMenus(roleId, menuIds);
        evictRoleUsersPermCache(roleId);
    }

    public void assignDepts(Long roleId, List<Long> deptIds) {
        SysRole role = roleMapper.selectById(roleId);
        if (role == null || role.getIsDeleted() == 1) {
            throw new BizException(ErrorCode.SYS_ROLE_NOT_FOUND, "角色不存在");
        }
        roleDeptMapper.delete(new LambdaQueryWrapper<SysRoleDept>().eq(SysRoleDept::getRoleId, roleId));
        if (deptIds != null) {
            for (Long deptId : deptIds) {
                SysRoleDept rd = new SysRoleDept();
                rd.setRoleId(roleId);
                rd.setDeptId(deptId);
                roleDeptMapper.insert(rd);
            }
        }
    }

    public List<RoleDTO> getRolesByUserId(Long userId) {
        return roleMapper.selectRolesByUserId(userId).stream()
                .map(this::toRoleDTO).collect(Collectors.toList());
    }

    public UserDataScopeDTO getDataScopeByUserId(Long userId) {
        List<SysRole> roles = roleMapper.selectRolesByUserId(userId);
        if (roles == null || roles.isEmpty()) {
            return new UserDataScopeDTO(4, List.of());
        }
        // admin 角色直接返回全部数据
        boolean isAdmin = roles.stream().anyMatch(r -> "admin".equals(r.getRoleKey()));
        if (isAdmin) {
            return new UserDataScopeDTO(1, List.of());
        }
        // 取最宽松（最小值）的 dataScope
        int effectiveScope = roles.stream()
                .map(SysRole::getDataScope)
                .filter(ds -> ds != null)
                .mapToInt(Integer::intValue)
                .min()
                .orElse(4);

        List<Long> customDeptIds = List.of();
        if (effectiveScope == 5) {
            // 汇总所有角色的自定义部门
            List<Long> roleIds = roles.stream().map(SysRole::getId).collect(Collectors.toList());
            customDeptIds = roleDeptMapper.selectList(
                            new LambdaQueryWrapper<SysRoleDept>().in(SysRoleDept::getRoleId, roleIds))
                    .stream()
                    .map(SysRoleDept::getDeptId)
                    .distinct()
                    .collect(Collectors.toList());
        }
        return new UserDataScopeDTO(effectiveScope, customDeptIds);
    }

    private void evictRoleUsersPermCache(Long roleId) {
        userRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getRoleId, roleId))
                .stream().map(SysUserRole::getUserId)
                .forEach(uid -> redisTemplate.delete("sys:user:permissions:" + uid));
    }

    private void insertRoleMenus(Long roleId, List<Long> menuIds) {
        if (menuIds == null || menuIds.isEmpty()) return;
        for (Long menuId : menuIds) {
            SysRoleMenu rm = new SysRoleMenu();
            rm.setRoleId(roleId);
            rm.setMenuId(menuId);
            roleMenuMapper.insert(rm);
        }
    }

    private RoleDTO toRoleDTO(SysRole role) {
        String ct = role.getCreateTime() != null ? role.getCreateTime().format(DATE_FMT) : null;
        return new RoleDTO(role.getId(), role.getRoleName(), role.getRoleKey(),
                role.getRoleSort(), role.getDataScope(), role.getStatus(), ct);
    }
}

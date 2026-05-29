package com.mro.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mro.auth.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    @Select("SELECT rm.perms FROM sys_role r " +
            "JOIN sys_user_role ur ON ur.role_id = r.id " +
            "JOIN sys_role_menu rm ON rm.role_id = r.id " +
            "WHERE ur.user_id = #{userId} AND r.status = 1 AND rm.perms IS NOT NULL AND rm.perms != ''")
    List<String> selectPermsByUserId(Long userId);

    @Select("SELECT r.role_key FROM sys_role r " +
            "JOIN sys_user_role ur ON ur.role_id = r.id " +
            "WHERE ur.user_id = #{userId} AND r.status = 1")
    List<String> selectRoleKeysByUserId(Long userId);
}

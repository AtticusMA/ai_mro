package com.mro.system.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mro.system.entity.SysMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface SysMenuMapper extends BaseMapper<SysMenu> {
    @Select("SELECT m.* FROM sys_menu m JOIN sys_role_menu rm ON rm.menu_id = m.id JOIN sys_user_role ur ON ur.role_id = rm.role_id WHERE ur.user_id = #{userId} AND m.status = 1 GROUP BY m.id ORDER BY m.order_num")
    List<SysMenu> selectMenusByUserId(Long userId);

    @Select("SELECT perms FROM sys_menu m JOIN sys_role_menu rm ON rm.menu_id = m.id JOIN sys_user_role ur ON ur.role_id = rm.role_id WHERE ur.user_id = #{userId} AND m.menu_type = 'F' AND m.status = 1 AND m.perms IS NOT NULL AND m.perms != ''")
    List<String> selectPermsByUserId(Long userId);

    @Select("SELECT menu_id FROM sys_role_menu WHERE role_id = #{roleId}")
    List<Long> selectMenuIdsByRoleId(Long roleId);

    @Select("SELECT COUNT(1) FROM sys_menu WHERE parent_id = #{menuId}")
    int countChildren(Long menuId);
}

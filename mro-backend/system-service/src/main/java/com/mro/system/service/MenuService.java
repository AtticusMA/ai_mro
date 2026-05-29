package com.mro.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mro.common.core.constant.ErrorCode;
import com.mro.common.core.exception.BizException;
import com.mro.common.dubbo.system.request.CreateMenuCommand;
import com.mro.common.dubbo.system.response.MenuTreeDTO;
import com.mro.common.dubbo.system.request.UpdateMenuCommand;
import com.mro.common.dubbo.system.response.UserMenuResult;
import com.mro.system.entity.SysMenu;
import com.mro.system.entity.SysRoleMenu;
import com.mro.system.mapper.SysMenuMapper;
import com.mro.system.mapper.SysRoleMenuMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class MenuService {

    private static final Logger log = LoggerFactory.getLogger(MenuService.class);
    private static final String USER_MENU_CACHE_PREFIX = "sys:user:menu:";
    private static final long MENU_CACHE_TTL_MINUTES = 30L;

    @Autowired private SysMenuMapper menuMapper;
    @Autowired private SysRoleMenuMapper roleMenuMapper;
    @Autowired private StringRedisTemplate redisTemplate;
    @Autowired private ObjectMapper objectMapper;

    public List<MenuTreeDTO> getAllMenuTree() {
        List<SysMenu> all = menuMapper.selectList(
                new LambdaQueryWrapper<SysMenu>().orderByAsc(SysMenu::getOrderNum));
        return buildMenuTree(all, 0L);
    }

    public UserMenuResult getUserMenus(Long userId, boolean isAdmin) {
        String cacheKey = USER_MENU_CACHE_PREFIX + userId;
        String cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            try {
                return objectMapper.readValue(cached, UserMenuResult.class);
            } catch (Exception e) {
                log.warn("Failed to deserialize user menu cache for userId={}", userId, e);
            }
        }

        UserMenuResult result;
        if (isAdmin) {
            List<SysMenu> all = menuMapper.selectList(
                    new LambdaQueryWrapper<SysMenu>().orderByAsc(SysMenu::getOrderNum));
            result = new UserMenuResult(buildMenuTree(all, 0L), List.of("*:*:*"));
        } else {
            List<SysMenu> userMenus = menuMapper.selectMenusByUserId(userId);
            List<String> perms = menuMapper.selectPermsByUserId(userId);
            result = new UserMenuResult(buildMenuTree(userMenus, 0L), perms);
        }

        try {
            String json = objectMapper.writeValueAsString(result);
            redisTemplate.opsForValue().set(cacheKey, json, MENU_CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.warn("Failed to cache user menus for userId={}", userId, e);
        }
        return result;
    }

    public MenuTreeDTO getMenuById(Long id) {
        SysMenu menu = menuMapper.selectById(id);
        if (menu == null) {
            throw new BizException(ErrorCode.SYS_MENU_NOT_FOUND, "菜单不存在");
        }
        return toDTO(menu, new ArrayList<>());
    }

    public List<Long> getMenuIdsByRoleId(Long roleId) {
        return menuMapper.selectMenuIdsByRoleId(roleId);
    }

    @Transactional(rollbackFor = Exception.class)
    public Long createMenu(CreateMenuCommand cmd, Long operatorId) {
        SysMenu menu = new SysMenu();
        menu.setMenuName(cmd.menuName());
        menu.setParentId(cmd.parentId() == null ? 0L : cmd.parentId());
        menu.setOrderNum(cmd.orderNum());
        menu.setPath(cmd.path());
        menu.setComponent(cmd.component());
        menu.setMenuType(cmd.menuType());
        menu.setPerms(cmd.perms());
        menu.setIcon(cmd.icon());
        menu.setVisible(cmd.visible() == null ? 1 : cmd.visible());
        menu.setStatus(cmd.status() == null ? 0 : cmd.status());
        menu.setCreateUserId(operatorId);
        menu.setCreateTime(LocalDateTime.now());
        menuMapper.insert(menu);
        evictAllUserMenuCaches();
        return menu.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateMenu(UpdateMenuCommand cmd, Long operatorId) {
        SysMenu menu = menuMapper.selectById(cmd.id());
        if (menu == null) {
            throw new BizException(ErrorCode.SYS_MENU_NOT_FOUND, "菜单不存在");
        }
        menu.setMenuName(cmd.menuName());
        if (cmd.parentId() != null) menu.setParentId(cmd.parentId());
        if (cmd.orderNum() != null) menu.setOrderNum(cmd.orderNum());
        if (cmd.path() != null) menu.setPath(cmd.path());
        if (cmd.component() != null) menu.setComponent(cmd.component());
        if (cmd.menuType() != null) menu.setMenuType(cmd.menuType());
        if (cmd.perms() != null) menu.setPerms(cmd.perms());
        if (cmd.icon() != null) menu.setIcon(cmd.icon());
        if (cmd.visible() != null) menu.setVisible(cmd.visible());
        if (cmd.status() != null) menu.setStatus(cmd.status());
        menu.setUpdateUserId(operatorId);
        menu.setUpdateTime(LocalDateTime.now());
        menuMapper.updateById(menu);
        evictAllUserMenuCaches();
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteMenu(Long id) {
        int childCount = menuMapper.countChildren(id);
        if (childCount > 0) {
            throw new BizException(ErrorCode.SYS_MENU_HAS_CHILDREN, "存在子菜单，不能删除");
        }
        roleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getMenuId, id));
        menuMapper.deleteById(id);
        evictAllUserMenuCaches();
    }

    public void evictUserMenuCache(Long userId) {
        redisTemplate.delete(USER_MENU_CACHE_PREFIX + userId);
    }

    private List<MenuTreeDTO> buildMenuTree(List<SysMenu> menus, Long parentId) {
        List<MenuTreeDTO> result = new ArrayList<>();
        for (SysMenu menu : menus) {
            Long pid = menu.getParentId() == null ? 0L : menu.getParentId();
            if (parentId.equals(pid)) {
                List<MenuTreeDTO> children = buildMenuTree(menus, menu.getId());
                result.add(toDTO(menu, children));
            }
        }
        result.sort((a, b) -> {
            int oa = a.orderNum() == null ? 0 : a.orderNum();
            int ob = b.orderNum() == null ? 0 : b.orderNum();
            return Integer.compare(oa, ob);
        });
        return result;
    }

    private MenuTreeDTO toDTO(SysMenu m, List<MenuTreeDTO> children) {
        return new MenuTreeDTO(m.getId(), m.getMenuName(), m.getParentId(), m.getOrderNum(),
                m.getPath(), m.getComponent(), m.getMenuType(), m.getPerms(), m.getIcon(),
                m.getVisible(), m.getStatus(), children);
    }

    private void evictAllUserMenuCaches() {
        Set<String> keys = redisTemplate.keys(USER_MENU_CACHE_PREFIX + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }
}

package com.mro.common.dubbo.system.response;
import java.io.Serializable;
import java.util.List;
public record MenuTreeDTO(Long id, String menuName, Long parentId, Integer orderNum, String path, String component, String menuType, String perms, String icon, Integer visible, Integer status, List<MenuTreeDTO> children) implements Serializable {}

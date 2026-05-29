package com.mro.common.dubbo.system.request;
import java.io.Serializable;
public record UpdateMenuCommand(Long id, String menuName, Long parentId, Integer orderNum, String path, String component, String menuType, String perms, String icon, Integer visible, Integer status) implements Serializable {}

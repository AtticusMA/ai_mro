package com.mro.common.dubbo.system.response;
import java.io.Serializable;
import java.util.List;
public record UserMenuResult(List<MenuTreeDTO> menus, List<String> permissions) implements Serializable {}

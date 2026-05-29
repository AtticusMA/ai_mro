package com.mro.common.dubbo.system.request;
import java.io.Serializable;
import java.util.List;
public record CreateRoleCommand(String roleName, String roleKey, Integer roleSort, Integer dataScope, Integer status, List<Long> menuIds) implements Serializable {}

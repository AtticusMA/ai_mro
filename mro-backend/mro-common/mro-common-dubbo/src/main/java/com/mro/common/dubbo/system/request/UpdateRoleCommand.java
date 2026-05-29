package com.mro.common.dubbo.system.request;
import java.io.Serializable;
import java.util.List;
public record UpdateRoleCommand(Long id, String roleName, Integer roleSort, Integer dataScope, Integer status, List<Long> menuIds) implements Serializable {}

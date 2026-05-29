package com.mro.common.dubbo.system.response;
import java.io.Serializable;
import java.util.List;
public record RoleDetailDTO(Long id, String roleName, String roleKey, Integer roleSort, Integer dataScope, Integer status, List<Long> menuIds) implements Serializable {}

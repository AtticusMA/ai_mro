package com.mro.common.dubbo.system.response;
import java.io.Serializable;
public record RoleDTO(Long id, String roleName, String roleKey, Integer roleSort, Integer dataScope, Integer status, String createTime) implements Serializable {}

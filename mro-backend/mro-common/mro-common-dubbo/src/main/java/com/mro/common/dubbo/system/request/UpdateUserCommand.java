package com.mro.common.dubbo.system.request;
import java.io.Serializable;
import java.util.List;
public record UpdateUserCommand(Long id, String realName, String employeeNo, Integer gender, String phone, String email, String avatar, Long deptId, Integer status, List<Long> roleIds) implements Serializable {}

package com.mro.common.dubbo.system.request;
import java.io.Serializable;
import java.util.List;
public record CreateUserCommand(String username, String password, String realName, String employeeNo, Integer gender, String phone, String email, Long deptId, Integer status, List<Long> roleIds) implements Serializable {}

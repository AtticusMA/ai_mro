package com.mro.common.dubbo.system.response;
import java.io.Serializable;
import java.util.List;
public record UserDTO(Long id, String username, String realName, String employeeNo, Integer gender, String phone, String email, String avatar, Long deptId, String deptName, Integer status, String createTime, List<String> roles) implements Serializable {}

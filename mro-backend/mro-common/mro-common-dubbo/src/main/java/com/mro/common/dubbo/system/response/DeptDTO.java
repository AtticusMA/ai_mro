package com.mro.common.dubbo.system.response;
import java.io.Serializable;
public record DeptDTO(Long id, String deptName, String deptCode, Long parentId, String ancestors, Integer orderNum, String leader, String phone, String email, Integer status) implements Serializable {}

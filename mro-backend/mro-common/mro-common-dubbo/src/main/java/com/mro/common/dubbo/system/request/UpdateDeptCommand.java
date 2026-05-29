package com.mro.common.dubbo.system.request;
import java.io.Serializable;
public record UpdateDeptCommand(Long id, String deptName, Long parentId, Integer orderNum, String leader, String phone, String email, Integer status) implements Serializable {}

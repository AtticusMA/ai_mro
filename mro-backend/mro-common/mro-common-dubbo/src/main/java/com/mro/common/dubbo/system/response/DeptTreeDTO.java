package com.mro.common.dubbo.system.response;
import java.io.Serializable;
import java.util.List;
public record DeptTreeDTO(Long id, String deptName, String deptCode, Long parentId, Integer orderNum, Integer status, List<DeptTreeDTO> children) implements Serializable {}

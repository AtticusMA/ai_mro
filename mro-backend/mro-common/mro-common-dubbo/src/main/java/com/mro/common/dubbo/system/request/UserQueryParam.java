package com.mro.common.dubbo.system.request;
import java.io.Serializable;
public record UserQueryParam(int pageNum, int pageSize, String keyword, Long deptId, Integer status) implements Serializable {}

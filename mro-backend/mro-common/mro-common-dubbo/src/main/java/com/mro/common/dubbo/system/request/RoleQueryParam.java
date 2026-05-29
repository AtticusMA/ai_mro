package com.mro.common.dubbo.system.request;
import java.io.Serializable;
public record RoleQueryParam(int pageNum, int pageSize, String keyword, Integer status) implements Serializable {}

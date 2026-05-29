package com.mro.common.dubbo.tool.request;
import java.io.Serializable;
public record AlertQueryParam(int pageNum, int pageSize, String alertType) implements Serializable {}
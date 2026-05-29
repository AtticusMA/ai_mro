package com.mro.common.dubbo.system.request;
import java.io.Serializable;
public record DictQueryParam(int pageNum, int pageSize, String dictGroup, String keyword, Integer status) implements Serializable {}

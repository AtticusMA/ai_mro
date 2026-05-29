package com.mro.common.dubbo.material.request;
import java.io.Serializable;
public record MaterialRequestQueryParam(int pageNum, int pageSize, Long workcardId,
    String status, Long requesterId) implements Serializable {}

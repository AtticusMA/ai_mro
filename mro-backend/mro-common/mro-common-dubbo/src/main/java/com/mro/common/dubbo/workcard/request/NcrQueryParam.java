package com.mro.common.dubbo.workcard.request;

import java.io.Serializable;

public record NcrQueryParam(
    Long workcardId,
    String status,
    int pageNum,
    int pageSize
) implements Serializable {}

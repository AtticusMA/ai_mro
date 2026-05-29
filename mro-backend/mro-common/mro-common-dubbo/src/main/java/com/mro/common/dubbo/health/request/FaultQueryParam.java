package com.mro.common.dubbo.health.request;

import java.io.Serializable;

public record FaultQueryParam(
        int pageNum,
        int pageSize,
        String severity,
        String status
) implements Serializable {}

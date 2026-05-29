package com.mro.common.dubbo.health.request;

import java.io.Serializable;

public record HealthQueryParam(
        int pageNum,
        int pageSize,
        String status,
        String severity,
        String aircraftType
) implements Serializable {}

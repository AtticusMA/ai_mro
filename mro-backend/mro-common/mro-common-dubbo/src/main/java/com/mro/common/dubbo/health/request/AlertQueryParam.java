package com.mro.common.dubbo.health.request;

import java.io.Serializable;

public record AlertQueryParam(
        int pageNum,
        int pageSize,
        String alertLevel,
        Boolean acknowledged,
        String aircraftId
) implements Serializable {}

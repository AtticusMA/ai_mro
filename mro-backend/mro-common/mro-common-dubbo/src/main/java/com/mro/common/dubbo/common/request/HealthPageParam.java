package com.mro.common.dubbo.common.request;

import java.io.Serializable;

public record HealthPageParam(
        int pageNum,
        int pageSize
) implements Serializable {}

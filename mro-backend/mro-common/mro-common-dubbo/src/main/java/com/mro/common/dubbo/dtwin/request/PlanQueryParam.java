package com.mro.common.dubbo.dtwin.request;

import java.io.Serializable;

public record PlanQueryParam(
        Long hangarId,
        String status,
        String planType,
        int pageNum,
        int pageSize
) implements Serializable {}

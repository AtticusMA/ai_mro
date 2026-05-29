package com.mro.common.dubbo.dtwin.request;

import java.io.Serializable;

public record OrderQueryParam(
        Long planId,
        Long workstationId,
        Long assigneeId,
        String status,
        int pageNum,
        int pageSize
) implements Serializable {}

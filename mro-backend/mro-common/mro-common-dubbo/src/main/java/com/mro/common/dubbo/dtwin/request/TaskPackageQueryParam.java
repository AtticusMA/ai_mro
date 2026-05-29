package com.mro.common.dubbo.dtwin.request;

import java.io.Serializable;

public record TaskPackageQueryParam(
        int pageNum,
        int pageSize,
        Long hangarId,
        String status,
        String aircraftType
) implements Serializable {}

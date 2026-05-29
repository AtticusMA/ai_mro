package com.mro.common.dubbo.ar.request;

import java.io.Serializable;

public record InspectionQueryParam(
        int pageNum,
        int pageSize,
        String status,
        String aircraftId
) implements Serializable {}

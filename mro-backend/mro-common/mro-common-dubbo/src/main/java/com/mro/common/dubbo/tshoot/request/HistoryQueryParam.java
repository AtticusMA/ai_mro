package com.mro.common.dubbo.tshoot.request;

import java.io.Serializable;

public record HistoryQueryParam(
        int pageNum,
        int pageSize,
        String aircraftId,
        String faultCode,
        String startDate,
        String endDate
) implements Serializable {}

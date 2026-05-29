package com.mro.common.dubbo.manual.request;

import java.io.Serializable;

public record ManualQueryParam(
        String manualNo,
        String aircraftType,
        String parsedStatus,
        int pageNum,
        int pageSize
) implements Serializable {}

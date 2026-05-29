package com.mro.common.dubbo.manual.request;

import java.io.Serializable;

public record ManualSearchParam(
        String query,
        String aircraftType,
        int pageNum,
        int pageSize
) implements Serializable {}

package com.mro.common.dubbo.dtwin.response;

import java.io.Serializable;

public record WorkstationLoadDTO(
        Long workstationId,
        String workstationName,
        int orderCount,
        double utilizationRate
) implements Serializable {}

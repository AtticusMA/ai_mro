package com.mro.common.dubbo.dtwin.request;

import java.io.Serializable;
import java.time.Instant;

public record UpdatePlanCommand(
        Long id,
        String planType,
        Instant scheduledStart,
        Instant scheduledEnd,
        String status,
        Long operatorId
) implements Serializable {}

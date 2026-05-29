package com.mro.common.dubbo.dtwin.request;

import java.io.Serializable;
import java.time.Instant;

public record CreatePlanCommand(
        Long hangarId,
        String aircraftId,
        String planType,
        Instant scheduledStart,
        Instant scheduledEnd,
        Long operatorId
) implements Serializable {}

package com.mro.common.dubbo.dtwin.response;

import java.io.Serializable;
import java.time.Instant;

public record ProductionPlanDTO(
        Long id,
        Long hangarId,
        String aircraftId,
        String planType,
        Instant scheduledStart,
        Instant scheduledEnd,
        String status
) implements Serializable {}

package com.mro.common.dubbo.ar.response;

import java.io.Serializable;
import java.time.Instant;

public record InspectionTaskDTO(
        Long id,
        String aircraftId,
        Long inspectorId,
        String inspectorName,
        String routeTemplate,
        String status,
        Instant startedAt,
        Instant completedAt
) implements Serializable {}

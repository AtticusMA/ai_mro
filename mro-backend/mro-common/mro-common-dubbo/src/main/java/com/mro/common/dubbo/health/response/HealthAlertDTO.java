package com.mro.common.dubbo.health.response;

import java.io.Serializable;
import java.time.Instant;

public record HealthAlertDTO(
        Long id,
        String aircraftId,
        String alertLevel,
        String message,
        Instant predictedFaultTime,
        boolean acknowledged,
        Instant createdAt
) implements Serializable {}

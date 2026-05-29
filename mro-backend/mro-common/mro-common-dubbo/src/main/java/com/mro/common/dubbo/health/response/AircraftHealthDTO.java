package com.mro.common.dubbo.health.response;

import java.io.Serializable;
import java.time.Instant;

public record AircraftHealthDTO(
        String aircraftId,
        String registrationNo,
        String aircraftType,
        String overallHealth,
        int activeAlerts,
        int activeFaults,
        Instant lastDataAt
) implements Serializable {}

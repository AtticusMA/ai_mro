package com.mro.common.dubbo.health.response;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

public record AircraftDetailDTO(
        String aircraftId,
        String aircraftType,
        String overallHealth,
        Instant latestDataAt,
        List<FaultRecordDTO> activeFaults,
        List<HealthAlertDTO> activeAlerts
) implements Serializable {}

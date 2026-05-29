package com.mro.common.dubbo.health.response;

import java.io.Serializable;
import java.time.Instant;
import java.util.Map;

public record PredictionReportDTO(
        Long id,
        String aircraftId,
        String modelVersion,
        Instant predictedAt,
        Map<String, Object> result
) implements Serializable {}

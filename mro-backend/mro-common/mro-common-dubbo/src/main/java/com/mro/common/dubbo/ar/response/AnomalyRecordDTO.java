package com.mro.common.dubbo.ar.response;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

public record AnomalyRecordDTO(
        Long id,
        Long taskId,
        String anomalyType,
        BigDecimal confidence,
        String snapshotUrl,
        Instant detectedAt
) implements Serializable {}

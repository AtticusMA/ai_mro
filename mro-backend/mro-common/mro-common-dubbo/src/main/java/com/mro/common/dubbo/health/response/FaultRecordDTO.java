package com.mro.common.dubbo.health.response;

import java.io.Serializable;
import java.time.Instant;

public record FaultRecordDTO(
        Long id,
        String faultCode,
        String severity,
        String component,
        Instant detectedAt,
        String status
) implements Serializable {}

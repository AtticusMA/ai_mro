package com.mro.common.dubbo.manual.response;

import java.io.Serializable;
import java.time.Instant;

public record ManualDocDTO(
        Long id,
        String title,
        String manualNo,
        String aircraftType,
        String format,
        String parsedStatus,
        Instant uploadedAt
) implements Serializable {}

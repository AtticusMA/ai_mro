package com.mro.common.dubbo.manual.response;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;

public record ManualVersionDTO(
        Long id,
        Long documentId,
        String versionNo,
        String changeSummary,
        LocalDate effectiveDate,
        String revisedByName,
        Instant createdAt
) implements Serializable {}

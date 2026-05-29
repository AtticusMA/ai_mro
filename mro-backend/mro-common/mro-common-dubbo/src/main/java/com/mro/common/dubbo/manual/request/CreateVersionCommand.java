package com.mro.common.dubbo.manual.request;

import java.io.Serializable;
import java.time.LocalDate;

public record CreateVersionCommand(
        String versionNo,
        String changeSummary,
        LocalDate effectiveDate,
        Long revisedBy
) implements Serializable {}

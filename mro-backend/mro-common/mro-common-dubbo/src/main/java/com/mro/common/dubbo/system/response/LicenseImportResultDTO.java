package com.mro.common.dubbo.system.response;

import java.io.Serializable;

public record LicenseImportResultDTO(
    int totalRows,
    int successCount,
    int failCount,
    String errorDetails
) implements Serializable {}

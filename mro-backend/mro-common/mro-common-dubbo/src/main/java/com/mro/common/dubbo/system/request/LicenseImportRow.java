package com.mro.common.dubbo.system.request;

import java.io.Serializable;
import java.time.LocalDate;

public record LicenseImportRow(
    Long userId,
    String licenseNo,
    String licenseType,
    String aircraftType,
    String category,
    String issuer,
    LocalDate issueDate,
    LocalDate expiryDate,
    String remark
) implements Serializable {}

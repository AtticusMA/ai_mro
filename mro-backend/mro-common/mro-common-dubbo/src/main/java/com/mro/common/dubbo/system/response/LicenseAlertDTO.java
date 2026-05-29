package com.mro.common.dubbo.system.response;

import java.io.Serializable;
import java.time.LocalDate;

public record LicenseAlertDTO(
    Long licenseId,
    Long userId,
    String userName,
    String licenseNo,
    String licenseType,
    LocalDate expiryDate,
    int daysRemaining,
    String alertLevel
) implements Serializable {}

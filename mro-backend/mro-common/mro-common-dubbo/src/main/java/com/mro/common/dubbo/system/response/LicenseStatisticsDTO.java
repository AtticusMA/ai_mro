package com.mro.common.dubbo.system.response;

import java.io.Serializable;

public record LicenseStatisticsDTO(
    long totalLicenses,
    long validCount,
    long expiringCount,
    long expiredCount
) implements Serializable {}

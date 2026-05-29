package com.mro.common.dubbo.system.response;

import java.io.Serializable;
import java.time.LocalDate;

public record QualificationCheckResult(
    boolean qualified,
    String licenseNo,
    LocalDate expiryDate,
    String reason
) implements Serializable {}

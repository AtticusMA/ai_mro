package com.mro.common.dubbo.system.request;

import java.io.Serializable;
import java.time.LocalDate;

public record LicenseRenewalCommand(
    Long licenseId,
    LocalDate newExpiryDate,
    String newLicenseNo,
    String fileUrl
) implements Serializable {}

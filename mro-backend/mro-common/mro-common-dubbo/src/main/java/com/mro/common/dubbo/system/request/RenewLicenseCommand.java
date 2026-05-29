package com.mro.common.dubbo.system.request;

import java.io.Serializable;
import java.time.LocalDate;

public record RenewLicenseCommand(
    Long id,
    LocalDate newExpiryDate,
    String fileUrl,
    String remark
) implements Serializable {}

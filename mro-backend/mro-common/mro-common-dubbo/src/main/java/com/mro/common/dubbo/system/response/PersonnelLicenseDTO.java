package com.mro.common.dubbo.system.response;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record PersonnelLicenseDTO(
    Long id,
    Long userId,
    String userName,
    String licenseNo,
    String licenseType,
    String aircraftType,
    String category,
    String issuer,
    LocalDate issueDate,
    LocalDate expiryDate,
    String status,
    String fileUrl,
    String remark,
    LocalDateTime createTime
) implements Serializable {}

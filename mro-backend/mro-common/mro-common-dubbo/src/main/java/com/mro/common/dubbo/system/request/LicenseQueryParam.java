package com.mro.common.dubbo.system.request;

import java.io.Serializable;

public record LicenseQueryParam(
    Long userId,
    String licenseType,
    String aircraftType,
    String status,
    int pageNum,
    int pageSize
) implements Serializable {}

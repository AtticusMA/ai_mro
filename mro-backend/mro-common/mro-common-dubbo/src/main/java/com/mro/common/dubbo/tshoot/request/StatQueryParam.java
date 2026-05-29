package com.mro.common.dubbo.tshoot.request;

import java.io.Serializable;

public record StatQueryParam(
        String aircraftType,
        String startDate,
        String endDate
) implements Serializable {}

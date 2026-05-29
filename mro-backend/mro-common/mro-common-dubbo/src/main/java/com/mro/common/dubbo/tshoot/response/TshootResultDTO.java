package com.mro.common.dubbo.tshoot.response;

import java.io.Serializable;

public record TshootResultDTO(
        Long queryId,
        String status,
        TshootReportDTO report
) implements Serializable {}

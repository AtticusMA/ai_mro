package com.mro.common.dubbo.dtwin.response;

import java.io.Serializable;
import java.math.BigDecimal;

public record WorkstationDTO(
        Long id,
        Long hangarId,
        String name,
        BigDecimal positionX,
        BigDecimal positionY,
        BigDecimal positionZ,
        String status,
        String currentAircraftId
) implements Serializable {}

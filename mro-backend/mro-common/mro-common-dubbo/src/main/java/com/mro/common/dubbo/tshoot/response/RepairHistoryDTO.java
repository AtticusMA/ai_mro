package com.mro.common.dubbo.tshoot.response;

import java.io.Serializable;
import java.time.Instant;

public record RepairHistoryDTO(
        Long id,
        String aircraftId,
        String faultCode,
        String repairAction,
        String componentReplaced,
        Instant repairedAt
) implements Serializable {}

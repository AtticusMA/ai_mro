package com.mro.common.dubbo.workcard.response;

import java.io.Serializable;
import java.time.Instant;

public record WorkcardDTO(
    Long id, String cardNo, String title, String cardType, String aircraftId,
    String priority, String status, String createdByName,
    Instant dueDate, int completionRate
) implements Serializable {}

package com.mro.common.dubbo.workcard.response;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

public record WorkcardAlertDTO(
    Long workcardId, String cardNo, String title,
    Instant dueDate, double hoursUntilDue,
    int completionRate, List<String> assignees
) implements Serializable {}

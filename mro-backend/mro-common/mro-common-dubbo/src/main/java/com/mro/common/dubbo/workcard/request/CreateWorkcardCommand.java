package com.mro.common.dubbo.workcard.request;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

public record CreateWorkcardCommand(
    String title, String cardType, String aircraftId, String priority,
    Instant dueDate, List<CreateStepCommand> steps, Long createdBy
) implements Serializable {}

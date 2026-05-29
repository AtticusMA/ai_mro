package com.mro.common.dubbo.workcard.request;

import java.io.Serializable;

public record UpdateWorkcardCommand(
    Long id, String title, String cardType, String aircraftId,
    String priority, java.time.Instant dueDate, Long updatedBy
) implements Serializable {}

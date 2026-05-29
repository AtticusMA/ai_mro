package com.mro.common.dubbo.workcard.response;

import java.io.Serializable;

public record WorkcardProgressDTO(
    int totalWorkcards, int inProgress, int completed,
    int overdue, double overallCompletionRate
) implements Serializable {}

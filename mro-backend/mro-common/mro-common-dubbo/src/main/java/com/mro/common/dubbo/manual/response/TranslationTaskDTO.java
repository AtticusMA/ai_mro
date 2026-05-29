package com.mro.common.dubbo.manual.response;

import java.io.Serializable;
import java.math.BigDecimal;

public record TranslationTaskDTO(
        Long taskId,
        String status,
        BigDecimal accuracyScore,
        String resultUrl
) implements Serializable {}

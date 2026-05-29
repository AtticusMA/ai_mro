package com.mro.common.dubbo.ar.response;

import java.io.Serializable;
import java.time.Instant;

public record VideoArchiveDTO(
        Long id,
        Long taskId,
        Long sessionId,
        String fileUrl,
        int durationSeconds,
        Instant createdAt
) implements Serializable {}

package com.mro.common.dubbo.ar.response;

import java.io.Serializable;
import java.time.Instant;

public record ArSessionDTO(
        Long id,
        Long callerId,
        Long expertId,
        String status,
        String signalingToken,
        Instant createdAt
) implements Serializable {}

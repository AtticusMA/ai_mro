package com.mro.common.dubbo.tshoot.response;

import java.io.Serializable;
import java.time.Instant;

public record KnowledgeBaseDTO(
        Long id,
        String name,
        String aircraftType,
        int docCount,
        String status,
        Instant createdAt
) implements Serializable {}

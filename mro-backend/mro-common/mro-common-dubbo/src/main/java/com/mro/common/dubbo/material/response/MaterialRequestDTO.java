package com.mro.common.dubbo.material.response;
import java.io.Serializable;
import java.time.LocalDateTime;
public record MaterialRequestDTO(Long id, String requestNo, Long workcardId, Long requesterId,
    String urgency, String status, int itemsCount, LocalDateTime createTime) implements Serializable {}

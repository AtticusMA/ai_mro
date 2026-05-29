package com.mro.common.dubbo.workcard.response;

import java.io.Serializable;
import java.time.LocalDateTime;

public record WorkcardCheckinDTO(
    Long id,
    Long workcardId,
    Long userId,
    String userName,
    LocalDateTime checkInTime,
    LocalDateTime checkOutTime,
    String location,
    String deviceId
) implements Serializable {}

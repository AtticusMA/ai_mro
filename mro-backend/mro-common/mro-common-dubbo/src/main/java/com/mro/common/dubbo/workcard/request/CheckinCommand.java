package com.mro.common.dubbo.workcard.request;

import java.io.Serializable;

public record CheckinCommand(
    Long workcardId,
    String location,
    String deviceId
) implements Serializable {}

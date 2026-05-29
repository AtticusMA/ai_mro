package com.mro.common.dubbo.manual.request;

import java.io.Serializable;

public record CreateManualCommand(
        String title,
        String manualNo,
        String aircraftType,
        String format,
        String fileUrl,
        Long uploaderId
) implements Serializable {}

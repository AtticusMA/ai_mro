package com.mro.common.dubbo.tshoot.request;

import java.io.Serializable;
import java.util.List;

public record FaultQueryCommand(
        Long userId,
        String inputType,
        String inputContent,
        String aircraftType,
        List<Long> kbIds
) implements Serializable {}

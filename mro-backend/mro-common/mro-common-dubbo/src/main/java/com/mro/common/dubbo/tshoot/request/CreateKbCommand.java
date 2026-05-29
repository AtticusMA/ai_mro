package com.mro.common.dubbo.tshoot.request;

import java.io.Serializable;

public record CreateKbCommand(
        String name,
        String aircraftType
) implements Serializable {}

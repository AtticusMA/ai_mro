package com.mro.common.dubbo.system.request;

import java.io.Serializable;

public record QualificationCheckParam(
    Long userId,
    String aircraftType,
    String category
) implements Serializable {}

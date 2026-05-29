package com.mro.common.dubbo.ar.request;

import java.io.Serializable;

public record CreateInspectionCommand(
        String aircraftId,
        String routeTemplate,
        Long inspectorId
) implements Serializable {}

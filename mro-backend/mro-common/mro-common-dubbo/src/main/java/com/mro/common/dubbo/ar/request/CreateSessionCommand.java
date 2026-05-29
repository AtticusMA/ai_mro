package com.mro.common.dubbo.ar.request;

import java.io.Serializable;

public record CreateSessionCommand(
        Long callerId,
        Long taskId,
        Long expertId
) implements Serializable {}

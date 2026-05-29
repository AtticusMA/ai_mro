package com.mro.common.dubbo.ar.response;

import java.io.Serializable;
import java.util.Map;

public record AnnotationDTO(
        Long sessionId,
        String annotationType,
        Map<String, Object> coordinates,
        String content
) implements Serializable {}

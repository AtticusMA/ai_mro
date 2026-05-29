package com.mro.common.dubbo.workcard.response;

import java.io.Serializable;
import java.time.LocalDate;

public record QualificationDTO(
    Long userId, String userName, String qualificationType,
    String level, LocalDate validTo
) implements Serializable {}

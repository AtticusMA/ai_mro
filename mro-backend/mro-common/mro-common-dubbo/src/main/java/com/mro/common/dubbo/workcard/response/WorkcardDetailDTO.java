package com.mro.common.dubbo.workcard.response;

import java.io.Serializable;
import java.util.List;

public record WorkcardDetailDTO(
    Long id, String cardNo, String title, String cardType, String aircraftId,
    String priority, String status, List<WorkcardStepDTO> steps
) implements Serializable {}

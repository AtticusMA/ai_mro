package com.mro.common.dubbo.material.request;
import java.io.Serializable;
import java.util.List;
public record CreateMaterialRequestCommand(Long workcardId, Long requesterId, Long deptId,
    String urgency, List<MaterialRequestItemDTO> items) implements Serializable {}

package com.mro.common.dubbo.material.response;
import com.mro.common.dubbo.material.request.MaterialRequestItemDTO;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
public record MaterialRequestDetailDTO(Long id, String requestNo, Long workcardId,
    Long requesterId, Long deptId, String urgency, String status, String rejectReason,
    List<MaterialRequestItemDTO> items, Long approvedBy, LocalDateTime approvedAt,
    Long receivedBy, LocalDateTime receivedAt, LocalDateTime createTime) implements Serializable {}

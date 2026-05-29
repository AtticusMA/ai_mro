package com.mro.common.dubbo.tool.response;
import java.io.Serializable;
import java.time.Instant;
public record BorrowRecordDTO(Long id, Long toolId, String toolName, Long userId, String userName, Instant borrowTime, Instant expectedReturn, Instant actualReturn, String status, Long workcardId) implements Serializable {}
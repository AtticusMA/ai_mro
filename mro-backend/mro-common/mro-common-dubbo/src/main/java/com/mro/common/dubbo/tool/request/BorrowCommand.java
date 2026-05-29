package com.mro.common.dubbo.tool.request;
import java.io.Serializable;
import java.util.List;
public record BorrowCommand(Long userId, Long cabinetId, List<Long> toolIds, Long workcardId, int expectedReturnHours) implements Serializable {}
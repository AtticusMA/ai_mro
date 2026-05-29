package com.mro.common.dubbo.tool.response;
import java.io.Serializable;
import java.util.List;
public record BorrowResultDTO(List<Long> borrowRecordIds) implements Serializable {}
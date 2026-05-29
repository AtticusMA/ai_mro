package com.mro.common.dubbo.tool.request;
import java.io.Serializable;
public record BorrowRecordQueryParam(int pageNum, int pageSize, Long userId, String status) implements Serializable {}
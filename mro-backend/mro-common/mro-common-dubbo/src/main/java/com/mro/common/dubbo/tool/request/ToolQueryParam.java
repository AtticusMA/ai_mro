package com.mro.common.dubbo.tool.request;
import java.io.Serializable;
public record ToolQueryParam(int pageNum, int pageSize, String status, String category, Long cabinetId) implements Serializable {}
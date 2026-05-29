package com.mro.common.dubbo.tool.response;
import java.io.Serializable;
import java.util.List;
public record ReturnResultDTO(int returnedCount, List<String> missingRfids) implements Serializable {}
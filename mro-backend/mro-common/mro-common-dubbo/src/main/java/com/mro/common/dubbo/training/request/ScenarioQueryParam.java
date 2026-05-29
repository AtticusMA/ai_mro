package com.mro.common.dubbo.training.request;

import java.io.Serializable;

public record ScenarioQueryParam(
    String status, String category, int pageNum, int pageSize
) implements Serializable {}

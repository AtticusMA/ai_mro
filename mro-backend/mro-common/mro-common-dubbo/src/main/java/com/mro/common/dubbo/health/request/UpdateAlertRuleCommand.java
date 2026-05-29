package com.mro.common.dubbo.health.request;

import java.io.Serializable;
import java.util.List;

public record UpdateAlertRuleCommand(
        Long id,
        String ruleName,
        String aircraftType,
        String metricName,
        String operator,
        Double threshold,
        String alertLevel,
        List<Long> notifyUserIds,
        Boolean enabled
) implements Serializable {}

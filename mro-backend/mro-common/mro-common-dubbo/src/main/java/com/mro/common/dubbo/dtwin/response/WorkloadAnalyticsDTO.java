package com.mro.common.dubbo.dtwin.response;

import java.io.Serializable;
import java.util.List;

public record WorkloadAnalyticsDTO(
        List<WorkstationLoadDTO> workstationLoads,
        double avgUtilizationRate
) implements Serializable {}

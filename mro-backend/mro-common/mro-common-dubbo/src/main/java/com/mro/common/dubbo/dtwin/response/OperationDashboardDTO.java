package com.mro.common.dubbo.dtwin.response;

import java.io.Serializable;
import java.util.List;

public record OperationDashboardDTO(
        int totalPackages,
        int inProgressPackages,
        int completedToday,
        int totalPersonnel,
        List<TaskPackageDTO> activePackages
) implements Serializable {}

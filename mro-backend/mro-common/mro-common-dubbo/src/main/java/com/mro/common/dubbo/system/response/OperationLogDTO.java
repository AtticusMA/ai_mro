package com.mro.common.dubbo.system.response;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class OperationLogDTO implements Serializable {

    private Long id;
    private String requestId;
    private Long operatorId;
    private String operatorName;
    private Long deptId;
    private String requestUri;
    private String requestMethod;
    private String requestParams;
    private String requestBody;
    private Integer responseStatus;
    private Long costMs;
    private String clientIp;
    private String userAgent;
    private LocalDateTime requestTime;
}

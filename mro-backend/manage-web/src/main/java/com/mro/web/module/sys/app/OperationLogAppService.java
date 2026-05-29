package com.mro.web.module.sys.app;

import com.mro.common.core.response.PageResult;
import com.mro.common.dubbo.system.response.OperationLogDTO;
import com.mro.common.dubbo.system.request.OperationLogQueryParam;
import com.mro.common.dubbo.system.request.*;
import com.mro.common.dubbo.system.response.*;
import com.mro.common.dubbo.system.service.OperationLogDubboService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class OperationLogAppService {

    @DubboReference(version = "1.0.0", timeout = 5000, retries = 0)
    private OperationLogDubboService operationLogDubboService;

    public PageResult<OperationLogDTO> listPage(int pageNum, int pageSize,
                                                String operatorName, String requestUri,
                                                Long deptId,
                                                LocalDateTime startTime, LocalDateTime endTime) {
        OperationLogQueryParam param = new OperationLogQueryParam();
        param.setPageNum(pageNum);
        param.setPageSize(pageSize);
        param.setOperatorName(operatorName);
        param.setRequestUri(requestUri);
        param.setDeptId(deptId);
        param.setStartTime(startTime);
        param.setEndTime(endTime);
        return operationLogDubboService.listPage(param);
    }

    public OperationLogDTO getById(Long id) {
        return operationLogDubboService.getById(id);
    }
}

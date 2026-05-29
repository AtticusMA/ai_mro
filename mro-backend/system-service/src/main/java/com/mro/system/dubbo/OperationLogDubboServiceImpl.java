package com.mro.system.dubbo;

import com.mro.common.core.response.PageResult;
import com.mro.common.dubbo.system.response.OperationLogDTO;
import com.mro.common.dubbo.system.request.OperationLogQueryParam;
import com.mro.common.dubbo.system.request.SaveOperationLogCommand;
import com.mro.common.dubbo.system.request.*;
import com.mro.common.dubbo.system.response.*;
import com.mro.common.dubbo.system.service.OperationLogDubboService;
import com.mro.system.service.OperationLogService;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

@DubboService(version = "1.0.0")
public class OperationLogDubboServiceImpl implements OperationLogDubboService {

    @Autowired
    private OperationLogService operationLogService;

    @Override
    public void save(SaveOperationLogCommand cmd) {
        operationLogService.save(cmd);
    }

    @Override
    public PageResult<OperationLogDTO> listPage(OperationLogQueryParam param) {
        return operationLogService.listPage(param);
    }

    @Override
    public OperationLogDTO getById(Long id) {
        return operationLogService.getById(id);
    }
}

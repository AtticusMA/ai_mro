package com.mro.common.dubbo.system.service;

import com.mro.common.core.response.PageResult;
import com.mro.common.dubbo.system.request.OperationLogQueryParam;
import com.mro.common.dubbo.system.request.SaveOperationLogCommand;
import com.mro.common.dubbo.system.response.OperationLogDTO;

public interface OperationLogDubboService {

    void save(SaveOperationLogCommand cmd);

    PageResult<OperationLogDTO> listPage(OperationLogQueryParam param);

    OperationLogDTO getById(Long id);
}

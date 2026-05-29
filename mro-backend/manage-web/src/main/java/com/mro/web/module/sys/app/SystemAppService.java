package com.mro.web.module.sys.app;

import com.mro.common.dubbo.common.response.UserInfoDTO;
import com.mro.common.dubbo.common.response.*;
import com.mro.common.dubbo.system.service.SystemDubboService;
import com.mro.web.context.UserContext;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

@Service
public class SystemAppService {

    @DubboReference(version = "1.0.0", timeout = 5000, retries = 0)
    private SystemDubboService systemDubboService;

    public UserInfoDTO getCurrentUserFullInfo() {
        return systemDubboService.getUserFullInfo(UserContext.getUserId());
    }
}

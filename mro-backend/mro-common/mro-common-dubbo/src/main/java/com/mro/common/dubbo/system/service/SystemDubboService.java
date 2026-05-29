package com.mro.common.dubbo.system.service;

import com.mro.common.dubbo.common.response.UserInfoDTO;

public interface SystemDubboService {

    UserInfoDTO getUserFullInfo(Long userId);

    String getDeptName(Long deptId);
}

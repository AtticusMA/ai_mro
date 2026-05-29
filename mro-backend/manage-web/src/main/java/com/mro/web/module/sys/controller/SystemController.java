package com.mro.web.module.sys.controller;

import com.mro.common.core.response.R;
import com.mro.common.dubbo.common.response.UserInfoDTO;
import com.mro.web.module.sys.app.SystemAppService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/system")
public class SystemController {

    @Autowired
    private SystemAppService systemAppService;

    @GetMapping("/current-user")
    public R<UserInfoDTO> currentUser() {
        return R.ok(systemAppService.getCurrentUserFullInfo());
    }
}

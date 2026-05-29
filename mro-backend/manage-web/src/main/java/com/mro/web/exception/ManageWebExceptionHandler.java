package com.mro.web.exception;

import com.mro.common.core.exception.GlobalExceptionHandler;
import com.mro.web.service.DingTalkAlertService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@RequiredArgsConstructor
public class ManageWebExceptionHandler extends GlobalExceptionHandler {

    private final DingTalkAlertService dingTalkAlertService;

    @Override
    protected void onSystemException(Exception ex, HttpServletRequest request) {
        dingTalkAlertService.sendAlert(ex, request);
    }
}

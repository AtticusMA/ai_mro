package com.mro.web.service;

import com.mro.common.dubbo.system.request.SaveOperationLogCommand;
import com.mro.common.dubbo.system.request.*;
import com.mro.common.dubbo.system.response.*;
import com.mro.common.dubbo.system.service.OperationLogDubboService;
import com.mro.web.context.UserContext;
import com.mro.web.interceptor.CachedBodyHttpServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.dubbo.config.annotation.DubboReference;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AsyncLogService {

    @DubboReference(version = "1.0.0", timeout = 3000, retries = 0)
    private OperationLogDubboService operationLogDubboService;

    @Async("logTaskExecutor")
    public void saveAsync(HttpServletRequest request, HttpServletResponse response,
                          long costMs, LocalDateTime requestTime) {
        try {
            SaveOperationLogCommand cmd = new SaveOperationLogCommand();
            cmd.setRequestId(MDC.get("requestId"));
            cmd.setOperatorId(UserContext.getUserId());
            cmd.setDeptId(UserContext.getDeptId());
            cmd.setRequestUri(request.getRequestURI());
            cmd.setRequestMethod(request.getMethod());
            cmd.setRequestParams(request.getQueryString());
            cmd.setResponseStatus(response.getStatus());
            cmd.setCostMs(costMs);
            cmd.setClientIp(resolveClientIp(request));
            cmd.setUserAgent(request.getHeader("User-Agent"));
            cmd.setRequestTime(requestTime);

            if (request instanceof CachedBodyHttpServletRequest cached) {
                String body = new String(cached.getCachedBody());
                cmd.setRequestBody(body.length() > 4000 ? body.substring(0, 4000) : body);
            }

            operationLogDubboService.save(cmd);
        } catch (Exception ignored) {
            // 日志持久化失败不影响主流程
        }
    }

    private String resolveClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isBlank() && !"unknown".equalsIgnoreCase(ip)) {
            return ip.split(",")[0].trim();
        }
        ip = request.getHeader("X-Real-IP");
        return (ip != null && !ip.isBlank()) ? ip : request.getRemoteAddr();
    }
}

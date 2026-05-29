package com.mro.web.interceptor;

import com.mro.common.core.constant.HeaderConstants;
import com.mro.web.context.UserContext;
import com.mro.web.service.AsyncLogService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class OperationLogInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(OperationLogInterceptor.class);
    private static final String ATTR_START_TIME = "op_log_start";

    private final AsyncLogService asyncLogService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute(ATTR_START_TIME, System.currentTimeMillis());

        String requestId = request.getHeader(HeaderConstants.REQUEST_ID);
        log.info("REQ-IN  {} {} requestId={} ip={}",
                request.getMethod(), request.getRequestURI(),
                requestId != null ? requestId : "-",
                getClientIp(request));
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        Long startTime = (Long) request.getAttribute(ATTR_START_TIME);
        long costMs = startTime != null ? System.currentTimeMillis() - startTime : -1;

        log.info("REQ-OUT {} {} status={} cost={}ms requestId={}",
                request.getMethod(), request.getRequestURI(),
                response.getStatus(), costMs,
                MDC.get("requestId") != null ? MDC.get("requestId") : "-");

        asyncLogService.saveAsync(request, response, costMs, LocalDateTime.now().minusNanos(costMs * 1_000_000L));
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isBlank() && !"unknown".equalsIgnoreCase(ip)) {
            return ip.split(",")[0].trim();
        }
        ip = request.getHeader("X-Real-IP");
        return (ip != null && !ip.isBlank()) ? ip : request.getRemoteAddr();
    }
}

package com.mro.web.context;

import com.mro.common.core.constant.HeaderConstants;
import com.mro.common.dubbo.common.request.UserContextDTO;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 从 Gateway 注入的 Header 读取用户上下文，存入 ThreadLocal
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class UserContextFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String userIdStr = request.getHeader(HeaderConstants.USER_ID);
            if (userIdStr != null && !userIdStr.isBlank()) {
                Long userId   = Long.parseLong(userIdStr);
                String deptIdStr = request.getHeader(HeaderConstants.USER_DEPT_ID);
                Long deptId  = (deptIdStr != null && !deptIdStr.isBlank()) ? Long.parseLong(deptIdStr) : null;

                UserContextDTO ctx = new UserContextDTO(
                    userId,
                    deptId,
                    UserContext.parseList(request.getHeader(HeaderConstants.USER_ROLES)),
                    UserContext.parseList(request.getHeader(HeaderConstants.USER_PERMISSIONS))
                );
                UserContext.set(ctx);
            }
            filterChain.doFilter(request, response);
        } finally {
            UserContext.clear();
        }
    }
}

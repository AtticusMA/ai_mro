package com.mro.web.config;

import com.mro.common.core.exception.GlobalExceptionHandler;
import com.mro.web.interceptor.OperationLogInterceptor;
import com.mro.web.interceptor.SmDecryptInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@Import(GlobalExceptionHandler.class)
@ServletComponentScan("com.mro.web.interceptor")
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final SmDecryptInterceptor smDecryptInterceptor;
    private final OperationLogInterceptor operationLogInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(smDecryptInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/security/public-key");
        registry.addInterceptor(operationLogInterceptor)
                .addPathPatterns("/api/**");
    }
}

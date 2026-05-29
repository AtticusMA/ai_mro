package com.mro.auth.config;

import com.mro.common.core.exception.GlobalExceptionHandler;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Configuration;

@Configuration
@Import(GlobalExceptionHandler.class)
public class WebConfig {
}

package com.mro.dtwin.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.mro.dtwin.mapper")
public class MybatisPlusConfig {
}

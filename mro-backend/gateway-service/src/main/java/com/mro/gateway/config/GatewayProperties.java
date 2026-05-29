package com.mro.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "mro.gateway")
public class GatewayProperties {

    private List<String> whiteList = List.of("/api/auth/login", "/api/auth/refresh-token");
    private String jwtSecret;

    public List<String> getWhiteList() { return whiteList; }
    public void setWhiteList(List<String> whiteList) { this.whiteList = whiteList; }
    public String getJwtSecret() { return jwtSecret; }
    public void setJwtSecret(String jwtSecret) { this.jwtSecret = jwtSecret; }
}

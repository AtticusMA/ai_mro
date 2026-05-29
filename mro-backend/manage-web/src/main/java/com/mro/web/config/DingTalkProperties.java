package com.mro.web.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "dingtalk.alert")
public class DingTalkProperties {

    private boolean enabled = false;
    private String webhook = "";
    private String secret = "";

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public String getWebhook() { return webhook; }
    public void setWebhook(String webhook) { this.webhook = webhook; }

    public String getSecret() { return secret; }
    public void setSecret(String secret) { this.secret = secret; }
}

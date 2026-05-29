package com.mro.web.config;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "security.sm2")
public class EncryptionProperties {

    private String privateKey;
    private String publicKey;

    @NestedConfigurationProperty
    private Encryption encryption = new Encryption();

    @PostConstruct
    public void validate() {
        if (encryption.isEnabled()) {
            if (privateKey == null || privateKey.isBlank()) {
                throw new IllegalStateException(
                    "[MRO] 国密加密已启用，但 SM2 私钥未配置。" +
                    "请设置环境变量 SM2_PRIVATE_KEY 或配置 security.sm2.private-key。");
            }
            if (publicKey == null || publicKey.isBlank()) {
                throw new IllegalStateException(
                    "[MRO] 国密加密已启用，但 SM2 公钥未配置。" +
                    "请设置环境变量 SM2_PUBLIC_KEY 或配置 security.sm2.public-key。");
            }
        }
    }

    @Data
    public static class Encryption {
        private boolean enabled = true;
        /** 时间戳容忍范围（秒），默认 ±5 分钟 */
        private int timestampTolerance = 300;
    }
}

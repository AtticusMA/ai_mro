package com.mro.web.module.security.controller;

import com.mro.common.core.response.R;
import com.mro.web.config.EncryptionProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/security")
@RequiredArgsConstructor
public class SecurityController {

    private final EncryptionProperties encryptionProperties;

    @GetMapping("/public-key")
    public R<Map<String, String>> getPublicKey() {
        return R.ok(Map.of("publicKey", encryptionProperties.getPublicKey()));
    }
}

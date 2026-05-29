package com.mro.web.interceptor;

import com.mro.common.core.constant.HeaderConstants;
import com.mro.common.core.util.SM2Util;
import com.mro.common.core.util.SM3Util;
import com.mro.web.config.EncryptionProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;

/**
 * 国密传输解密拦截器：
 * 1. 时间戳防重放（±timestampTolerance 秒）
 * 2. requestId 防重放（Redis SETNX TTL = 2 × timestampTolerance）
 * 3. SM2 解密 SM4 key + IV
 * 4. HMAC-SM3 验签（body + timestamp + requestId）
 * 5. 将 key/iv 写入请求属性，供后续 @Encrypted 字段解密使用
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SmDecryptInterceptor implements HandlerInterceptor {

    public static final String ATTR_SM4_KEY = "sm4Key";
    public static final String ATTR_SM4_IV  = "sm4Iv";

    private static final String REDIS_KEY_PREFIX = "sm:req:id:";

    private final EncryptionProperties props;
    private final StringRedisTemplate redisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws IOException {

        if (!props.getEncryption().isEnabled()) {
            return true;
        }

        String encryptedKey = request.getHeader(HeaderConstants.ENCRYPTED_KEY);
        String encryptedIv  = request.getHeader(HeaderConstants.ENCRYPTED_IV);
        String signature    = request.getHeader(HeaderConstants.SIGNATURE);
        String timestampStr = request.getHeader(HeaderConstants.TIMESTAMP);
        String requestId    = request.getHeader(HeaderConstants.REQUEST_ID);

        if (anyBlank(encryptedKey, encryptedIv, signature, timestampStr, requestId)) {
            reject(response, "缺少加密请求头");
            return false;
        }

        // 1. 时间戳校验
        long now = System.currentTimeMillis() / 1000L;
        long timestamp;
        try {
            timestamp = Long.parseLong(timestampStr);
        } catch (NumberFormatException e) {
            reject(response, "时间戳格式错误");
            return false;
        }
        long tolerance = props.getEncryption().getTimestampTolerance();
        if (Math.abs(now - timestamp) > tolerance) {
            reject(response, "请求已过期");
            return false;
        }

        // 2. requestId 防重放（Redis SETNX）
        String redisKey = REDIS_KEY_PREFIX + requestId;
        Boolean isNew = redisTemplate.opsForValue()
                .setIfAbsent(redisKey, "1", Duration.ofSeconds(tolerance * 2));
        if (!Boolean.TRUE.equals(isNew)) {
            reject(response, "重放请求");
            return false;
        }

        // 3. SM2 解密 SM4 key + IV
        // 前端将 16字节随机 key 以 hex 字符串形式经 SM2 加密传输，后端解密后需再 hex-decode 还原为 16 字节
        byte[] sm4Key;
        byte[] sm4Iv;
        try {
            byte[] keyHexBytes = SM2Util.decryptByPrivateKey(encryptedKey, props.getPrivateKey());
            byte[] ivHexBytes  = SM2Util.decryptByPrivateKey(encryptedIv,  props.getPrivateKey());
            sm4Key = hexToBytes(new String(keyHexBytes, StandardCharsets.UTF_8));
            sm4Iv  = hexToBytes(new String(ivHexBytes,  StandardCharsets.UTF_8));
        } catch (Exception e) {
            log.warn("SM2 解密失败: {}", e.getMessage());
            reject(response, "密钥解密失败");
            return false;
        }

        // 4. HMAC-SM3 验签：签名覆盖 body + timestamp + requestId
        String body = readBody(request);
        String signPayload = body + timestampStr + requestId;
        if (!SM3Util.hmacVerify(sm4Key, signPayload, signature)) {
            log.warn("HMAC-SM3 验签失败, requestId={}", requestId);
            reject(response, "签名验证失败");
            return false;
        }

        // 5. 将 key/iv 写入请求属性，供 Controller 层 FieldEncryptionUtil 使用
        request.setAttribute(ATTR_SM4_KEY, sm4Key);
        request.setAttribute(ATTR_SM4_IV,  sm4Iv);

        return true;
    }

    private String readBody(HttpServletRequest request) throws IOException {
        if (request instanceof CachedBodyHttpServletRequest cached) {
            return new String(cached.getCachedBody(), StandardCharsets.UTF_8);
        }
        byte[] bytes = StreamUtils.copyToByteArray(request.getInputStream());
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private void reject(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":4001,\"msg\":\"" + message + "\"}");
    }

    private static boolean anyBlank(String... values) {
        for (String v : values) {
            if (v == null || v.isBlank()) return true;
        }
        return false;
    }

    private static byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }
}

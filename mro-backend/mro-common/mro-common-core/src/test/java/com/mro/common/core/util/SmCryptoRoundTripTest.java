package com.mro.common.core.util;

import cn.hutool.core.util.HexUtil;
import cn.hutool.crypto.SmUtil;
import cn.hutool.crypto.asymmetric.SM2;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 前后端国密传输加密协议端到端一致性测试（T-015）。
 *
 * 模拟：
 *   前端：随机生成 SM4 key/IV → SM4加密敏感字段 → SM2加密key/IV → HMAC-SM3签名
 *   后端：SM2解密key/IV → HMAC-SM3验签 → SM4解密敏感字段
 */
class SmCryptoRoundTripTest {

    static String privateKeyHex;
    static String publicKeyHex;

    @BeforeAll
    static void generateKeyPair() {
        Security.addProvider(new BouncyCastleProvider());
        SM2 sm2 = SmUtil.sm2();
        BCECPrivateKey priv = (BCECPrivateKey) sm2.getPrivateKey();
        BCECPublicKey  pub  = (BCECPublicKey)  sm2.getPublicKey();
        privateKeyHex = HexUtil.encodeHexStr(priv.getD().toByteArray());
        String full = HexUtil.encodeHexStr(pub.getQ().getEncoded(false));
        publicKeyHex = full.substring(2); // strip 04 prefix
    }

    // ── 前端侧模拟 ──────────────────────────────────────────────────

    private byte[] frontendGenerateKey() {
        byte[] key = new byte[16];
        new SecureRandom().nextBytes(key);
        return key;
    }

    /** 前端：SM4-CBC 加密字段，返回 Base64 密文（hutool 同款，模拟一致性） */
    private String frontendSm4Encrypt(String plaintext, byte[] key, byte[] iv) {
        return SM4Util.encrypt(plaintext, key, iv);
    }

    /**
     * 前端：SM2 加密 SM4 key Hex 字符串（16字节 → 32字符 hex → SM2 C1C3C2 → Base64）
     * 前端实际传输的是 hex 字符串的 SM2 密文 Base64
     */
    private String frontendSm2EncryptKeyHex(byte[] rawKey, String pubKeyHex) {
        String keyHex = HexUtil.encodeHexStr(rawKey);
        byte[] keyHexBytes = keyHex.getBytes(StandardCharsets.UTF_8);
        String encBase64 = SM2Util.encryptByPublicKey(keyHexBytes, pubKeyHex);
        return encBase64;
    }

    /** 前端：HMAC-SM3 签名，key 为 SM4 key 原始字节 */
    private String frontendHmacSign(byte[] sm4Key, String payload) {
        return SM3Util.hmacSign(sm4Key, payload);
    }

    // ── 后端侧模拟（与真实 SmDecryptInterceptor 逻辑一致）──────────

    private byte[] backendSm2DecryptToKeyBytes(String encBase64, String privKeyHex) {
        byte[] hexBytes = SM2Util.decryptByPrivateKey(encBase64, privKeyHex);
        String hexStr = new String(hexBytes, StandardCharsets.UTF_8);
        return hexToBytes(hexStr);
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

    // ── Tests ──────────────────────────────────────────────────────

    @Test
    void sm4_encrypt_decrypt_roundtrip() {
        byte[] key = frontendGenerateKey();
        byte[] iv  = frontendGenerateKey();
        String plain = "13800138000";

        String cipher = SM4Util.encrypt(plain, key, iv);
        assertThat(cipher).isNotEqualTo(plain);

        String decrypted = SM4Util.decrypt(cipher, key, iv);
        assertThat(decrypted).isEqualTo(plain);
    }

    @Test
    void sm4_chinese_chars_roundtrip() {
        byte[] key = frontendGenerateKey();
        byte[] iv  = frontendGenerateKey();
        String plain = "张三";

        String decrypted = SM4Util.decrypt(SM4Util.encrypt(plain, key, iv), key, iv);
        assertThat(decrypted).isEqualTo(plain);
    }

    @Test
    void sm2_encrypt_decrypt_sm4_key_hex_roundtrip() {
        byte[] originalKey = frontendGenerateKey();

        // 前端：加密 key 的 hex 字符串
        String encBase64 = frontendSm2EncryptKeyHex(originalKey, publicKeyHex);

        // 后端：SM2解密 → hex解码 → 还原 16 字节 key
        byte[] recoveredKey = backendSm2DecryptToKeyBytes(encBase64, privateKeyHex);

        assertThat(recoveredKey).isEqualTo(originalKey);
    }

    @Test
    void hmac_sm3_sign_verify() {
        byte[] key = frontendGenerateKey();
        String payload = "{\"mobile\":\"xxx\"}1716700800abc-uuid";

        String sig = SM3Util.hmacSign(key, payload);
        assertThat(SM3Util.hmacVerify(key, payload, sig)).isTrue();
        assertThat(SM3Util.hmacVerify(key, payload + "tampered", sig)).isFalse();
    }

    @Test
    void full_protocol_roundtrip_mobile_idcard() {
        // ── 前端侧 ──
        byte[] sm4Key = frontendGenerateKey();
        byte[] sm4Iv  = frontendGenerateKey();

        // 加密敏感字段
        String plainMobile = "13800138001";
        String plainIdCard = "110101199001011234";
        String encMobile = frontendSm4Encrypt(plainMobile, sm4Key, sm4Iv);
        String encIdCard  = frontendSm4Encrypt(plainIdCard, sm4Key, sm4Iv);

        // 防重放元数据
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000L);
        String requestId = java.util.UUID.randomUUID().toString();

        // HMAC 签名
        String bodyStr = "{\"mobile\":\"" + encMobile + "\",\"idCard\":\"" + encIdCard + "\"}";
        String payload = bodyStr + timestamp + requestId;
        String signature = frontendHmacSign(sm4Key, payload);

        // SM2 加密 key / IV
        String encKeyB64 = frontendSm2EncryptKeyHex(sm4Key, publicKeyHex);
        String encIvB64  = frontendSm2EncryptKeyHex(sm4Iv,  publicKeyHex);

        // ── 后端侧 ──
        // 1. SM2 解密 → hex-decode → 还原 key/iv
        byte[] recoveredKey = backendSm2DecryptToKeyBytes(encKeyB64, privateKeyHex);
        byte[] recoveredIv  = backendSm2DecryptToKeyBytes(encIvB64,  privateKeyHex);

        assertThat(recoveredKey).isEqualTo(sm4Key);
        assertThat(recoveredIv).isEqualTo(sm4Iv);

        // 2. HMAC 验签
        assertThat(SM3Util.hmacVerify(recoveredKey, payload, signature)).isTrue();

        // 3. SM4 解密字段
        assertThat(SM4Util.decrypt(encMobile, recoveredKey, recoveredIv)).isEqualTo(plainMobile);
        assertThat(SM4Util.decrypt(encIdCard,  recoveredKey, recoveredIv)).isEqualTo(plainIdCard);
    }
}

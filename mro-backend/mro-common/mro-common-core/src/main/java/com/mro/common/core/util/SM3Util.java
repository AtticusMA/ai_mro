package com.mro.common.core.util;

import cn.hutool.core.codec.Base64;
import cn.hutool.crypto.SmUtil;
import cn.hutool.crypto.digest.HMac;
import cn.hutool.crypto.digest.HmacAlgorithm;

import java.nio.charset.StandardCharsets;

/**
 * SM3 摘要 + HMAC-SM3 工具类。
 * HMAC-SM3 用于请求防篡改签名：以 SM4 密钥为 HMAC 密钥，对 Body+timestamp+requestId 计算 MAC。
 */
public class SM3Util {

    /**
     * 计算 SM3 摘要，返回 Hex 字符串（64位）。
     */
    public static String digest(String data) {
        return SmUtil.sm3(data);
    }

    /**
     * 计算 HMAC-SM3，返回 Base64 编码的 MAC 值。
     *
     * @param key  HMAC 密钥（使用 SM4 密钥，16字节）
     * @param data 待签名数据
     */
    public static String hmacSign(byte[] key, String data) {
        HMac hmac = new HMac(HmacAlgorithm.HmacSM3, key);
        byte[] mac = hmac.digest(data.getBytes(StandardCharsets.UTF_8));
        return Base64.encode(mac);
    }

    /**
     * 验证 HMAC-SM3 签名。
     *
     * @param key       HMAC 密钥
     * @param data      原始数据
     * @param signBase64 待验证签名（Base64）
     */
    public static boolean hmacVerify(byte[] key, String data, String signBase64) {
        String expected = hmacSign(key, data);
        return expected.equals(signBase64);
    }
}

package com.mro.common.core.util;

import cn.hutool.crypto.Mode;
import cn.hutool.crypto.Padding;
import cn.hutool.crypto.symmetric.SM4;
import lombok.extern.slf4j.Slf4j;

/**
 * SM4 对称加密工具类（SM4/CBC/PKCS5Padding）。
 * 密钥和 IV 均为 16字节。
 */

public class SM4Util {

    /**
     * SM4-CBC 加密，返回 Base64 密文。
     *
     * @param plainText 明文字符串（UTF-8）
     * @param key       16字节 SM4 密钥
     * @param iv        16字节 IV
     */
    public static String encrypt(String plainText, byte[] key, byte[] iv) {
        SM4 sm4 = new SM4(Mode.CBC, Padding.PKCS5Padding, key, iv);
        return sm4.encryptBase64(plainText);
    }

    /**
     * SM4-CBC 解密，返回明文字符串（UTF-8）。
     *
     * @param encryptedBase64 Base64 密文
     * @param key             16字节 SM4 密钥
     * @param iv              16字节 IV
     */
    public static String decrypt(String encryptedBase64, byte[] key, byte[] iv) {
        SM4 sm4 = new SM4(Mode.CBC, Padding.PKCS5Padding, key, iv);
        return sm4.decryptStr(encryptedBase64);
    }
}


package com.mro.common.core.util;

import cn.hutool.core.codec.Base64;
import cn.hutool.crypto.SmUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.SM2;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.Security;

/**
 * SM2 非对称加密工具类（C1C3C2 模式）。
 * 公钥加密 / 私钥解密 / 私钥签名（SM2withSM3）/ 公钥验签。
 * 密钥均为 Hex 字符串：私钥 32字节，公钥 64字节（不含 04 前缀）。
 */
public class SM2Util {

    static {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    /**
     * SM2 公钥加密（C1C3C2），返回 Base64 密文。
     * publicKeyHex 为 64字节（128 hex 字符），不含 04 前缀。
     */
    public static String encryptByPublicKey(byte[] data, String publicKeyHex) {
        SM2 sm2 = SmUtil.sm2(null, "04" + publicKeyHex);
        return sm2.encryptBase64(data, KeyType.PublicKey);
    }

    /**
     * SM2 私钥解密，返回明文字节。
     */
    public static byte[] decryptByPrivateKey(String encryptedBase64, String privateKeyHex) {
        SM2 sm2 = SmUtil.sm2(privateKeyHex, null);
        return sm2.decrypt(Base64.decode(encryptedBase64), KeyType.PrivateKey);
    }

    /**
     * SM2withSM3 私钥签名，返回 Base64 签名。
     */
    public static String sign(byte[] data, String privateKeyHex) {
        SM2 sm2 = SmUtil.sm2(privateKeyHex, null);
        byte[] signature = sm2.sign(data);
        return Base64.encode(signature);
    }

    /**
     * SM2withSM3 公钥验签。
     * publicKeyHex 为 64字节（128 hex 字符），不含 04 前缀。
     */
    public static boolean verify(byte[] data, String signBase64, String publicKeyHex) {
        SM2 sm2 = SmUtil.sm2(null, "04" + publicKeyHex);
        return sm2.verify(data, Base64.decode(signBase64));
    }
}


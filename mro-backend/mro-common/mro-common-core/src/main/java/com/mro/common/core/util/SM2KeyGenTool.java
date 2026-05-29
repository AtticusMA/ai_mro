package com.mro.common.core.util;

import cn.hutool.core.util.HexUtil;
import cn.hutool.crypto.SmUtil;
import cn.hutool.crypto.asymmetric.SM2;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;

/**
 * 运行一次生成 SM2 密钥对，将输出内容存入 Nacos 或环境变量。
 * 使用方式：直接运行 main 方法。
 */
public class SM2KeyGenTool {

    public static void main(String[] args) {
        SM2 sm2 = SmUtil.sm2();
        BCECPrivateKey privateKey = (BCECPrivateKey) sm2.getPrivateKey();
        BCECPublicKey publicKey = (BCECPublicKey) sm2.getPublicKey();

        String privateKeyHex = HexUtil.encodeHexStr(privateKey.getD().toByteArray());
        // 去掉 04 前缀（非压缩点标识），取后 64 字节（x+y）
        String fullPublicKeyHex = HexUtil.encodeHexStr(publicKey.getQ().getEncoded(false));
        String publicKeyHex = fullPublicKeyHex.substring(2);

        System.out.println("=== SM2 密钥对（存入 Nacos: security.sm2.*）===");
        System.out.println("SM2 私钥 Hex（32字节）: " + privateKeyHex);
        System.out.println("SM2 公钥 Hex（64字节，无04前缀）: " + publicKeyHex);
        System.out.println();
        System.out.println("application.yml 配置：");
        System.out.println("security:");
        System.out.println("  sm2:");
        System.out.println("    private-key: " + privateKeyHex);
        System.out.println("    public-key: " + publicKeyHex);
    }
}

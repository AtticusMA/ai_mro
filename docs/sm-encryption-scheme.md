# 前后端请求国密改造方案（SM2+SM4+SM3）

## 1. 方案概述

本方案将原有 **RSA + AES + SHA256** 混合加密架构，全面替换为中国国家密码标准（国密）算法：

| 原算法 | 国密替换 | 说明 |
|--------|----------|------|
| RSA-2048（非对称加密/签名）| **SM2** | 椭圆曲线非对称算法，256位密钥强度等同于RSA-3072 |
| AES-256-CBC（对称加密）| **SM4-CBC** | 分组对称加密，128位密钥，块大小128位 |
| SHA256（摘要/签名）| **SM3** | 哈希算法，输出256位摘要 |

**核心思路与原方案一致，仅替换算法实现：**
- 使用 SM4 加密敏感字段（速度快，适合大数据量）
- 使用 SM2 加密 SM4 密钥（安全传输对称密钥）
- 使用 SM2/SM3 签名验证请求完整性（防篡改、防重放）

---

## 2. 国密算法说明

### 2.1 SM2（非对称加密）
- 基于椭圆曲线密码学（ECC），使用国密推荐曲线 sm2p256v1
- 密钥长度：256位（公私钥各32字节）
- 加密模式：C1C3C2（新标准，原C1C2C3已废弃）
- 签名算法：SM2withSM3（内置SM3摘要，无需额外指定）

### 2.2 SM4（对称加密）
- 分组长度：128位（16字节），密钥长度：128位（16字节）
- 注意：SM4 密钥为 **16字节**，原 AES-256 为 32字节，需调整
- 模式：SM4/CBC/PKCS7Padding（与AES/CBC用法一致）

### 2.3 SM3（摘要算法）
- 输出 256位（32字节）摘要
- 用于签名前的数据摘要计算

---

## 3. 密钥管理

### 3.1 密钥生成

**后端 SM2 密钥对（替换 RSA 密钥对）：**

```bash
# 使用 BouncyCastle 或 openssl（gmssl）生成
# 推荐在应用启动时通过代码生成，存储到配置中心（Nacos）

# gmssl 命令行（需安装 GmSSL）
gmssl sm2keygen -out sm2_private.pem
gmssl sm2 -in sm2_private.pem -pubout -out sm2_public.pem
```

**配置存储（替换 RSA 配置）：**

```yaml
# application.yml
security:
  sm2:
    private-key: "${SM2_PRIVATE_KEY}"   # Hex 编码的私钥，从 Nacos 注入
    public-key: "${SM2_PUBLIC_KEY}"     # Hex 编码的公钥，分发给前端
  encryption:
    enabled: true
    timestamp-tolerance: 300000         # 5分钟防重放窗口
```

### 3.2 前端密钥获取（与原方案一致）

```javascript
// 从后端接口获取 SM2 公钥（Hex 格式）
const SM2_PUBLIC_KEY = await fetchPublicKey(); // GET /api/security/public-key
```

---

## 4. 加密流程

### 4.1 前端请求加密流程（字段级加密，与原方案结构相同）

```
1. 生成随机 SM4 密钥（16字节）和 IV（16字节）
2. 识别业务对象中的敏感字段（@Encrypted 注解或字段名配置）
3. 使用 SM4-CBC 对敏感字段进行加密，替换原字段值（Base64编码）
4. 保持 JSON 结构不变，非敏感字段明文传输
5. 对 Body JSON 字符串 + timestamp + requestId 使用 SM3 计算摘要，再用 SM2 私钥签名
6. 使用后端 SM2 公钥加密 SM4 密钥和 IV
7. 发送请求：
```

**Header 传输元数据（Header key 与原方案保持一致，便于网关统一处理）：**

```http
POST /api/secure/user/save HTTP/1.1
Content-Type: application/json
X-Encrypted-Key: <SM2加密后的SM4密钥，Base64>
X-Encrypted-Iv:  <SM2加密后的IV，Base64>
X-Signature:     <SM2withSM3签名，Base64>
X-Timestamp:     1713340800000
X-Request-Id:    550e8400-e29b-41d4-a716-446655440000

{
  "username": "zhangsan",
  "mobile": "U2FsdGVkX1+...",    // SM4加密
  "idCard": "U2FsdGVkX1+...",    // SM4加密
  "address": "北京市朝阳区"       // 明文
}
```

### 4.2 后端解密流程（与原方案步骤一致，替换算法实现）

```
1. 从 Header 读取元数据（X-Encrypted-Key、X-Signature、X-Timestamp、X-Request-Id）
2. 验证时间戳（防重放，5分钟内有效）
3. 验证 requestId 唯一性（存入 Redis，有效期5分钟）
4. 读取 Body JSON 对象
5. 验证签名：对 Body JSON + timestamp + requestId 用 SM2 公钥验签（内置SM3摘要）
6. 使用后端 SM2 私钥解密 SM4 密钥和 IV
7. 递归遍历 JSON，识别 @Encrypted 标注字段
8. 使用 SM4 密钥解密敏感字段，还原原始值
9. 处理业务逻辑
10. 响应敏感字段可按需 SM4 加密返回
```

---

## 5. 后端代码实现（Java）

### 5.1 Maven 依赖

```xml
<!-- BouncyCastle 国密支持（替换原 commons-codec 的 RSA 使用） -->
<dependency>
    <groupId>org.bouncycastle</groupId>
    <artifactId>bcprov-jdk18on</artifactId>
    <version>1.78.1</version>
</dependency>
<!-- hutool-crypto 封装了国密，可选用 -->
<dependency>
    <groupId>cn.hutool</groupId>
    <artifactId>hutool-crypto</artifactId>
    <version>5.8.26</version>
</dependency>
```

### 5.2 SM2 工具类（替换 RSAUtil）

```java
package com.mro.common.util;

import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import cn.hutool.crypto.SmUtil;
import cn.hutool.crypto.asymmetric.SM2;
import cn.hutool.core.codec.Base64;

import java.security.Security;

public class SM2Util {

    static {
        // 注册 BouncyCastle 为 JCE Provider
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    /**
     * 使用 SM2 公钥加密（C1C3C2 模式）
     * @param data      待加密的明文字节
     * @param publicKeyHex 公钥 Hex 字符串（64字节，不含04前缀则自动补）
     * @return Base64 编码的密文
     */
    public static String encryptByPublicKey(byte[] data, String publicKeyHex) {
        SM2 sm2 = SmUtil.sm2(null, publicKeyHex);
        return sm2.encryptBase64(data, cn.hutool.crypto.KeyType.PublicKey);
    }

    /**
     * 使用 SM2 私钥解密
     * @param encryptedBase64 Base64 编码的密文
     * @param privateKeyHex   私钥 Hex 字符串（32字节）
     * @return 明文字节
     */
    public static byte[] decryptByPrivateKey(String encryptedBase64, String privateKeyHex) {
        SM2 sm2 = SmUtil.sm2(privateKeyHex, null);
        return sm2.decrypt(Base64.decode(encryptedBase64), cn.hutool.crypto.KeyType.PrivateKey);
    }

    /**
     * SM2 签名（内置 SM3 摘要，即 SM2withSM3）
     * @param data          待签名数据（UTF-8 字节）
     * @param privateKeyHex 私钥 Hex
     * @return Base64 编码的签名
     */
    public static String sign(byte[] data, String privateKeyHex) {
        SM2 sm2 = SmUtil.sm2(privateKeyHex, null);
        return sm2.signBase64(data, cn.hutool.crypto.KeyType.PrivateKey);
    }

    /**
     * SM2 验签
     * @param data         原始数据
     * @param signBase64   Base64 编码的签名
     * @param publicKeyHex 公钥 Hex
     * @return 验签是否通过
     */
    public static boolean verify(byte[] data, String signBase64, String publicKeyHex) {
        SM2 sm2 = SmUtil.sm2(null, publicKeyHex);
        return sm2.verify(data, Base64.decode(signBase64));
    }
}
```

### 5.3 SM4 工具类（替换 AESUtil）

```java
package com.mro.common.util;

import cn.hutool.core.codec.Base64;
import cn.hutool.crypto.SmUtil;
import cn.hutool.crypto.symmetric.SM4;
import cn.hutool.crypto.symmetric.SymmetricAlgorithm;

import javax.crypto.spec.IvParameterSpec;

public class SM4Util {

    private static final String TRANSFORMATION = "SM4/CBC/PKCS7Padding";

    /**
     * SM4 加密
     * @param plainText 明文字符串
     * @param key       16字节 SM4 密钥
     * @param iv        16字节 IV
     * @return Base64 编码密文
     */
    public static String encrypt(String plainText, byte[] key, byte[] iv) {
        SM4 sm4 = SmUtil.sm4(key);
        sm4.setMode(cn.hutool.crypto.Mode.CBC);
        sm4.setIv(iv);
        return sm4.encryptBase64(plainText);
    }

    /**
     * SM4 解密
     * @param encryptedBase64 Base64 编码密文
     * @param key             16字节 SM4 密钥
     * @param iv              16字节 IV
     * @return 明文字符串
     */
    public static String decrypt(String encryptedBase64, byte[] key, byte[] iv) {
        SM4 sm4 = SmUtil.sm4(key);
        sm4.setMode(cn.hutool.crypto.Mode.CBC);
        sm4.setIv(iv);
        return sm4.decryptStr(encryptedBase64);
    }
}
```

### 5.4 SM3 摘要工具类（签名时的数据摘要，可选独立使用）

```java
package com.mro.common.util;

import cn.hutool.crypto.SmUtil;
import cn.hutool.core.codec.Base64;

public class SM3Util {

    /**
     * 计算 SM3 摘要
     * @return Hex 字符串（64位）
     */
    public static String digest(String data) {
        return SmUtil.sm3(data);
    }

    /**
     * 验证 SM3 摘要
     */
    public static boolean verify(String data, String digestHex) {
        return digestHex.equals(SmUtil.sm3(data));
    }
}
```

### 5.5 解密拦截器（替换 DecryptInterceptor，算法部分变更）

```java
package com.mro.common.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mro.common.util.FieldEncryptionUtil;
import com.mro.common.util.SM2Util;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class SmDecryptInterceptor implements HandlerInterceptor {

    // ---- 国密配置（替换原 security.rsa.*）----
    @Value("${security.sm2.private-key}")
    private String sm2PrivateKey;

    @Value("${security.sm2.public-key}")
    private String sm2PublicKey;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private static final long TIMESTAMP_TOLERANCE = 5 * 60 * 1000L;

    // Header key 与原方案保持不变，方便迁移
    private static final String HEADER_ENCRYPTED_KEY = "X-Encrypted-Key";
    private static final String HEADER_ENCRYPTED_IV  = "X-Encrypted-Iv";
    private static final String HEADER_SIGNATURE     = "X-Signature";
    private static final String HEADER_TIMESTAMP     = "X-Timestamp";
    private static final String HEADER_REQUEST_ID    = "X-Request-Id";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String bodyJson = readBody(request);
        if (!StringUtils.hasText(bodyJson)) {
            return true;
        }

        String encryptedKey = request.getHeader(HEADER_ENCRYPTED_KEY);
        if (!StringUtils.hasText(encryptedKey)) {
            // 非加密请求，直接放行
            request.setAttribute("requestBody", bodyJson);
            return true;
        }

        String encryptedIv = request.getHeader(HEADER_ENCRYPTED_IV);
        String signature   = request.getHeader(HEADER_SIGNATURE);
        String timestampStr = request.getHeader(HEADER_TIMESTAMP);
        String requestId   = request.getHeader(HEADER_REQUEST_ID);

        // 1. 验证时间戳
        long timestamp = Long.parseLong(timestampStr);
        if (Math.abs(System.currentTimeMillis() - timestamp) > TIMESTAMP_TOLERANCE) {
            log.warn("SM请求已过期，timestamp: {}", timestamp);
            sendError(response, 4001, "请求已过期");
            return false;
        }

        // 2. 验证 requestId 唯一性（防重放）
        String redisKey = "sm:req:id:" + requestId;
        Boolean isNew = redisTemplate.opsForValue().setIfAbsent(redisKey, "1", 5, TimeUnit.MINUTES);
        if (!Boolean.TRUE.equals(isNew)) {
            log.warn("SM请求重复，requestId: {}", requestId);
            sendError(response, 4003, "请求重复");
            return false;
        }

        // 3. 验证签名（SM2withSM3）
        String signData = bodyJson + timestamp + requestId;
        if (!SM2Util.verify(signData.getBytes("UTF-8"), signature, sm2PublicKey)) {
            log.warn("SM签名验证失败，requestId: {}", requestId);
            sendError(response, 4002, "签名验证失败");
            return false;
        }

        // 4. 用 SM2 私钥解密 SM4 密钥和 IV
        byte[] sm4Key = SM2Util.decryptByPrivateKey(encryptedKey, sm2PrivateKey);
        byte[] sm4Iv  = SM2Util.decryptByPrivateKey(encryptedIv, sm2PrivateKey);

        // 5. 解析 JSON 并递归解密 @Encrypted 字段
        Map<String, Object> bodyMap = objectMapper.readValue(bodyJson,
                new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
        FieldEncryptionUtil.decryptFields(bodyMap, sm4Key, sm4Iv);

        request.setAttribute("decryptedBody", objectMapper.writeValueAsString(bodyMap));
        request.setAttribute("sm4Key", sm4Key);
        request.setAttribute("sm4Iv", sm4Iv);

        log.info("SM请求解密成功，requestId: {}", requestId);
        return true;
    }

    private String readBody(HttpServletRequest request) throws Exception {
        BufferedReader reader = request.getReader();
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) sb.append(line);
        return sb.toString().trim();
    }

    private void sendError(HttpServletResponse response, int code, String msg) throws Exception {
        response.setStatus(401);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(String.format("{\"code\":%d,\"msg\":\"%s\"}", code, msg));
    }
}
```

### 5.6 FieldEncryptionUtil 改造点

`FieldEncryptionUtil` 类只需将内部调用替换：

```java
// 原：AESUtil.encrypt(plainText, aesKey, iv)
// 改：SM4Util.encrypt(plainText, sm4Key, sm4Iv)

// 原：AESUtil.decrypt(encryptedText, aesKey, iv)
// 改：SM4Util.decrypt(encryptedText, sm4Key, sm4Iv)
```

其余递归逻辑、`@Encrypted` 注解不变。

### 5.7 公钥接口（替换为返回 SM2 公钥）

```java
@GetMapping("/api/security/public-key")
public R<String> getPublicKey(@Value("${security.sm2.public-key}") String sm2PublicKey) {
    return R.ok(sm2PublicKey); // 返回 Hex 格式 SM2 公钥
}
```

### 5.8 application.yml 配置变更

```yaml
# 删除
# security:
#   rsa:
#     private-key: ...
#     public-key: ...

# 新增
security:
  sm2:
    private-key: ${SM2_PRIVATE_KEY}   # Hex，32字节私钥
    public-key: ${SM2_PUBLIC_KEY}     # Hex，64字节公钥（不含04前缀）
  encryption:
    enabled: true
    timestamp-tolerance: 300000
```

---

## 6. 前端代码实现（JavaScript）

### 6.1 依赖替换

```bash
# 删除原依赖
# npm uninstall jsencrypt

# 新增国密库（sm-crypto 是目前最常用的前端国密库）
npm install sm-crypto
# uuid 和 axios 保持不变
```

### 6.2 国密加密服务（替换 EncryptionService）

```javascript
import { sm2, sm4 } from 'sm-crypto';
import { v4 as uuidv4 } from 'uuid';

/**
 * 国密加密服务（SM2 + SM4 + SM3）
 * 替换原 EncryptionService（RSA + AES + SHA256）
 */
class SmEncryptionService {
  /**
   * @param {string} sm2PublicKey - 后端 SM2 公钥（Hex，64字节，不含04前缀）
   * @param {string} [sm2PrivateKey] - 前端 SM2 私钥（Hex，仅签名时使用）
   */
  constructor(sm2PublicKey, sm2PrivateKey = null) {
    this.sm2PublicKey  = sm2PublicKey;
    this.sm2PrivateKey = sm2PrivateKey;
  }

  // ---------- SM4 工具 ----------

  /** 生成随机 SM4 密钥（16字节 = 32位 Hex） */
  generateSM4Key() {
    const arr = new Uint8Array(16);
    crypto.getRandomValues(arr);
    return Array.from(arr).map(b => b.toString(16).padStart(2, '0')).join('');
  }

  /** 生成随机 IV（16字节） */
  generateIV() {
    const arr = new Uint8Array(16);
    crypto.getRandomValues(arr);
    return Array.from(arr).map(b => b.toString(16).padStart(2, '0')).join('');
  }

  /**
   * SM4-CBC 加密单个字段
   * @param {string} plainText 明文
   * @param {string} keyHex   SM4密钥（Hex）
   * @param {string} ivHex    IV（Hex）
   * @returns {string} Base64密文
   */
  sm4Encrypt(plainText, keyHex, ivHex) {
    // sm-crypto sm4 加密，返回十六进制密文
    const encryptedHex = sm4.encrypt(plainText, keyHex, { mode: 'cbc', iv: ivHex });
    // 转为 Base64 便于传输
    return btoa(encryptedHex.match(/.{1,2}/g).map(b => String.fromCharCode(parseInt(b, 16))).join(''));
  }

  // ---------- SM2 工具 ----------

  /**
   * SM2 加密（C1C3C2 模式，cipherMode=1）
   * @param {string} dataHex 待加密数据（Hex）
   * @returns {string} Base64密文
   */
  sm2Encrypt(dataHex) {
    const encryptedHex = sm2.doEncrypt(dataHex, this.sm2PublicKey, 1); // 1 = C1C3C2
    return btoa(encryptedHex.match(/.{1,2}/g).map(b => String.fromCharCode(parseInt(b, 16))).join(''));
  }

  /**
   * SM2withSM3 签名
   * @param {string} data 待签名字符串
   * @returns {string} Base64签名
   */
  sm2Sign(data) {
    if (!this.sm2PrivateKey) throw new Error('未配置前端 SM2 私钥，无法签名');
    const msgHex = Array.from(new TextEncoder().encode(data))
                        .map(b => b.toString(16).padStart(2, '0')).join('');
    const signHex = sm2.doSignature(msgHex, this.sm2PrivateKey, { hash: true }); // hash:true 内置SM3
    return btoa(signHex.match(/.{1,2}/g).map(b => String.fromCharCode(parseInt(b, 16))).join(''));
  }

  // ---------- 字段加密 ----------

  /**
   * 递归加密敏感字段（逻辑与原方案完全一致，仅加密函数替换）
   */
  encryptSensitiveFields(obj, sensitiveFields, keyHex, ivHex) {
    if (!obj || typeof obj !== 'object') return obj;
    if (Array.isArray(obj)) {
      return obj.map(item => this.encryptSensitiveFields(item, sensitiveFields, keyHex, ivHex));
    }
    const result = { ...obj };
    for (const [fieldName, value] of Object.entries(result)) {
      if (value == null) continue;
      if (sensitiveFields.includes(fieldName) && typeof value === 'string') {
        result[fieldName] = this.sm4Encrypt(value, keyHex, ivHex); // 替换为 SM4
      } else if (typeof value === 'object') {
        result[fieldName] = this.encryptSensitiveFields(value, sensitiveFields, keyHex, ivHex);
      }
    }
    return result;
  }

  // ---------- 构建完整加密请求 ----------

  /**
   * 构建加密请求（Header 模式，字段级加密）
   * 返回结构与原方案完全一致，业务代码无需修改
   *
   * @param {object} data           原始业务数据
   * @param {string[]} sensitiveFields 敏感字段列表
   * @returns {{ body, encryptedKey, encryptedIv, signature, timestamp, requestId }}
   */
  buildEncryptedRequest(data, sensitiveFields = ['mobile', 'idCard', 'password']) {
    // 1. 生成 SM4 密钥和 IV
    const sm4KeyHex = this.generateSM4Key();
    const ivHex     = this.generateIV();

    // 2. 递归加密敏感字段
    const clonedData   = JSON.parse(JSON.stringify(data));
    const encryptedData = this.encryptSensitiveFields(clonedData, sensitiveFields, sm4KeyHex, ivHex);
    const bodyJson      = JSON.stringify(encryptedData);

    // 3. 用 SM2 公钥加密 SM4 密钥和 IV（替换原 RSA 加密）
    const encryptedKey = this.sm2Encrypt(sm4KeyHex);
    const encryptedIv  = this.sm2Encrypt(ivHex);

    // 4. 生成签名参数
    const timestamp = Date.now();
    const requestId = uuidv4();

    // 5. SM2withSM3 签名（替换原 SHA256 摘要）
    const signData  = `${bodyJson}${timestamp}${requestId}`;
    const signature = this.sm2Sign(signData);

    return {
      body: encryptedData,
      bodyJson,
      encryptedKey,   // Header: X-Encrypted-Key
      encryptedIv,    // Header: X-Encrypted-Iv
      signature,      // Header: X-Signature
      timestamp,      // Header: X-Timestamp
      requestId       // Header: X-Request-Id
    };
  }
}

export default SmEncryptionService;
```

### 6.3 Axios 拦截器（替换原拦截器，结构不变）

```javascript
import SmEncryptionService from './SmEncryptionService';

const SENSITIVE_FIELDS = ['mobile', 'idCard', 'password', 'bankCard'];

let encryptionService = null;

// 初始化：获取 SM2 公钥（替换原 RSA 公钥获取）
async function initEncryption() {
  const { data } = await axios.get('/api/security/public-key');
  // data 是 SM2 公钥 Hex 字符串
  // sm2PrivateKey 为前端私钥，如无双向签名需求可为 null（签名改为后端验签客户端证书）
  encryptionService = new SmEncryptionService(data.data, import.meta.env.VITE_SM2_PRIVATE_KEY);
}

// 请求拦截器（与原方案完全一致，仅 encryptionService 实现替换）
apiClient.interceptors.request.use(async (config) => {
  if (config.url.startsWith('/secure/')) {
    if (!encryptionService) await initEncryption();

    const encrypted = encryptionService.buildEncryptedRequest(config.data, SENSITIVE_FIELDS);

    config.headers['X-Encrypted-Key'] = encrypted.encryptedKey;
    config.headers['X-Encrypted-Iv']  = encrypted.encryptedIv;
    config.headers['X-Signature']     = encrypted.signature;
    config.headers['X-Timestamp']     = encrypted.timestamp.toString();
    config.headers['X-Request-Id']    = encrypted.requestId;
    config.data = encrypted.body;
  }
  return config;
});
```

---

## 7. 改造对照表

| 改造点 | 原方案 | 国密方案 | 改动范围 |
|--------|--------|----------|----------|
| 非对称加密算法 | RSA-2048 | SM2（sm2p256v1） | 工具类替换 |
| 对称加密算法 | AES-256-CBC | SM4-CBC | 工具类替换 |
| 密钥长度（对称）| 32字节 | **16字节** | 前端生成密钥长度调整 |
| 签名算法 | SHA256withRSA | SM2withSM3 | 工具类替换 |
| 摘要算法 | SHA256 | SM3 | 工具类替换 |
| 后端依赖 | JCA 内置 | BouncyCastle + hutool-crypto | pom.xml 新增 |
| 前端依赖 | crypto-js + jsencrypt | **sm-crypto** | package.json 替换 |
| Header 键名 | X-Encrypted-Key 等 | **不变** | 无需修改 |
| `@Encrypted` 注解 | 不变 | 不变 | 无需修改 |
| `FieldEncryptionUtil` | 不变 | 内部调用替换为 SM4 | 最小改动 |
| `DecryptInterceptor` | RSA 解密 + AES 解密 | SM2 解密 + SM4 解密 | 替换工具类调用 |
| 配置 Key | security.rsa.* | **security.sm2.*** | yml 修改 |
| Redis 防重放 | request:id:* | sm:req:id:* | 可选区分 |

---

## 8. 密钥生成工具（后端初始化脚本）

```java
/**
 * 运行一次，生成 SM2 密钥对并输出，存入 Nacos 或环境变量
 */
public static void main(String[] args) {
    SM2 sm2 = SmUtil.sm2();
    BCECPrivateKey privateKey = (BCECPrivateKey) sm2.getPrivateKey();
    BCECPublicKey  publicKey  = (BCECPublicKey)  sm2.getPublicKey();

    String privateKeyHex = HexUtil.encodeHexStr(privateKey.getD().toByteArray());
    String publicKeyHex  = HexUtil.encodeHexStr(
        publicKey.getQ().getEncoded(false)).substring(2); // 去掉 04 前缀

    System.out.println("SM2 私钥（Hex）: " + privateKeyHex);
    System.out.println("SM2 公钥（Hex）: " + publicKeyHex);
}
```

---

## 9. 安全增强（与原方案一致，无变化）

- **防重放**：时间戳（5分钟窗口）+ requestId Redis 唯一性校验
- **防篡改**：SM2withSM3 签名覆盖 Body + timestamp + requestId
- **密钥轮换**：SM2 密钥每 90 天轮换，通过 keyId Header 支持多版本共存
- **传输安全**：国密改造不替代 HTTPS/TLS，建议配合 TLCP（国密TLS）部署

---

## 10. 实施步骤

**阶段一：后端**
1. `pom.xml` 新增 `bcprov-jdk18on` + `hutool-crypto`
2. 新建 `SM2Util`、`SM4Util`、`SM3Util` 工具类
3. `FieldEncryptionUtil` 内部调用替换为 SM4
4. 新建 `SmDecryptInterceptor` 替换 `DecryptInterceptor`
5. `application.yml` 替换 `security.rsa.*` 为 `security.sm2.*`
6. 运行密钥生成脚本，将密钥存入 Nacos/环境变量

**阶段二：前端**
1. `npm uninstall crypto-js jsencrypt` / `npm install sm-crypto`
2. 新建 `SmEncryptionService` 替换 `EncryptionService`
3. Axios 拦截器中替换 `new EncryptionService(rsaKey)` 为 `new SmEncryptionService(sm2Key, sm2PrivKey)`
4. `.env` 新增 `VITE_SM2_PRIVATE_KEY`（前端签名私钥，或走无私钥模式）

**阶段三：联调测试**
1. 验证 SM4 加密/解密一致性
2. 验证 SM2 加密密钥传输
3. 验证 SM2withSM3 签名校验
4. 验证防重放机制

---

## 11. 常见问题

### Q1: SM4 密钥只有 128位，是否比 AES-256 弱？
**A:** SM4 密钥 128位（16字节），AES-256 为 256位（32字节）。在已知攻击方法下 SM4-128 安全性与 AES-128 相当，满足国家商密合规要求。若需更高强度，可按 GM/T 0002 标准评估。

### Q2: sm-crypto 库是否可信？
**A:** `sm-crypto` 是目前前端国密使用最广泛的开源库（npm 周下载 10万+），但建议在生产环境做代码审计或选用经国密局认证的商业 SDK。

### Q3: 前端没有 SM2 私钥如何签名？
**A:** 两种选择：①前端生成临时 SM2 密钥对，公钥随请求上传，后端用上传的公钥验签（需防中间人）；②省略前端签名，仅做后端到前端的验签方向（单向签名），防重放靠 timestamp + requestId 保证。

### Q4: 与原方案是否可以并行切换？
**A:** 可以。在 `DecryptInterceptor` 中通过 Header `X-Crypto-Version: sm`（或无此Header默认RSA）兼容新旧请求，分阶段切换。

---

**文档版本:** 1.0
**基于原文档:** encryptionsignature&front-back-end.md v2.0
**最后更新:** 2026-05-27
**变更记录:**
- v1.0 (2026-05-27): 基于 RSA+AES+SHA256 方案完成国密（SM2+SM4+SM3）改造

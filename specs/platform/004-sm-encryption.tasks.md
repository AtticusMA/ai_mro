---
id: PLT-004
plan: platform/004-sm-encryption.plan.md
created: 2026-05-27
updated: 2026-05-27
---

# Tasks: 前后端传输国密加密（SM2+SM4+HMAC-SM3）

| ID | 任务 | 负责人 | 依赖 | DoD | 状态 |
|----|------|--------|------|-----|------|
| T-001 | pom.xml 新增 `bcprov-jdk18on:1.78.1` + `hutool-crypto:5.8.26` 依赖 | @dev | - | 依赖引入后编译通过；`Security.getProvider("BC")` 可获取 | done |
| T-002 | 编写 SM2 密钥对生成工具脚本（main 方法），输出私钥 Hex（32字节）和公钥 Hex（64字节，无 04 前缀） | @dev | T-001 | 运行脚本输出两行 Hex 字符串；长度校验正确 | done |
| T-003 | 实现 `SM2Util`：公钥加密（C1C3C2）、私钥解密、私钥签名、公钥验签 | @dev | T-001 | 单测：加密→解密往返一致；签名→验签通过；Code Review | done |
| T-004 | 实现 `SM4Util`：SM4-CBC/PKCS7Padding 加密、解密（16字节密钥+IV） | @dev | T-001 | 单测：加密→解密往返一致；中文字符正确处理；Code Review | done |
| T-005 | 实现 `SM3Util`：HMAC-SM3 计算与验证 | @dev | T-001 | 单测：相同输入输出一致；篡改消息后验证失败；Code Review | done |
| T-006 | `application.yml` 新增 `security.sm2.*` 配置项（private-key / public-key / encryption.enabled / timestamp-tolerance） | @dev | T-001 | 配置项注入到 `@ConfigurationProperties` Bean；缺失时启动失败并打印明确错误信息 | done |
| T-007 | 实现 `SecurityController.getPublicKey`：`GET /api/security/public-key` 返回 SM2 公钥 Hex | @dev | T-003 T-006 | 接口返回 64字节 Hex；Gateway 白名单放行配置；Code Review | done |
| T-008 | `FieldEncryptionUtil` 内部调用从 AESUtil 改为 SM4Util | @dev | T-004 | 单测：含 `@Encrypted` 嵌套对象 + 集合字段加解密一致；Code Review | done |
| T-009 | 实现 `SmDecryptInterceptor`：读取 5 个 Header → 验 timestamp → Redis SETNX requestId → SM2 解密 SM4 Key/IV → HMAC-SM3 验签 → FieldEncryptionUtil 解密字段 | @dev | T-003 T-005 T-008 | 单测（Mock Redis）：timestamp 超期→4001；requestId 重复→4003；HMAC 失败→4002；正常流程→字段解密成功；Code Review | done |
| T-010 | 注册 `SmDecryptInterceptor` 到 WebMvcConfigurer，拦截 `/api/**`；`enabled=false` 时跳过 | @dev | T-009 | 配置类注册完成；`enabled=false` 启动后直接放行请求；Code Review | done |
| T-011 | 前端 `npm install sm-crypto`，移除 `jsencrypt` / `crypto-js` | @dev | - | `package.json` 更新；`npm run build` 编译通过 | done |
| T-012 | 实现前端 `SmEncryptionService`：generateSM4Key / generateIV / sm4Encrypt / sm2Encrypt / hmacSm3Sign / buildEncryptedRequest | @dev | T-011 | 单测（Vitest）：buildEncryptedRequest 输出含 5 个加密元数据字段；SM4 加密字段与明文不同；Code Review | done |
| T-013 | 前端公钥初始化：启动时调用 `/api/security/public-key` 获取 SM2 公钥，内存缓存；SmEncryptionService 单例持有 | @dev | T-012 | 首次请求触发公钥获取；刷新页面重新获取；公钥非空时不重复请求；Code Review | done |
| T-014 | Axios 请求拦截器接入 `SmEncryptionService`：全部请求自动添加 5 个加密 Header，Body 敏感字段自动 SM4 加密 | @dev | T-013 | 拦截器覆盖全部 `/api/**` 请求；Header 中包含 X-Encrypted-Key/Iv/Signature/Timestamp/Request-Id；Code Review | done |
| T-015 | 前后端集成联调：前端 `buildEncryptedRequest` → 后端 `SmDecryptInterceptor` → Controller 收到明文数据 | @dev | T-010 T-014 | 含 `mobile` + `idCard` 字段的保存请求联调通过；Controller 打印明文值正确；抓包确认敏感字段密文传输 | done |
| T-016 | 端到端验收：登录 → 发起含敏感字段请求 → 数据库入库明文正确；验收清单全绿 | @dev | T-015 | spec 第 9 节全部验收项通过；`npm run specs:validate` 通过 | doing |

> 状态枚举：todo / doing / review / done / blocked

---
id: ADR-001
title: 密码加密改用 BCrypt
status: accepted
date: 2026-05-23
deciders: ['@arch']
---

# ADR-001: 密码加密改用 BCrypt

## Context

原始草稿 `项目需求文档.md` 写的是 MD5 加密用户密码。MD5 是快速不可逆哈希，但已被广泛证明不适合密码场景：
- 无 salt 时彩虹表攻击成本极低；
- 无可调代价因子，无法随硬件提升。

本系统涉及企业内部用户/部门数据，安全性要求至少要满足主流合规与密码学最佳实践。

## Decision

- 用户密码统一使用 **BCrypt**（cost factor 默认 10，可在 Spring Security 配置中调整）。
- 数据库 `sys_user.password` 字段长度调整为 `VARCHAR(255)`，足以存储 BCrypt 输出（60 字符）并预留扩展。
- 任何引入新的认证流程（重置密码、初次设密、Excel 导入用户）都必须通过 `PasswordEncoder` 接口，禁止裸写哈希。

## Consequences

### 正面
- 自带 salt，相同明文哈希结果不同，杜绝彩虹表。
- cost factor 可随硬件升级调高，无需更换算法。
- 与 Spring Security 默认实现天然契合。

### 负面
- 比 MD5 慢约 2~3 个数量级；登录场景无感，但密码字典攻击成本对攻击者大幅升高（这就是优点）。
- 无法回滚成 MD5（这是预期内的单向迁移）。

## Alternatives Considered

| 方案 | 描述 | 拒绝理由 |
|------|------|----------|
| MD5 + salt | 加盐后再 MD5 | 仍无 cost factor，速度过快 |
| SHA-256 + salt | 高强度但同样无 cost factor | 同上 |
| Argon2id | 当前最优密码哈希 | Spring Security 支持需额外依赖；BCrypt 对本项目已足够 |
| PBKDF2 | 标准化但参数选择繁琐 | BCrypt 更简单，库支持更广 |

## References

- 关联 Spec：`auth/001-login-jwt.spec.md`
- 关联 Charter：`CHARTER.md` §4 安全

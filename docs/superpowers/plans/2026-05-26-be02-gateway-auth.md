# BE-02: gateway-service + auth-service

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement `gateway-service` (Spring Cloud Gateway with JWT validation via auth-service) and `auth-service` (login, JWT issuance, Redis blacklist, AuthDubboService provider).

**Architecture:** `gateway-service` is a non-blocking Spring Cloud Gateway routing all `/api/**` requests; it POSTs to `auth-service /internal/auth/verify` to validate JWT, then injects user context headers before forwarding to `manage-web`. `auth-service` is a Dubbo provider on port 20880 that owns the `mro_auth` MySQL schema, issues JWTs with JJWT, maintains Redis blacklist/cache.

**Tech Stack:** Spring Cloud Gateway (reactive), Spring Boot 3.3, JJWT 0.12, Spring Data Redis, Dubbo 3.3, MyBatis-Plus 3.5, BCrypt, Java 21 virtual threads

**Prerequisites:** BE-01 complete (`mro-common-core`, `mro-common-dubbo` installed in local Maven).

**Refs:** AUTH-001, PLAT-001, PLAT-002

---

## File Structure

```
mro-backend/
├── gateway-service/
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/mro/gateway/
│       │   ├── GatewayApplication.java
│       │   ├── filter/
│       │   │   └── AuthGlobalFilter.java        ← JWT validation filter
│       │   └── config/
│       │       └── GatewayConfig.java           ← CORS + whitelist config
│       └── main/resources/
│           ├── application.yml
│           └── bootstrap.yml                    ← Nacos config
├── auth-service/
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/mro/auth/
│       │   ├── AuthServiceApplication.java
│       │   ├── entity/
│       │   │   └── SysUser.java                 ← MyBatis-Plus entity (mirrors mro_auth view)
│       │   ├── mapper/
│       │   │   └── SysUserAuthMapper.java
│       │   ├── service/
│       │   │   ├── JwtService.java              ← JWT issue/verify/parse
│       │   │   ├── LoginService.java            ← login business logic
│       │   │   └── AuthDubboServiceImpl.java    ← Dubbo provider
│       │   ├── controller/
│       │   │   ├── AuthController.java          ← /api/auth/** HTTP endpoints
│       │   │   └── InternalAuthController.java  ← /internal/auth/verify (gateway call)
│       │   └── config/
│       │       └── SecurityConfig.java          ← BCrypt bean
│       └── main/resources/
│           ├── application.yml
│           ├── bootstrap.yml
│           └── db/migration/
│               └── V1__auth_schema.sql
│       └── test/java/com/mro/auth/
│           ├── service/JwtServiceTest.java
│           ├── service/LoginServiceTest.java
│           └── controller/AuthControllerTest.java
```

---

## Task 1: gateway-service Module

**Files:**
- Create: `mro-backend/gateway-service/pom.xml`
- Create: `mro-backend/gateway-service/src/main/java/com/mro/gateway/GatewayApplication.java`
- Create: `mro-backend/gateway-service/src/main/resources/application.yml`
- Create: `mro-backend/gateway-service/src/main/java/com/mro/gateway/filter/AuthGlobalFilter.java`
- Create: `mro-backend/gateway-service/src/main/java/com/mro/gateway/config/GatewayConfig.java`

- [ ] **Step 1: Create gateway-service POM**

```xml
<!-- mro-backend/gateway-service/pom.xml -->
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.mro</groupId>
        <artifactId>mro-backend</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </parent>

    <artifactId>gateway-service</artifactId>
    <name>MRO Gateway Service</name>

    <dependencies>
        <dependency>
            <groupId>com.mro</groupId>
            <artifactId>mro-common-core</artifactId>
        </dependency>
        <!-- Gateway is reactive — exclude spring-boot-starter-web if transitive -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-gateway</artifactId>
        </dependency>
        <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
        </dependency>
        <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-loadbalancer</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis-reactive</artifactId>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-api</artifactId>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-impl</artifactId>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-jackson</artifactId>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

- [ ] **Step 2: Create GatewayApplication**

```java
// mro-backend/gateway-service/src/main/java/com/mro/gateway/GatewayApplication.java
package com.mro.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class GatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}
```

- [ ] **Step 3: Create application.yml**

```yaml
# mro-backend/gateway-service/src/main/resources/application.yml
server:
  port: 8080

spring:
  application:
    name: gateway-service
  threads:
    virtual:
      enabled: true
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD:}
      database: 0
  cloud:
    nacos:
      discovery:
        server-addr: ${NACOS_ADDR:localhost:8848}
        namespace: dev
        group: DEFAULT_GROUP
      config:
        server-addr: ${NACOS_ADDR:localhost:8848}
        namespace: dev
        file-extension: yaml
    gateway:
      discovery:
        locator:
          enabled: false
      routes:
        # Auth service — direct route (no manage-web)
        - id: auth-service
          uri: lb://auth-service
          predicates:
            - Path=/api/auth/**
          filters:
            - StripPrefix=0
        # Everything else → manage-web (JWT validated first)
        - id: manage-web
          uri: lb://manage-web
          predicates:
            - Path=/api/**
          filters:
            - StripPrefix=0
      globalcors:
        cors-configurations:
          '[/**]':
            allowedOriginPatterns: "*"
            allowedMethods: "*"
            allowedHeaders: "*"
            allowCredentials: true
            maxAge: 3600

# JWT secret (override via Nacos in production)
jwt:
  secret: ${JWT_SECRET:mro-jwt-secret-key-must-be-at-least-32-chars}

# Public paths that skip JWT validation
gateway:
  whitelist:
    - /api/auth/login
    - /api/auth/refresh-token
    - /actuator/**
```

- [ ] **Step 4: Create AuthGlobalFilter**

```java
// mro-backend/gateway-service/src/main/java/com/mro/gateway/filter/AuthGlobalFilter.java
package com.mro.gateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mro.common.core.result.Result;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${gateway.whitelist}")
    private List<String> whitelist;

    private final ReactiveStringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public AuthGlobalFilter(ReactiveStringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public int getOrder() {
        return -100;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();

        if (isWhitelisted(path)) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized(exchange, 4011, "Token 无效或已过期");
        }

        String token = authHeader.substring(7);
        Jws<Claims> jws;
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            jws = Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
        } catch (JwtException e) {
            return unauthorized(exchange, 4011, "Token 无效或已过期");
        }

        Claims claims = jws.getPayload();
        String jti = claims.getId();

        return redisTemplate.hasKey("auth:blacklist:" + jti)
                .flatMap(blacklisted -> {
                    if (Boolean.TRUE.equals(blacklisted)) {
                        return unauthorized(exchange, 4012, "Token 已失效（已登出）");
                    }

                    String userId = claims.getSubject();
                    String deptId = claims.get("deptId", Long.class) != null
                            ? String.valueOf(claims.get("deptId", Long.class)) : "";
                    @SuppressWarnings("unchecked")
                    List<String> roles = (List<String>) claims.get("roles");
                    @SuppressWarnings("unchecked")
                    List<String> permissions = (List<String>) claims.get("permissions");

                    ServerHttpRequest mutated = exchange.getRequest().mutate()
                            .header("X-User-Id", userId)
                            .header("X-User-Dept-Id", deptId)
                            .header("X-User-Roles", roles != null ? String.join(",", roles) : "")
                            .header("X-User-Permissions", permissions != null ? String.join(",", permissions) : "")
                            .build();

                    return chain.filter(exchange.mutate().request(mutated).build());
                });
    }

    private boolean isWhitelisted(String path) {
        return whitelist.stream().anyMatch(p -> PATH_MATCHER.match(p, path));
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, int code, String msg) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        Result<Void> body = Result.fail(code, msg);
        try {
            byte[] bytes = objectMapper.writeValueAsBytes(body);
            DataBuffer buffer = response.bufferFactory().wrap(bytes);
            return response.writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            return response.setComplete();
        }
    }
}
```

- [ ] **Step 5: Verify gateway-service compiles**

```bash
cd mro-backend
mvn compile -pl gateway-service -am -DskipTests
```

Expected: `BUILD SUCCESS`

- [ ] **Step 6: Commit**

```bash
cd mro-backend
git add gateway-service/
git commit -m "feat(gateway): add JWT validation global filter with Redis blacklist check

Refs: AUTH-001, PLAT-001"
```

---

## Task 2: auth-service Module Setup + DB Schema

**Files:**
- Create: `mro-backend/auth-service/pom.xml`
- Create: `mro-backend/auth-service/src/main/resources/db/migration/V1__auth_schema.sql`
- Create: `mro-backend/auth-service/src/main/resources/application.yml`
- Create: `mro-backend/auth-service/src/main/java/com/mro/auth/AuthServiceApplication.java`

- [ ] **Step 1: Create auth-service POM**

```xml
<!-- mro-backend/auth-service/pom.xml -->
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.mro</groupId>
        <artifactId>mro-backend</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </parent>

    <artifactId>auth-service</artifactId>
    <name>MRO Auth Service</name>

    <dependencies>
        <dependency>
            <groupId>com.mro</groupId>
            <artifactId>mro-common-core</artifactId>
        </dependency>
        <dependency>
            <groupId>com.mro</groupId>
            <artifactId>mro-common-dubbo</artifactId>
        </dependency>
        <dependency>
            <groupId>com.mro</groupId>
            <artifactId>mro-common-data</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis</artifactId>
        </dependency>
        <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
        </dependency>
        <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
        </dependency>
        <dependency>
            <groupId>org.apache.dubbo</groupId>
            <artifactId>dubbo-spring-boot-starter</artifactId>
        </dependency>
        <dependency>
            <groupId>com.alibaba.nacos</groupId>
            <artifactId>nacos-client</artifactId>
        </dependency>
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
        </dependency>
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
        </dependency>
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-core</artifactId>
        </dependency>
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-mysql</artifactId>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-api</artifactId>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-impl</artifactId>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-jackson</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.mockito</groupId>
            <artifactId>mockito-core</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

- [ ] **Step 2: Create application.yml**

```yaml
# mro-backend/auth-service/src/main/resources/application.yml
server:
  port: 8082

spring:
  application:
    name: auth-service
  threads:
    virtual:
      enabled: true
  datasource:
    url: jdbc:mysql://${MYSQL_HOST:localhost}:3306/mro_auth?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai
    username: ${MYSQL_USER:root}
    password: ${MYSQL_PASSWORD:root}
    driver-class-name: com.mysql.cj.jdbc.Driver
  flyway:
    locations: classpath:db/migration
    baseline-on-migrate: true
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD:}
      database: 0
  cloud:
    nacos:
      discovery:
        server-addr: ${NACOS_ADDR:localhost:8848}
        namespace: dev
        group: DEFAULT_GROUP
      config:
        server-addr: ${NACOS_ADDR:localhost:8848}
        namespace: dev
        file-extension: yaml

dubbo:
  application:
    name: auth-service
  registry:
    address: nacos://${NACOS_ADDR:localhost:8848}?namespace=dev
  protocol:
    name: dubbo
    port: 20880
  provider:
    group: mro
    version: 1.0.0

mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.slf4j.Slf4jImpl
  global-config:
    db-config:
      logic-delete-field: isDeleted
      logic-delete-value: 1
      logic-not-delete-value: 0

jwt:
  secret: ${JWT_SECRET:mro-jwt-secret-key-must-be-at-least-32-chars}
  access-token-expiry: 7200      # seconds (2h)
  refresh-token-expiry: 604800   # seconds (7d)
```

- [ ] **Step 3: Create Flyway migration — auth schema**

```sql
-- mro-backend/auth-service/src/main/resources/db/migration/V1__auth_schema.sql
CREATE DATABASE IF NOT EXISTS mro_auth CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE mro_auth;

-- auth-service只读 sys_user 基本信息（用于登录验证）
-- sys_user 的主表由 system-service 管理 (mro_system schema)
-- auth-service 维护一个本地轻量副本，包含登录验证所需字段
CREATE TABLE IF NOT EXISTS auth_user_credential (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id     BIGINT       NOT NULL COMMENT '对应 mro_system.sys_user.id',
    username    VARCHAR(64)  NOT NULL COMMENT '登录名',
    password    VARCHAR(100) NOT NULL COMMENT 'BCrypt 加密密码',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '0 禁用 / 1 启用',
    dept_id     BIGINT       NOT NULL COMMENT '所属部门ID，用于构建JWT',
    is_deleted  TINYINT      NOT NULL DEFAULT 0,
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME              DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_id (user_id),
    UNIQUE KEY uk_username (username),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='登录凭证表';

CREATE TABLE IF NOT EXISTS auth_login_log (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    user_id     BIGINT       NOT NULL,
    username    VARCHAR(64)  NOT NULL,
    ip_address  VARCHAR(50)           DEFAULT NULL,
    user_agent  VARCHAR(255)          DEFAULT NULL,
    success     TINYINT      NOT NULL COMMENT '1 成功 / 0 失败',
    fail_reason VARCHAR(100)          DEFAULT NULL,
    login_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_user_id (user_id),
    KEY idx_login_time (login_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='登录日志';

-- Default admin credential (password = 'Admin@123', BCrypt encoded)
INSERT IGNORE INTO auth_user_credential (user_id, username, password, status, dept_id, create_time)
VALUES (1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBpwTTyBumqnm2', 1, 1, NOW());
```

- [ ] **Step 4: Create AuthServiceApplication**

```java
// mro-backend/auth-service/src/main/java/com/mro/auth/AuthServiceApplication.java
package com.mro.auth;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
@EnableDubbo
public class AuthServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}
```

- [ ] **Step 5: Verify auth-service compiles**

```bash
cd mro-backend
mvn compile -pl auth-service -am -DskipTests
```

Expected: `BUILD SUCCESS`

- [ ] **Step 6: Commit**

```bash
cd mro-backend
git add auth-service/
git commit -m "chore(auth): add auth-service module, POM, application.yml, Flyway migration

Refs: AUTH-001"
```

---

## Task 3: JwtService

**Files:**
- Create: `mro-backend/auth-service/src/main/java/com/mro/auth/service/JwtService.java`
- Test: `mro-backend/auth-service/src/test/java/com/mro/auth/service/JwtServiceTest.java`

- [ ] **Step 1: Write failing tests**

```java
// mro-backend/auth-service/src/test/java/com/mro/auth/service/JwtServiceTest.java
package com.mro.auth.service;

import com.mro.auth.entity.AuthUserCredential;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setup() {
        jwtService = new JwtService(
                "mro-test-secret-key-must-be-at-least-32-chars",
                7200L,
                604800L
        );
    }

    @Test
    void generateAccessToken_containsExpectedClaims() {
        String token = jwtService.generateAccessToken(1L, "admin", 10L,
                List.of("admin"), List.of("dept:list"));
        assertThat(token).isNotBlank();
        var claims = jwtService.parseAccessToken(token);
        assertThat(claims.getSubject()).isEqualTo("1");
        assertThat(claims.get("username")).isEqualTo("admin");
        assertThat(claims.get("deptId", Long.class)).isEqualTo(10L);
    }

    @Test
    void generateRefreshToken_isDifferentFromAccessToken() {
        String access = jwtService.generateAccessToken(1L, "admin", 10L, List.of(), List.of());
        String refresh = jwtService.generateRefreshToken(1L);
        assertThat(access).isNotEqualTo(refresh);
    }

    @Test
    void parseAccessToken_invalidToken_throwsException() {
        assertThatThrownBy(() -> jwtService.parseAccessToken("not.a.valid.token"))
                .isInstanceOf(io.jsonwebtoken.JwtException.class);
    }

    @Test
    void extractJti_returnsUuid() {
        String token = jwtService.generateAccessToken(1L, "admin", 10L, List.of(), List.of());
        String jti = jwtService.extractJti(token);
        assertThat(jti).matches("[0-9a-f-]{36}");
    }

    @Test
    void extractUserId_fromRefreshToken() {
        String refresh = jwtService.generateRefreshToken(42L);
        Long userId = jwtService.extractUserIdFromRefreshToken(refresh);
        assertThat(userId).isEqualTo(42L);
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
cd mro-backend
mvn test -pl auth-service -am -Dtest=JwtServiceTest -DskipTests=false
```

Expected: FAIL — `JwtService cannot be resolved`

- [ ] **Step 3: Create AuthUserCredential entity (needed by JwtService test)**

```java
// mro-backend/auth-service/src/main/java/com/mro/auth/entity/AuthUserCredential.java
package com.mro.auth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("auth_user_credential")
public class AuthUserCredential {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String username;

    private String password;

    private Integer status;

    private Long deptId;

    @TableLogic
    private Integer isDeleted;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
```

- [ ] **Step 4: Implement JwtService**

```java
// mro-backend/auth-service/src/main/java/com/mro/auth/service/JwtService.java
package com.mro.auth.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class JwtService {

    private final SecretKey secretKey;
    private final long accessTokenExpiry;
    private final long refreshTokenExpiry;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expiry}") long accessTokenExpiry,
            @Value("${jwt.refresh-token-expiry}") long refreshTokenExpiry) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiry = accessTokenExpiry;
        this.refreshTokenExpiry = refreshTokenExpiry;
    }

    public String generateAccessToken(Long userId, String username, Long deptId,
                                      List<String> roles, List<String> permissions) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(String.valueOf(userId))
                .claim("username", username)
                .claim("deptId", deptId)
                .claim("roles", roles)
                .claim("permissions", permissions)
                .issuedAt(new Date(now))
                .expiration(new Date(now + accessTokenExpiry * 1000))
                .signWith(secretKey)
                .compact();
    }

    public String generateRefreshToken(Long userId) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(String.valueOf(userId))
                .claim("type", "refresh")
                .issuedAt(new Date(now))
                .expiration(new Date(now + refreshTokenExpiry * 1000))
                .signWith(secretKey)
                .compact();
    }

    public Claims parseAccessToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Claims parseRefreshToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        if (!"refresh".equals(claims.get("type"))) {
            throw new JwtException("Not a refresh token");
        }
        return claims;
    }

    public String extractJti(String token) {
        return parseAccessToken(token).getId();
    }

    public Long extractUserIdFromRefreshToken(String token) {
        return Long.valueOf(parseRefreshToken(token).getSubject());
    }

    public long getRemainingTtl(Claims claims) {
        long expMs = claims.getExpiration().getTime();
        long nowMs = System.currentTimeMillis();
        return Math.max(0, (expMs - nowMs) / 1000);
    }

    public long getAccessTokenExpiry() {
        return accessTokenExpiry;
    }
}
```

- [ ] **Step 5: Run tests**

```bash
cd mro-backend
mvn test -pl auth-service -am -Dtest=JwtServiceTest -DskipTests=false
```

Expected: `Tests run: 5, Failures: 0, Errors: 0`

- [ ] **Step 6: Commit**

```bash
cd mro-backend
git add auth-service/src/
git commit -m "feat(auth): add JwtService for token generation and parsing

Refs: AUTH-001"
```

---

## Task 4: LoginService + AuthUserCredentialMapper

**Files:**
- Create: `mro-backend/auth-service/src/main/java/com/mro/auth/mapper/AuthUserCredentialMapper.java`
- Create: `mro-backend/auth-service/src/main/java/com/mro/auth/service/LoginService.java`
- Test: `mro-backend/auth-service/src/test/java/com/mro/auth/service/LoginServiceTest.java`

- [ ] **Step 1: Write failing tests**

```java
// mro-backend/auth-service/src/test/java/com/mro/auth/service/LoginServiceTest.java
package com.mro.auth.service;

import com.mro.auth.entity.AuthUserCredential;
import com.mro.auth.mapper.AuthUserCredentialMapper;
import com.mro.common.core.exception.BusinessException;
import com.mro.common.core.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class LoginServiceTest {

    @Mock
    private AuthUserCredentialMapper credentialMapper;

    @Mock
    private org.springframework.data.redis.core.StringRedisTemplate redisTemplate;

    private JwtService jwtService;
    private LoginService loginService;
    private BCryptPasswordEncoder encoder;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        jwtService = new JwtService(
                "mro-test-secret-key-must-be-at-least-32-chars",
                7200L, 604800L);
        encoder = new BCryptPasswordEncoder();
        loginService = new LoginService(credentialMapper, jwtService, encoder, redisTemplate);
    }

    @Test
    void login_correctPassword_returnsTokens() {
        AuthUserCredential cred = new AuthUserCredential();
        cred.setUserId(1L);
        cred.setUsername("admin");
        cred.setPassword(encoder.encode("Admin@123"));
        cred.setStatus(1);
        cred.setDeptId(10L);

        when(credentialMapper.findByUsername("admin")).thenReturn(cred);

        var result = loginService.login("admin", "Admin@123");

        assertThat(result.accessToken()).isNotBlank();
        assertThat(result.refreshToken()).isNotBlank();
        assertThat(result.expiresIn()).isEqualTo(7200L);
    }

    @Test
    void login_wrongPassword_throwsUnauthorized() {
        AuthUserCredential cred = new AuthUserCredential();
        cred.setUserId(1L);
        cred.setUsername("admin");
        cred.setPassword(encoder.encode("Admin@123"));
        cred.setStatus(1);
        cred.setDeptId(10L);

        when(credentialMapper.findByUsername("admin")).thenReturn(cred);

        assertThatThrownBy(() -> loginService.login("admin", "wrong"))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getCode())
                        .isEqualTo(ErrorCode.UNAUTHORIZED.getCode()));
    }

    @Test
    void login_userNotFound_throwsUnauthorized() {
        when(credentialMapper.findByUsername("ghost")).thenReturn(null);

        assertThatThrownBy(() -> loginService.login("ghost", "pass"))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getCode())
                        .isEqualTo(ErrorCode.UNAUTHORIZED.getCode()));
    }

    @Test
    void login_disabledUser_throwsAccountDisabled() {
        AuthUserCredential cred = new AuthUserCredential();
        cred.setUserId(2L);
        cred.setUsername("disabled");
        cred.setPassword(encoder.encode("pass123"));
        cred.setStatus(0);
        cred.setDeptId(10L);

        when(credentialMapper.findByUsername("disabled")).thenReturn(cred);

        assertThatThrownBy(() -> loginService.login("disabled", "pass123"))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getCode())
                        .isEqualTo(ErrorCode.ACCOUNT_DISABLED.getCode()));
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
cd mro-backend
mvn test -pl auth-service -am -Dtest=LoginServiceTest -DskipTests=false
```

Expected: FAIL — `LoginService cannot be resolved`

- [ ] **Step 3: Create AuthUserCredentialMapper**

```java
// mro-backend/auth-service/src/main/java/com/mro/auth/mapper/AuthUserCredentialMapper.java
package com.mro.auth.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mro.auth.entity.AuthUserCredential;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AuthUserCredentialMapper extends BaseMapper<AuthUserCredential> {

    default AuthUserCredential findByUsername(String username) {
        return selectOne(new LambdaQueryWrapper<AuthUserCredential>()
                .eq(AuthUserCredential::getUsername, username));
    }

    default AuthUserCredential findByUserId(Long userId) {
        return selectOne(new LambdaQueryWrapper<AuthUserCredential>()
                .eq(AuthUserCredential::getUserId, userId));
    }
}
```

- [ ] **Step 4: Create LoginResult record**

```java
// mro-backend/auth-service/src/main/java/com/mro/auth/service/LoginResult.java
package com.mro.auth.service;

public record LoginResult(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn,
        Long userId,
        String username,
        String realName,
        String avatar
) {}
```

- [ ] **Step 5: Implement LoginService**

```java
// mro-backend/auth-service/src/main/java/com/mro/auth/service/LoginService.java
package com.mro.auth.service;

import com.mro.auth.entity.AuthUserCredential;
import com.mro.auth.mapper.AuthUserCredentialMapper;
import com.mro.common.core.exception.BusinessException;
import com.mro.common.core.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginService {

    private final AuthUserCredentialMapper credentialMapper;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final StringRedisTemplate redisTemplate;

    public LoginResult login(String username, String rawPassword) {
        AuthUserCredential cred = credentialMapper.findByUsername(username);

        // Unified error message to prevent account enumeration
        if (cred == null || !passwordEncoder.matches(rawPassword, cred.getPassword())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        if (cred.getStatus() != 1) {
            throw new BusinessException(ErrorCode.ACCOUNT_DISABLED);
        }

        // Issue tokens with minimal claims; permissions resolved by manage-web via Dubbo
        String accessToken = jwtService.generateAccessToken(
                cred.getUserId(), cred.getUsername(), cred.getDeptId(),
                List.of(), List.of()); // roles/permissions injected at BFF layer
        String refreshToken = jwtService.generateRefreshToken(cred.getUserId());

        return new LoginResult(
                accessToken, refreshToken, "Bearer",
                jwtService.getAccessTokenExpiry(),
                cred.getUserId(), cred.getUsername(),
                null, null);
    }

    public void logout(String jti, long remainingTtlSeconds) {
        if (remainingTtlSeconds > 0) {
            redisTemplate.opsForValue()
                    .set("auth:blacklist:" + jti, "1", remainingTtlSeconds, TimeUnit.SECONDS);
        }
    }

    public LoginResult refreshToken(String refreshToken) {
        Claims claims;
        try {
            claims = jwtService.parseRefreshToken(refreshToken);
        } catch (JwtException e) {
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_INVALID);
        }

        Long userId = Long.valueOf(claims.getSubject());
        AuthUserCredential cred = credentialMapper.findByUserId(userId);
        if (cred == null || cred.getStatus() != 1) {
            throw new BusinessException(ErrorCode.ACCOUNT_DISABLED);
        }

        String newAccessToken = jwtService.generateAccessToken(
                cred.getUserId(), cred.getUsername(), cred.getDeptId(),
                List.of(), List.of());

        return new LoginResult(newAccessToken, null, "Bearer",
                jwtService.getAccessTokenExpiry(),
                cred.getUserId(), cred.getUsername(), null, null);
    }

    public void changePassword(Long userId, String oldPassword, String newPassword) {
        AuthUserCredential cred = credentialMapper.findByUserId(userId);
        if (cred == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        if (!passwordEncoder.matches(oldPassword, cred.getPassword())) {
            throw new BusinessException(ErrorCode.OLD_PASSWORD_WRONG);
        }
        if (passwordEncoder.matches(newPassword, cred.getPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_SAME);
        }
        cred.setPassword(passwordEncoder.encode(newPassword));
        credentialMapper.updateById(cred);
    }
}
```

- [ ] **Step 6: Run tests**

```bash
cd mro-backend
mvn test -pl auth-service -am -Dtest=LoginServiceTest -DskipTests=false
```

Expected: `Tests run: 4, Failures: 0, Errors: 0`

- [ ] **Step 7: Commit**

```bash
cd mro-backend
git add auth-service/src/
git commit -m "feat(auth): add LoginService with login/logout/refresh/changePassword

Refs: AUTH-001"
```

---

## Task 5: AuthController + InternalAuthController

**Files:**
- Create: `mro-backend/auth-service/src/main/java/com/mro/auth/config/SecurityConfig.java`
- Create: `mro-backend/auth-service/src/main/java/com/mro/auth/controller/AuthController.java`
- Create: `mro-backend/auth-service/src/main/java/com/mro/auth/controller/InternalAuthController.java`
- Test: `mro-backend/auth-service/src/test/java/com/mro/auth/controller/AuthControllerTest.java`

- [ ] **Step 1: Write failing tests**

```java
// mro-backend/auth-service/src/test/java/com/mro/auth/controller/AuthControllerTest.java
package com.mro.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mro.auth.service.LoginResult;
import com.mro.auth.service.LoginService;
import com.mro.common.core.exception.BusinessException;
import com.mro.common.core.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LoginService loginService;

    @Test
    void login_success_returns200WithTokens() throws Exception {
        LoginResult result = new LoginResult("access123", "refresh456", "Bearer",
                7200L, 1L, "admin", "管理员", null);
        when(loginService.login("admin", "Admin@123")).thenReturn(result);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"admin","password":"Admin@123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.accessToken").value("access123"))
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"));
    }

    @Test
    void login_wrongCredentials_returns200WithErrorCode() throws Exception {
        when(loginService.login(any(), any()))
                .thenThrow(new BusinessException(ErrorCode.UNAUTHORIZED));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"admin","password":"wrong"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(4010));
    }

    @Test
    void login_missingUsername_returns400() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"password":"Admin@123"}
                                """))
                .andExpect(status().isBadRequest());
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
cd mro-backend
mvn test -pl auth-service -am -Dtest=AuthControllerTest -DskipTests=false
```

Expected: FAIL — `AuthController cannot be resolved`

- [ ] **Step 3: Create SecurityConfig (disable Spring Security for service, BCrypt bean)**

```java
// mro-backend/auth-service/src/main/java/com/mro/auth/config/SecurityConfig.java
package com.mro.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

- [ ] **Step 4: Create request/response records for AuthController**

```java
// mro-backend/auth-service/src/main/java/com/mro/auth/controller/LoginRequest.java
package com.mro.auth.controller;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank String username,
        @NotBlank String password
) {}
```

```java
// mro-backend/auth-service/src/main/java/com/mro/auth/controller/RefreshTokenRequest.java
package com.mro.auth.controller;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(
        @NotBlank String refreshToken
) {}
```

```java
// mro-backend/auth-service/src/main/java/com/mro/auth/controller/ChangePasswordRequest.java
package com.mro.auth.controller;

import jakarta.validation.constraints.NotBlank;

public record ChangePasswordRequest(
        @NotBlank String oldPassword,
        @NotBlank String newPassword,
        @NotBlank String confirmPassword
) {}
```

- [ ] **Step 5: Implement AuthController**

```java
// mro-backend/auth-service/src/main/java/com/mro/auth/controller/AuthController.java
package com.mro.auth.controller;

import com.mro.auth.service.LoginResult;
import com.mro.auth.service.LoginService;
import com.mro.common.core.exception.BusinessException;
import com.mro.common.core.exception.ErrorCode;
import com.mro.common.core.result.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final LoginService loginService;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody @Valid LoginRequest req) {
        LoginResult lr = loginService.login(req.username(), req.password());
        return Result.ok(Map.of(
                "accessToken", lr.accessToken(),
                "refreshToken", lr.refreshToken() != null ? lr.refreshToken() : "",
                "tokenType", lr.tokenType(),
                "expiresIn", lr.expiresIn(),
                "userId", lr.userId(),
                "username", lr.username()
        ));
    }

    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader("X-Jti") String jti,
                               @RequestHeader("X-Token-Ttl") long remainingTtl) {
        loginService.logout(jti, remainingTtl);
        return Result.ok();
    }

    @PostMapping("/refresh-token")
    public Result<Map<String, Object>> refreshToken(@RequestBody @Valid RefreshTokenRequest req) {
        LoginResult lr = loginService.refreshToken(req.refreshToken());
        return Result.ok(Map.of(
                "accessToken", lr.accessToken(),
                "expiresIn", lr.expiresIn()
        ));
    }

    @PostMapping("/change-password")
    public Result<Void> changePassword(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody @Valid ChangePasswordRequest req) {
        if (!req.newPassword().equals(req.confirmPassword())) {
            throw new BusinessException(ErrorCode.PARAM_INVALID.getCode(), "两次密码输入不一致");
        }
        loginService.changePassword(userId, req.oldPassword(), req.newPassword());
        return Result.ok();
    }
}
```

- [ ] **Step 6: Implement InternalAuthController (called by Gateway)**

```java
// mro-backend/auth-service/src/main/java/com/mro/auth/controller/InternalAuthController.java
package com.mro.auth.controller;

import com.mro.auth.service.JwtService;
import com.mro.common.core.result.Result;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/internal/auth")
@RequiredArgsConstructor
public class InternalAuthController {

    private final JwtService jwtService;
    private final StringRedisTemplate redisTemplate;

    @PostMapping("/verify")
    public Result<Map<String, Object>> verify(@RequestHeader("Authorization") String bearerToken) {
        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
            return Result.fail(4011, "Token 无效");
        }
        String token = bearerToken.substring(7);
        try {
            Claims claims = jwtService.parseAccessToken(token);
            String jti = claims.getId();

            Boolean blacklisted = redisTemplate.hasKey("auth:blacklist:" + jti);
            if (Boolean.TRUE.equals(blacklisted)) {
                return Result.fail(4012, "Token 已失效（已登出）");
            }

            return Result.ok(Map.of(
                    "userId", claims.getSubject(),
                    "deptId", claims.get("deptId", Long.class),
                    "roles", claims.get("roles"),
                    "permissions", claims.get("permissions"),
                    "jti", jti
            ));
        } catch (JwtException e) {
            return Result.fail(4011, "Token 无效或已过期");
        }
    }
}
```

- [ ] **Step 7: Run tests**

```bash
cd mro-backend
mvn test -pl auth-service -am -Dtest=AuthControllerTest -DskipTests=false
```

Expected: `Tests run: 3, Failures: 0, Errors: 0`

- [ ] **Step 8: Commit**

```bash
cd mro-backend
git add auth-service/src/
git commit -m "feat(auth): add AuthController, InternalAuthController, SecurityConfig

Refs: AUTH-001"
```

---

## Task 6: AuthDubboServiceImpl (Dubbo Provider)

**Files:**
- Create: `mro-backend/auth-service/src/main/java/com/mro/auth/service/AuthDubboServiceImpl.java`
- Test: `mro-backend/auth-service/src/test/java/com/mro/auth/service/AuthDubboServiceImplTest.java`

- [ ] **Step 1: Write failing tests**

```java
// mro-backend/auth-service/src/test/java/com/mro/auth/service/AuthDubboServiceImplTest.java
package com.mro.auth.service;

import com.mro.auth.entity.AuthUserCredential;
import com.mro.auth.mapper.AuthUserCredentialMapper;
import com.mro.common.core.exception.BusinessException;
import com.mro.common.dubbo.auth.dto.TokenDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthDubboServiceImplTest {

    @Mock
    private AuthUserCredentialMapper credentialMapper;
    @Mock
    private StringRedisTemplate redisTemplate;

    private JwtService jwtService;
    private LoginService loginService;
    private AuthDubboServiceImpl dubboService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        jwtService = new JwtService(
                "mro-test-secret-key-must-be-at-least-32-chars",
                7200L, 604800L);
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        loginService = new LoginService(credentialMapper, jwtService, encoder, redisTemplate);
        dubboService = new AuthDubboServiceImpl(loginService, jwtService);
    }

    @Test
    void refreshToken_validToken_returnsNewAccessToken() {
        AuthUserCredential cred = new AuthUserCredential();
        cred.setUserId(1L);
        cred.setUsername("admin");
        cred.setDeptId(10L);
        cred.setStatus(1);
        when(credentialMapper.findByUserId(1L)).thenReturn(cred);

        String refreshTok = jwtService.generateRefreshToken(1L);
        TokenDTO result = dubboService.refreshToken(refreshTok);

        assertThat(result.accessToken()).isNotBlank();
        assertThat(result.expiresIn()).isEqualTo(7200L);
    }

    @Test
    void logout_callsLoginServiceLogout() {
        var ops = mock(org.springframework.data.redis.core.ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(ops);

        dubboService.logout("test-jti", 3600L);

        verify(ops).set(eq("auth:blacklist:test-jti"), eq("1"), eq(3600L),
                eq(java.util.concurrent.TimeUnit.SECONDS));
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
cd mro-backend
mvn test -pl auth-service -am -Dtest=AuthDubboServiceImplTest -DskipTests=false
```

Expected: FAIL — `AuthDubboServiceImpl cannot be resolved`

- [ ] **Step 3: Implement AuthDubboServiceImpl**

```java
// mro-backend/auth-service/src/main/java/com/mro/auth/service/AuthDubboServiceImpl.java
package com.mro.auth.service;

import com.mro.common.dubbo.auth.AuthDubboService;
import com.mro.common.dubbo.auth.dto.TokenDTO;
import com.mro.common.dubbo.auth.dto.UserInfoDTO;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;

import java.util.List;

@DubboService(version = "1.0.0", group = "mro")
@RequiredArgsConstructor
public class AuthDubboServiceImpl implements AuthDubboService {

    private final LoginService loginService;
    private final JwtService jwtService;

    @Override
    public UserInfoDTO getUserInfo(Long userId) {
        // auth-service only knows credentials; full user info resolved by manage-web → system-service
        // This method provides the stub for interface compliance; manage-web calls system-service directly
        throw new UnsupportedOperationException("getUserInfo delegated to system-service via manage-web");
    }

    @Override
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        loginService.changePassword(userId, oldPassword, newPassword);
    }

    @Override
    public TokenDTO refreshToken(String refreshToken) {
        LoginResult result = loginService.refreshToken(refreshToken);
        return new TokenDTO(result.accessToken(), result.expiresIn());
    }

    @Override
    public void logout(String jti, long remainingTtlSeconds) {
        loginService.logout(jti, remainingTtlSeconds);
    }
}
```

- [ ] **Step 4: Run tests**

```bash
cd mro-backend
mvn test -pl auth-service -am -Dtest=AuthDubboServiceImplTest -DskipTests=false
```

Expected: `Tests run: 2, Failures: 0, Errors: 0`

- [ ] **Step 5: Run all auth-service tests**

```bash
cd mro-backend
mvn test -pl auth-service -am -DskipTests=false
```

Expected: All tests pass.

- [ ] **Step 6: Commit**

```bash
cd mro-backend
git add auth-service/src/
git commit -m "feat(auth): add AuthDubboServiceImpl Dubbo provider

Refs: AUTH-001"
```

---

## Task 7: Final Build Validation

- [ ] **Step 1: Build gateway-service and auth-service**

```bash
cd mro-backend
mvn clean package -pl gateway-service,auth-service -am -DskipTests
```

Expected: `BUILD SUCCESS`. JARs created in `gateway-service/target/` and `auth-service/target/`.

- [ ] **Step 2: Run all tests for both services**

```bash
cd mro-backend
mvn test -pl gateway-service,auth-service -am
```

Expected: All tests pass. Summary:
- `gateway-service`: 0 unit tests (integration tests need running infra)
- `auth-service`: ~14 tests passing

- [ ] **Step 3: Final commit**

```bash
cd mro-backend
git add .
git commit -m "chore(be-02): gateway-service + auth-service complete — all tests pass

Refs: AUTH-001, PLAT-001"
```

---

## Next Step

After BE-02 is complete, proceed to **BE-03** (`docs/superpowers/plans/2026-05-26-be03-system-service.md`) — system-service with dept/user/role/menu/dict/data-permission modules. BE-03 can run in parallel with BE-02.

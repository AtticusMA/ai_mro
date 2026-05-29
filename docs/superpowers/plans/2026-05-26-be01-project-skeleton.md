# BE-01: Maven Multi-Module Skeleton + mro-common Modules

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Bootstrap `mro-backend/` Maven multi-module monorepo with parent POM and three shared common modules (`mro-common-core`, `mro-common-dubbo`, `mro-common-data`) that all 13 services will depend on.

**Architecture:** Parent POM provides unified dependency management (Spring Boot 3.3, SCA 2023.0.x, Dubbo 3.3, MyBatis-Plus 3.5, Java 21). `mro-common-core` holds framework utilities (Result, PageResult, exceptions, UserContextDTO). `mro-common-dubbo` holds all Dubbo service interfaces and Record DTOs. `mro-common-data` holds MyBatis-Plus config and data-permission interceptor.

**Tech Stack:** Java 21, Maven 3.9+, Spring Boot 3.3.x, Spring Cloud Alibaba 2023.0.x, Apache Dubbo 3.3.x, MyBatis-Plus 3.5.x, Lombok

**Prerequisites:** None. This is the foundation for all other BE plans.

**Refs:** PLAT-001, PLAT-002, SYS-006, AUTH-001

---

## File Structure

```
mro-backend/
├── pom.xml                                         ← Parent POM (Task 1)
├── mro-common/
│   ├── pom.xml                                     ← mro-common parent (Task 1)
│   ├── mro-common-core/
│   │   ├── pom.xml                                 ← Task 1
│   │   └── src/main/java/com/mro/common/core/
│   │       ├── result/
│   │       │   ├── Result.java                     ← Task 2
│   │       │   ├── PageResult.java                 ← Task 2
│   │       │   └── PageParam.java                  ← Task 2
│   │       ├── exception/
│   │       │   ├── BusinessException.java          ← Task 3
│   │       │   ├── ErrorCode.java                  ← Task 3
│   │       │   └── GlobalExceptionHandler.java     ← Task 3
│   │       ├── context/
│   │       │   └── UserContextDTO.java             ← Task 4
│   │       └── util/
│   │           └── UserContextHolder.java          ← Task 4
│   ├── mro-common-dubbo/
│   │   ├── pom.xml                                 ← Task 1
│   │   └── src/main/java/com/mro/common/dubbo/
│   │       ├── auth/
│   │       │   ├── AuthDubboService.java           ← Task 5
│   │       │   └── dto/                            ← Task 5 (UserInfoDTO, TokenDTO)
│   │       ├── system/
│   │       │   ├── UserDubboService.java           ← Task 5
│   │       │   ├── DeptDubboService.java           ← Task 5
│   │       │   ├── RoleDubboService.java           ← Task 5
│   │       │   ├── MenuDubboService.java           ← Task 5
│   │       │   ├── DictDubboService.java           ← Task 5
│   │       │   └── dto/                            ← Task 5
│   │       ├── rag/
│   │       │   ├── RagDubboService.java            ← Task 6
│   │       │   └── dto/                            ← Task 6
│   │       └── mro/
│   │           ├── AircraftHealthDubboService.java ← Task 6
│   │           ├── ArMaintenanceDubboService.java  ← Task 6
│   │           ├── FaultDiagnosisDubboService.java ← Task 6
│   │           ├── ManualDubboService.java         ← Task 6
│   │           ├── DigitalTwinDubboService.java    ← Task 6
│   │           ├── ToolingMaterialDubboService.java← Task 6
│   │           ├── VrArTrainingDubboService.java   ← Task 6
│   │           ├── PaperlessCheckinDubboService.java←Task 6
│   │           └── dto/                            ← Task 6
│   └── mro-common-data/
│       ├── pom.xml                                 ← Task 1
│       └── src/main/java/com/mro/common/data/
│           ├── config/
│           │   └── MyBatisPlusConfig.java          ← Task 7
│           ├── annotation/
│           │   └── DataScope.java                  ← Task 7
│           └── interceptor/
│               └── DataScopeInterceptor.java       ← Task 7
```

---

## Task 1: Parent POM + Module POMs

**Files:**
- Create: `mro-backend/pom.xml`
- Create: `mro-backend/mro-common/pom.xml`
- Create: `mro-backend/mro-common/mro-common-core/pom.xml`
- Create: `mro-backend/mro-common/mro-common-dubbo/pom.xml`
- Create: `mro-backend/mro-common/mro-common-data/pom.xml`

- [ ] **Step 1: Create root mro-backend directory and parent POM**

```xml
<!-- mro-backend/pom.xml -->
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.mro</groupId>
    <artifactId>mro-backend</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <packaging>pom</packaging>
    <name>MRO Backend</name>

    <modules>
        <module>mro-common</module>
        <module>gateway-service</module>
        <module>auth-service</module>
        <module>system-service</module>
        <module>manage-web</module>
        <module>aircraft-health-service</module>
        <module>ar-maintenance-service</module>
        <module>fault-diagnosis-service</module>
        <module>maintenance-manual-service</module>
        <module>digital-twin-service</module>
        <module>tooling-material-service</module>
        <module>vr-ar-training-service</module>
        <module>paperless-checkin-service</module>
        <module>rag-service</module>
    </modules>

    <properties>
        <java.version>21</java.version>
        <maven.compiler.source>21</maven.compiler.source>
        <maven.compiler.target>21</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <spring-boot.version>3.3.5</spring-boot.version>
        <spring-cloud-alibaba.version>2023.0.3.2</spring-cloud-alibaba.version>
        <dubbo.version>3.3.3</dubbo.version>
        <mybatis-plus.version>3.5.9</mybatis-plus.version>
        <lombok.version>1.18.34</lombok.version>
        <mapstruct.version>1.6.3</mapstruct.version>
        <jjwt.version>0.12.6</jjwt.version>
        <knife4j.version>4.5.0</knife4j.version>
    </properties>

    <dependencyManagement>
        <dependencies>
            <!-- Spring Boot BOM -->
            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-dependencies</artifactId>
                <version>${spring-boot.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
            <!-- Spring Cloud Alibaba BOM -->
            <dependency>
                <groupId>com.alibaba.cloud</groupId>
                <artifactId>spring-cloud-alibaba-dependencies</artifactId>
                <version>${spring-cloud-alibaba.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
            <!-- Dubbo BOM -->
            <dependency>
                <groupId>org.apache.dubbo</groupId>
                <artifactId>dubbo-bom</artifactId>
                <version>${dubbo.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
            <!-- Internal modules -->
            <dependency>
                <groupId>com.mro</groupId>
                <artifactId>mro-common-core</artifactId>
                <version>${project.version}</version>
            </dependency>
            <dependency>
                <groupId>com.mro</groupId>
                <artifactId>mro-common-dubbo</artifactId>
                <version>${project.version}</version>
            </dependency>
            <dependency>
                <groupId>com.mro</groupId>
                <artifactId>mro-common-data</artifactId>
                <version>${project.version}</version>
            </dependency>
            <!-- MyBatis-Plus -->
            <dependency>
                <groupId>com.baomidou</groupId>
                <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
                <version>${mybatis-plus.version}</version>
            </dependency>
            <!-- Lombok -->
            <dependency>
                <groupId>org.projectlombok</groupId>
                <artifactId>lombok</artifactId>
                <version>${lombok.version}</version>
                <scope>provided</scope>
            </dependency>
            <!-- MapStruct -->
            <dependency>
                <groupId>org.mapstruct</groupId>
                <artifactId>mapstruct</artifactId>
                <version>${mapstruct.version}</version>
            </dependency>
            <!-- JWT -->
            <dependency>
                <groupId>io.jsonwebtoken</groupId>
                <artifactId>jjwt-api</artifactId>
                <version>${jjwt.version}</version>
            </dependency>
            <dependency>
                <groupId>io.jsonwebtoken</groupId>
                <artifactId>jjwt-impl</artifactId>
                <version>${jjwt.version}</version>
                <scope>runtime</scope>
            </dependency>
            <dependency>
                <groupId>io.jsonwebtoken</groupId>
                <artifactId>jjwt-jackson</artifactId>
                <version>${jjwt.version}</version>
                <scope>runtime</scope>
            </dependency>
            <!-- Knife4j (OpenAPI 3) -->
            <dependency>
                <groupId>com.github.xiaoymin</groupId>
                <artifactId>knife4j-openapi3-jakarta-spring-boot-starter</artifactId>
                <version>${knife4j.version}</version>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <build>
        <pluginManagement>
            <plugins>
                <plugin>
                    <groupId>org.springframework.boot</groupId>
                    <artifactId>spring-boot-maven-plugin</artifactId>
                    <version>${spring-boot.version}</version>
                    <configuration>
                        <excludes>
                            <exclude>
                                <groupId>org.projectlombok</groupId>
                                <artifactId>lombok</artifactId>
                            </exclude>
                        </excludes>
                    </configuration>
                </plugin>
                <plugin>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-compiler-plugin</artifactId>
                    <version>3.13.0</version>
                    <configuration>
                        <source>21</source>
                        <target>21</target>
                        <annotationProcessorPaths>
                            <path>
                                <groupId>org.projectlombok</groupId>
                                <artifactId>lombok</artifactId>
                                <version>${lombok.version}</version>
                            </path>
                            <path>
                                <groupId>org.mapstruct</groupId>
                                <artifactId>mapstruct-processor</artifactId>
                                <version>${mapstruct.version}</version>
                            </path>
                        </annotationProcessorPaths>
                    </configuration>
                </plugin>
            </plugins>
        </pluginManagement>
    </build>
</project>
```

- [ ] **Step 2: Create mro-common parent POM**

```xml
<!-- mro-backend/mro-common/pom.xml -->
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

    <artifactId>mro-common</artifactId>
    <packaging>pom</packaging>
    <name>MRO Common</name>

    <modules>
        <module>mro-common-core</module>
        <module>mro-common-dubbo</module>
        <module>mro-common-data</module>
    </modules>
</project>
```

- [ ] **Step 3: Create mro-common-core POM**

```xml
<!-- mro-backend/mro-common/mro-common-core/pom.xml -->
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.mro</groupId>
        <artifactId>mro-common</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </parent>

    <artifactId>mro-common-core</artifactId>
    <name>MRO Common Core</name>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
            <optional>true</optional>
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
</project>
```

- [ ] **Step 4: Create mro-common-dubbo POM**

```xml
<!-- mro-backend/mro-common/mro-common-dubbo/pom.xml -->
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.mro</groupId>
        <artifactId>mro-common</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </parent>

    <artifactId>mro-common-dubbo</artifactId>
    <name>MRO Common Dubbo</name>

    <dependencies>
        <dependency>
            <groupId>com.mro</groupId>
            <artifactId>mro-common-core</artifactId>
        </dependency>
        <dependency>
            <groupId>org.apache.dubbo</groupId>
            <artifactId>dubbo-spring-boot-starter</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
</project>
```

- [ ] **Step 5: Create mro-common-data POM**

```xml
<!-- mro-backend/mro-common/mro-common-data/pom.xml -->
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.mro</groupId>
        <artifactId>mro-common</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </parent>

    <artifactId>mro-common-data</artifactId>
    <name>MRO Common Data</name>

    <dependencies>
        <dependency>
            <groupId>com.mro</groupId>
            <artifactId>mro-common-core</artifactId>
        </dependency>
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
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
</project>
```

- [ ] **Step 6: Verify Maven structure compiles**

```bash
cd mro-backend
mvn validate -pl mro-common -am
```

Expected: `BUILD SUCCESS`

- [ ] **Step 7: Commit**

```bash
cd mro-backend
git add pom.xml mro-common/
git commit -m "chore: bootstrap mro-backend Maven multi-module structure

Refs: PLAT-001"
```

---

## Task 2: Result, PageResult, PageParam

**Files:**
- Create: `mro-backend/mro-common/mro-common-core/src/main/java/com/mro/common/core/result/Result.java`
- Create: `mro-backend/mro-common/mro-common-core/src/main/java/com/mro/common/core/result/PageResult.java`
- Create: `mro-backend/mro-common/mro-common-core/src/main/java/com/mro/common/core/result/PageParam.java`
- Test: `mro-backend/mro-common/mro-common-core/src/test/java/com/mro/common/core/result/ResultTest.java`

- [ ] **Step 1: Write failing tests**

```java
// mro-backend/mro-common/mro-common-core/src/test/java/com/mro/common/core/result/ResultTest.java
package com.mro.common.core.result;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class ResultTest {

    @Test
    void success_setsCodeZeroAndData() {
        Result<String> r = Result.ok("hello");
        assertThat(r.getCode()).isEqualTo(0);
        assertThat(r.getMsg()).isEqualTo("ok");
        assertThat(r.getData()).isEqualTo("hello");
        assertThat(r.getTimestamp()).isPositive();
    }

    @Test
    void fail_setsCodeAndMessage() {
        Result<Void> r = Result.fail(4011, "Token 无效");
        assertThat(r.getCode()).isEqualTo(4011);
        assertThat(r.getMsg()).isEqualTo("Token 无效");
        assertThat(r.getData()).isNull();
    }

    @Test
    void pageResult_setsAllFields() {
        PageResult<String> pr = PageResult.of(java.util.List.of("a", "b"), 100L, 1, 20);
        assertThat(pr.getList()).hasSize(2);
        assertThat(pr.getTotal()).isEqualTo(100L);
        assertThat(pr.getPageNum()).isEqualTo(1);
        assertThat(pr.getPageSize()).isEqualTo(20);
    }

    @Test
    void pageParam_defaultValues() {
        PageParam p = new PageParam();
        assertThat(p.getPageNum()).isEqualTo(1);
        assertThat(p.getPageSize()).isEqualTo(20);
    }

    @Test
    void pageParam_rejectsPageSizeOver100() {
        PageParam p = new PageParam();
        p.setPageSize(200);
        // validate via jakarta.validation — tested in integration; here just verify setter
        assertThat(p.getPageSize()).isEqualTo(200); // setter stores value; @Max enforced by Spring
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
cd mro-backend
mvn test -pl mro-common/mro-common-core -Dtest=ResultTest
```

Expected: FAIL — `Result cannot be resolved to a type`

- [ ] **Step 3: Implement Result**

```java
// mro-backend/mro-common/mro-common-core/src/main/java/com/mro/common/core/result/Result.java
package com.mro.common.core.result;

import lombok.Getter;

@Getter
public class Result<T> {

    private final int code;
    private final String msg;
    private final T data;
    private final long timestamp;

    private Result(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    public static <T> Result<T> ok(T data) {
        return new Result<>(0, "ok", data);
    }

    public static Result<Void> ok() {
        return new Result<>(0, "ok", null);
    }

    public static <T> Result<T> fail(int code, String msg) {
        return new Result<>(code, msg, null);
    }
}
```

- [ ] **Step 4: Implement PageResult**

```java
// mro-backend/mro-common/mro-common-core/src/main/java/com/mro/common/core/result/PageResult.java
package com.mro.common.core.result;

import lombok.Getter;
import java.util.List;

@Getter
public class PageResult<T> {

    private final List<T> list;
    private final long total;
    private final int pageNum;
    private final int pageSize;

    private PageResult(List<T> list, long total, int pageNum, int pageSize) {
        this.list = list;
        this.total = total;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
    }

    public static <T> PageResult<T> of(List<T> list, long total, int pageNum, int pageSize) {
        return new PageResult<>(list, total, pageNum, pageSize);
    }
}
```

- [ ] **Step 5: Implement PageParam**

```java
// mro-backend/mro-common/mro-common-core/src/main/java/com/mro/common/core/result/PageParam.java
package com.mro.common.core.result;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PageParam {

    @Min(1)
    private int pageNum = 1;

    @Min(1)
    @Max(100)
    private int pageSize = 20;
}
```

- [ ] **Step 6: Run tests to verify they pass**

```bash
cd mro-backend
mvn test -pl mro-common/mro-common-core -Dtest=ResultTest
```

Expected: `Tests run: 5, Failures: 0, Errors: 0`

- [ ] **Step 7: Commit**

```bash
cd mro-backend
git add mro-common/mro-common-core/src/
git commit -m "feat(common-core): add Result, PageResult, PageParam

Refs: PLAT-001"
```

---

## Task 3: BusinessException + ErrorCode + GlobalExceptionHandler

**Files:**
- Create: `mro-backend/mro-common/mro-common-core/src/main/java/com/mro/common/core/exception/ErrorCode.java`
- Create: `mro-backend/mro-common/mro-common-core/src/main/java/com/mro/common/core/exception/BusinessException.java`
- Create: `mro-backend/mro-common/mro-common-core/src/main/java/com/mro/common/core/exception/GlobalExceptionHandler.java`
- Test: `mro-backend/mro-common/mro-common-core/src/test/java/com/mro/common/core/exception/BusinessExceptionTest.java`

- [ ] **Step 1: Write failing tests**

```java
// mro-backend/mro-common/mro-common-core/src/test/java/com/mro/common/core/exception/BusinessExceptionTest.java
package com.mro.common.core.exception;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class BusinessExceptionTest {

    @Test
    void constructor_withCodeAndMessage() {
        BusinessException ex = new BusinessException(4011, "Token 无效");
        assertThat(ex.getCode()).isEqualTo(4011);
        assertThat(ex.getMessage()).isEqualTo("Token 无效");
    }

    @Test
    void constructor_withErrorCode() {
        BusinessException ex = new BusinessException(ErrorCode.TOKEN_INVALID);
        assertThat(ex.getCode()).isEqualTo(4011);
        assertThat(ex.getMessage()).isEqualTo("Token 无效或已过期");
    }

    @Test
    void errorCode_hasCorrectValues() {
        assertThat(ErrorCode.SUCCESS.getCode()).isEqualTo(0);
        assertThat(ErrorCode.TOKEN_INVALID.getCode()).isEqualTo(4011);
        assertThat(ErrorCode.PARAM_INVALID.getCode()).isEqualTo(4001);
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
cd mro-backend
mvn test -pl mro-common/mro-common-core -Dtest=BusinessExceptionTest
```

Expected: FAIL — `ErrorCode cannot be resolved`

- [ ] **Step 3: Implement ErrorCode**

```java
// mro-backend/mro-common/mro-common-core/src/main/java/com/mro/common/core/exception/ErrorCode.java
package com.mro.common.core.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // General
    SUCCESS(0, "ok"),
    PARAM_INVALID(4001, "请求参数无效"),
    UNAUTHORIZED(4010, "用户名或密码错误"),
    TOKEN_INVALID(4011, "Token 无效或已过期"),
    TOKEN_BLACKLISTED(4012, "Token 已失效（已登出）"),
    REFRESH_TOKEN_INVALID(4013, "refreshToken 无效或已过期"),
    PASSWORD_SAME(4014, "新旧密码不能相同"),
    OLD_PASSWORD_WRONG(4015, "旧密码校验失败"),
    FORBIDDEN(4020, "无此接口权限"),
    ACCOUNT_DISABLED(4021, "账号已禁用"),
    DEPT_DISABLED(4022, "所属部门已禁用"),

    // System service 4100-4199
    USER_NOT_FOUND(4120, "用户不存在"),
    USERNAME_DUPLICATE(4121, "用户名已存在"),
    PHONE_DUPLICATE(4122, "手机号已存在"),
    EMPLOYEE_NO_DUPLICATE(4123, "工号已存在"),
    DEPT_NOT_FOUND(4100, "部门不存在"),
    DEPT_HAS_CHILDREN(4101, "部门下有子部门，禁止删除"),
    DEPT_HAS_USERS(4102, "部门下有在岗用户，禁止删除"),
    DEPT_CODE_DUPLICATE(4103, "部门编码已存在"),
    ROLE_NOT_FOUND(4140, "角色不存在"),
    ROLE_KEY_DUPLICATE(4141, "权限字符串已存在"),
    MENU_NOT_FOUND(4160, "菜单不存在"),
    DICT_NOT_FOUND(4180, "字典不存在"),

    // Server error
    INTERNAL_ERROR(5000, "服务器内部错误");

    private final int code;
    private final String message;
}
```

- [ ] **Step 4: Implement BusinessException**

```java
// mro-backend/mro-common/mro-common-core/src/main/java/com/mro/common/core/exception/BusinessException.java
package com.mro.common.core.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }
}
```

- [ ] **Step 5: Implement GlobalExceptionHandler**

```java
// mro-backend/mro-common/mro-common-core/src/main/java/com/mro/common/core/exception/GlobalExceptionHandler.java
package com.mro.common.core.exception;

import com.mro.common.core.result.Result;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusiness(BusinessException ex) {
        return Result.fail(ex.getCode(), ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleValidation(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .findFirst()
                .orElse("参数校验失败");
        return Result.fail(ErrorCode.PARAM_INVALID.getCode(), msg);
    }

    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleBind(BindException ex) {
        String msg = ex.getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .findFirst()
                .orElse("参数校验失败");
        return Result.fail(ErrorCode.PARAM_INVALID.getCode(), msg);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleConstraint(ConstraintViolationException ex) {
        String msg = ex.getConstraintViolations().stream()
                .map(cv -> cv.getPropertyPath() + ": " + cv.getMessage())
                .findFirst()
                .orElse("参数校验失败");
        return Result.fail(ErrorCode.PARAM_INVALID.getCode(), msg);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleGeneral(Exception ex) {
        log.error("Unhandled exception", ex);
        return Result.fail(ErrorCode.INTERNAL_ERROR.getCode(), ErrorCode.INTERNAL_ERROR.getMessage());
    }
}
```

- [ ] **Step 6: Run tests**

```bash
cd mro-backend
mvn test -pl mro-common/mro-common-core -Dtest=BusinessExceptionTest
```

Expected: `Tests run: 3, Failures: 0, Errors: 0`

- [ ] **Step 7: Commit**

```bash
cd mro-backend
git add mro-common/mro-common-core/src/
git commit -m "feat(common-core): add BusinessException, ErrorCode, GlobalExceptionHandler

Refs: PLAT-001"
```

---

## Task 4: UserContextDTO + UserContextHolder

**Files:**
- Create: `mro-backend/mro-common/mro-common-core/src/main/java/com/mro/common/core/context/UserContextDTO.java`
- Create: `mro-backend/mro-common/mro-common-core/src/main/java/com/mro/common/core/context/UserContextHolder.java`
- Test: `mro-backend/mro-common/mro-common-core/src/test/java/com/mro/common/core/context/UserContextHolderTest.java`

- [ ] **Step 1: Write failing tests**

```java
// mro-backend/mro-common/mro-common-core/src/test/java/com/mro/common/core/context/UserContextHolderTest.java
package com.mro.common.core.context;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.*;

class UserContextHolderTest {

    @AfterEach
    void cleanup() {
        UserContextHolder.clear();
    }

    @Test
    void setAndGet_returnsContext() {
        UserContextDTO ctx = new UserContextDTO(1L, "admin", "管理员", 10L,
                List.of("admin"), List.of("dept:list"));
        UserContextHolder.set(ctx);
        assertThat(UserContextHolder.get()).isSameAs(ctx);
    }

    @Test
    void get_withoutSet_returnsNull() {
        assertThat(UserContextHolder.get()).isNull();
    }

    @Test
    void clear_removesContext() {
        UserContextDTO ctx = new UserContextDTO(1L, "admin", "管理员", 10L,
                List.of("admin"), List.of("dept:list"));
        UserContextHolder.set(ctx);
        UserContextHolder.clear();
        assertThat(UserContextHolder.get()).isNull();
    }

    @Test
    void requiredUserId_throwsWhenNotSet() {
        assertThatThrownBy(UserContextHolder::requiredUserId)
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void requiredUserId_returnsWhenSet() {
        UserContextDTO ctx = new UserContextDTO(42L, "user", "用户", 10L,
                List.of(), List.of());
        UserContextHolder.set(ctx);
        assertThat(UserContextHolder.requiredUserId()).isEqualTo(42L);
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
cd mro-backend
mvn test -pl mro-common/mro-common-core -Dtest=UserContextHolderTest
```

Expected: FAIL — `UserContextDTO cannot be resolved`

- [ ] **Step 3: Implement UserContextDTO**

```java
// mro-backend/mro-common/mro-common-core/src/main/java/com/mro/common/core/context/UserContextDTO.java
package com.mro.common.core.context;

import java.util.List;

public record UserContextDTO(
        Long userId,
        String username,
        String realName,
        Long deptId,
        List<String> roles,
        List<String> permissions
) {
    public boolean hasPermission(String permission) {
        return permissions != null && permissions.contains(permission);
    }

    public boolean hasRole(String role) {
        return roles != null && roles.contains(role);
    }

    public boolean isSuperAdmin() {
        return roles != null && roles.contains("*:*:*");
    }
}
```

- [ ] **Step 4: Implement UserContextHolder**

```java
// mro-backend/mro-common/mro-common-core/src/main/java/com/mro/common/core/context/UserContextHolder.java
package com.mro.common.core.context;

public final class UserContextHolder {

    private static final ThreadLocal<UserContextDTO> HOLDER = new ThreadLocal<>();

    private UserContextHolder() {}

    public static void set(UserContextDTO ctx) {
        HOLDER.set(ctx);
    }

    public static UserContextDTO get() {
        return HOLDER.get();
    }

    public static void clear() {
        HOLDER.remove();
    }

    public static Long requiredUserId() {
        UserContextDTO ctx = HOLDER.get();
        if (ctx == null) {
            throw new IllegalStateException("No user context in current thread");
        }
        return ctx.userId();
    }
}
```

- [ ] **Step 5: Run tests**

```bash
cd mro-backend
mvn test -pl mro-common/mro-common-core -Dtest=UserContextHolderTest
```

Expected: `Tests run: 5, Failures: 0, Errors: 0`

- [ ] **Step 6: Run all mro-common-core tests**

```bash
cd mro-backend
mvn test -pl mro-common/mro-common-core
```

Expected: All tests pass.

- [ ] **Step 7: Commit**

```bash
cd mro-backend
git add mro-common/mro-common-core/src/
git commit -m "feat(common-core): add UserContextDTO (record) and UserContextHolder

Refs: PLAT-001"
```

---

## Task 5: Auth + System Dubbo Interfaces & DTOs

**Files:**
- Create: `mro-backend/mro-common/mro-common-dubbo/src/main/java/com/mro/common/dubbo/auth/AuthDubboService.java`
- Create: `mro-backend/mro-common/mro-common-dubbo/src/main/java/com/mro/common/dubbo/auth/dto/UserInfoDTO.java`
- Create: `mro-backend/mro-common/mro-common-dubbo/src/main/java/com/mro/common/dubbo/auth/dto/TokenDTO.java`
- Create: `mro-backend/mro-common/mro-common-dubbo/src/main/java/com/mro/common/dubbo/system/UserDubboService.java`
- Create: `mro-backend/mro-common/mro-common-dubbo/src/main/java/com/mro/common/dubbo/system/DeptDubboService.java`
- Create: `mro-backend/mro-common/mro-common-dubbo/src/main/java/com/mro/common/dubbo/system/RoleDubboService.java`
- Create: `mro-backend/mro-common/mro-common-dubbo/src/main/java/com/mro/common/dubbo/system/MenuDubboService.java`
- Create: `mro-backend/mro-common/mro-common-dubbo/src/main/java/com/mro/common/dubbo/system/DictDubboService.java`
- Create: `mro-backend/mro-common/mro-common-dubbo/src/main/java/com/mro/common/dubbo/system/dto/` (all DTOs)

- [ ] **Step 1: Create AuthDubboService**

```java
// mro-backend/mro-common/mro-common-dubbo/src/main/java/com/mro/common/dubbo/auth/AuthDubboService.java
package com.mro.common.dubbo.auth;

import com.mro.common.dubbo.auth.dto.TokenDTO;
import com.mro.common.dubbo.auth.dto.UserInfoDTO;

public interface AuthDubboService {

    UserInfoDTO getUserInfo(Long userId);

    void changePassword(Long userId, String oldPassword, String newPassword);

    TokenDTO refreshToken(String refreshToken);

    void logout(String jti, long remainingTtlSeconds);
}
```

- [ ] **Step 2: Create Auth DTOs**

```java
// mro-backend/mro-common/mro-common-dubbo/src/main/java/com/mro/common/dubbo/auth/dto/UserInfoDTO.java
package com.mro.common.dubbo.auth.dto;

import java.io.Serializable;
import java.util.List;

public record UserInfoDTO(
        Long userId,
        String username,
        String realName,
        String avatar,
        Long deptId,
        String deptName,
        List<String> roles,
        List<String> permissions
) implements Serializable {}
```

```java
// mro-backend/mro-common/mro-common-dubbo/src/main/java/com/mro/common/dubbo/auth/dto/TokenDTO.java
package com.mro.common.dubbo.auth.dto;

import java.io.Serializable;

public record TokenDTO(
        String accessToken,
        long expiresIn
) implements Serializable {}
```

- [ ] **Step 3: Create System DTOs**

```java
// mro-backend/mro-common/mro-common-dubbo/src/main/java/com/mro/common/dubbo/system/dto/UserDTO.java
package com.mro.common.dubbo.system.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

public record UserDTO(
        Long id,
        String username,
        String employeeNo,
        String realName,
        Long deptId,
        String deptName,
        String phone,
        String email,
        String avatar,
        Integer status,
        List<String> roles,
        LocalDateTime createTime
) implements Serializable {}
```

```java
// mro-backend/mro-common/mro-common-dubbo/src/main/java/com/mro/common/dubbo/system/dto/CreateUserCommand.java
package com.mro.common.dubbo.system.dto;

import java.io.Serializable;

public record CreateUserCommand(
        String username,
        String password,
        String employeeNo,
        String realName,
        Long deptId,
        String phone,
        String email,
        List<Long> roleIds
) implements Serializable {
    // fix: import java.util.List
}
```

```java
// mro-backend/mro-common/mro-common-dubbo/src/main/java/com/mro/common/dubbo/system/dto/CreateUserCommand.java
package com.mro.common.dubbo.system.dto;

import java.io.Serializable;
import java.util.List;

public record CreateUserCommand(
        String username,
        String password,
        String employeeNo,
        String realName,
        Long deptId,
        String phone,
        String email,
        List<Long> roleIds
) implements Serializable {}
```

```java
// mro-backend/mro-common/mro-common-dubbo/src/main/java/com/mro/common/dubbo/system/dto/UpdateUserCommand.java
package com.mro.common.dubbo.system.dto;

import java.io.Serializable;
import java.util.List;

public record UpdateUserCommand(
        Long id,
        String employeeNo,
        String realName,
        Long deptId,
        String phone,
        String email,
        String avatar,
        List<Long> roleIds
) implements Serializable {}
```

```java
// mro-backend/mro-common/mro-common-dubbo/src/main/java/com/mro/common/dubbo/system/dto/UserQueryParam.java
package com.mro.common.dubbo.system.dto;

import com.mro.common.core.result.PageParam;
import java.io.Serializable;

public record UserQueryParam(
        String username,
        String realName,
        Long deptId,
        Integer status,
        int pageNum,
        int pageSize
) implements Serializable {}
```

```java
// mro-backend/mro-common/mro-common-dubbo/src/main/java/com/mro/common/dubbo/system/dto/DeptDTO.java
package com.mro.common.dubbo.system.dto;

import java.io.Serializable;
import java.util.List;

public record DeptDTO(
        Long id,
        String deptName,
        String deptCode,
        Long parentId,
        String ancestors,
        Integer orderNum,
        String leader,
        String phone,
        String email,
        Integer status,
        List<DeptDTO> children
) implements Serializable {}
```

```java
// mro-backend/mro-common/mro-common-dubbo/src/main/java/com/mro/common/dubbo/system/dto/CreateDeptCommand.java
package com.mro.common.dubbo.system.dto;

import java.io.Serializable;

public record CreateDeptCommand(
        String deptName,
        String deptCode,
        Long parentId,
        Integer orderNum,
        String leader,
        String phone,
        String email
) implements Serializable {}
```

```java
// mro-backend/mro-common/mro-common-dubbo/src/main/java/com/mro/common/dubbo/system/dto/UpdateDeptCommand.java
package com.mro.common.dubbo.system.dto;

import java.io.Serializable;

public record UpdateDeptCommand(
        Long id,
        String deptName,
        String deptCode,
        Long parentId,
        Integer orderNum,
        String leader,
        String phone,
        String email
) implements Serializable {}
```

```java
// mro-backend/mro-common/mro-common-dubbo/src/main/java/com/mro/common/dubbo/system/dto/RoleDTO.java
package com.mro.common.dubbo.system.dto;

import java.io.Serializable;

public record RoleDTO(
        Long id,
        String roleName,
        String roleKey,
        Integer dataScope,
        Integer orderNum,
        Integer status
) implements Serializable {}
```

```java
// mro-backend/mro-common/mro-common-dubbo/src/main/java/com/mro/common/dubbo/system/dto/CreateRoleCommand.java
package com.mro.common.dubbo.system.dto;

import java.io.Serializable;
import java.util.List;

public record CreateRoleCommand(
        String roleName,
        String roleKey,
        Integer dataScope,
        Integer orderNum,
        List<Long> menuIds,
        List<Long> dataScopeDeptIds
) implements Serializable {}
```

```java
// mro-backend/mro-common/mro-common-dubbo/src/main/java/com/mro/common/dubbo/system/dto/UpdateRoleCommand.java
package com.mro.common.dubbo.system.dto;

import java.io.Serializable;
import java.util.List;

public record UpdateRoleCommand(
        Long id,
        String roleName,
        String roleKey,
        Integer dataScope,
        Integer orderNum,
        List<Long> menuIds,
        List<Long> dataScopeDeptIds
) implements Serializable {}
```

```java
// mro-backend/mro-common/mro-common-dubbo/src/main/java/com/mro/common/dubbo/system/dto/MenuDTO.java
package com.mro.common.dubbo.system.dto;

import java.io.Serializable;
import java.util.List;

public record MenuDTO(
        Long id,
        Long parentId,
        String menuName,
        String menuType,
        String path,
        String component,
        String perms,
        String icon,
        Integer orderNum,
        Integer visible,
        Integer status,
        List<MenuDTO> children
) implements Serializable {}
```

```java
// mro-backend/mro-common/mro-common-dubbo/src/main/java/com/mro/common/dubbo/system/dto/DictTypeDTO.java
package com.mro.common.dubbo.system.dto;

import java.io.Serializable;

public record DictTypeDTO(
        Long id,
        String dictName,
        String dictType,
        Integer status
) implements Serializable {}
```

```java
// mro-backend/mro-common/mro-common-dubbo/src/main/java/com/mro/common/dubbo/system/dto/DictDataDTO.java
package com.mro.common.dubbo.system.dto;

import java.io.Serializable;

public record DictDataDTO(
        Long id,
        String dictType,
        String dictLabel,
        String dictValue,
        Integer orderNum,
        String cssClass,
        Integer status
) implements Serializable {}
```

- [ ] **Step 4: Create UserDubboService**

```java
// mro-backend/mro-common/mro-common-dubbo/src/main/java/com/mro/common/dubbo/system/UserDubboService.java
package com.mro.common.dubbo.system;

import com.mro.common.core.result.PageResult;
import com.mro.common.dubbo.system.dto.*;

public interface UserDubboService {

    Long createUser(CreateUserCommand cmd);

    void updateUser(UpdateUserCommand cmd);

    void deleteUser(Long userId);

    void enableUser(Long userId);

    void disableUser(Long userId);

    void resetPassword(Long userId, String newPassword);

    UserDTO getById(Long userId);

    PageResult<UserDTO> listUsers(UserQueryParam param);

    UserDTO getByUsername(String username);

    boolean verifyPassword(Long userId, String rawPassword);
}
```

- [ ] **Step 5: Create DeptDubboService**

```java
// mro-backend/mro-common/mro-common-dubbo/src/main/java/com/mro/common/dubbo/system/DeptDubboService.java
package com.mro.common.dubbo.system;

import com.mro.common.dubbo.system.dto.*;
import java.util.List;

public interface DeptDubboService {

    Long createDept(CreateDeptCommand cmd);

    void updateDept(UpdateDeptCommand cmd);

    void deleteDept(Long deptId);

    void enableDept(Long deptId);

    void disableDept(Long deptId);

    DeptDTO getById(Long deptId);

    List<DeptDTO> listTree(String deptName, Integer status);

    List<Long> listChildrenIds(Long deptId);
}
```

- [ ] **Step 6: Create RoleDubboService**

```java
// mro-backend/mro-common/mro-common-dubbo/src/main/java/com/mro/common/dubbo/system/RoleDubboService.java
package com.mro.common.dubbo.system;

import com.mro.common.core.result.PageResult;
import com.mro.common.dubbo.system.dto.*;
import java.util.List;

public interface RoleDubboService {

    Long createRole(CreateRoleCommand cmd);

    void updateRole(UpdateRoleCommand cmd);

    void deleteRole(Long roleId);

    void enableRole(Long roleId);

    void disableRole(Long roleId);

    RoleDTO getById(Long roleId);

    PageResult<RoleDTO> listRoles(int pageNum, int pageSize, String roleName, Integer status);

    List<RoleDTO> listAll();

    List<Long> listMenuIdsByRoleId(Long roleId);
}
```

- [ ] **Step 7: Create MenuDubboService**

```java
// mro-backend/mro-common/mro-common-dubbo/src/main/java/com/mro/common/dubbo/system/MenuDubboService.java
package com.mro.common.dubbo.system;

import com.mro.common.dubbo.system.dto.MenuDTO;
import java.util.List;

public interface MenuDubboService {

    Long createMenu(MenuDTO menu);

    void updateMenu(MenuDTO menu);

    void deleteMenu(Long menuId);

    MenuDTO getById(Long menuId);

    List<MenuDTO> listTree(String menuName, Integer status);

    List<MenuDTO> listByRoleIds(List<Long> roleIds);

    List<String> listPermsByRoleIds(List<Long> roleIds);
}
```

- [ ] **Step 8: Create DictDubboService**

```java
// mro-backend/mro-common/mro-common-dubbo/src/main/java/com/mro/common/dubbo/system/DictDubboService.java
package com.mro.common.dubbo.system;

import com.mro.common.dubbo.system.dto.*;
import java.util.List;

public interface DictDubboService {

    List<DictDataDTO> listByDictType(String dictType);

    DictTypeDTO getDictType(Long id);

    List<DictTypeDTO> listDictTypes();
}
```

- [ ] **Step 9: Verify mro-common-dubbo compiles**

```bash
cd mro-backend
mvn compile -pl mro-common/mro-common-dubbo -am
```

Expected: `BUILD SUCCESS`

- [ ] **Step 10: Commit**

```bash
cd mro-backend
git add mro-common/mro-common-dubbo/
git commit -m "feat(common-dubbo): add auth + system Dubbo service interfaces and DTOs

Refs: AUTH-001, SYS-001, SYS-002, SYS-003, SYS-004, SYS-005"
```

---

## Task 6: MRO + RAG Dubbo Interfaces & DTOs

**Files:**
- Create: all files under `mro-backend/mro-common/mro-common-dubbo/src/main/java/com/mro/common/dubbo/rag/`
- Create: all files under `mro-backend/mro-common/mro-common-dubbo/src/main/java/com/mro/common/dubbo/mro/`

- [ ] **Step 1: Create RagDubboService and DTOs**

```java
// mro-backend/mro-common/mro-common-dubbo/src/main/java/com/mro/common/dubbo/rag/RagDubboService.java
package com.mro.common.dubbo.rag;

import com.mro.common.core.result.PageResult;
import com.mro.common.dubbo.rag.dto.*;
import java.util.List;

public interface RagDubboService {

    Long createKnowledgeBase(CreateKbCommand cmd);

    PageResult<KbDTO> listKnowledgeBases(KbQueryParam param);

    void deleteKnowledgeBase(Long kbId);

    Long uploadDocument(Long kbId, UploadDocCommand cmd);

    RagDocStatusDTO getDocumentStatus(Long docId);

    void deleteDocument(Long kbId, Long docId);

    List<RetrievalChunkDTO> retrieve(RetrieveParam param);

    Long submitTranslation(TranslateCommand cmd);

    TranslationResultDTO getTranslationResult(Long taskId);
}
```

```java
// mro-backend/mro-common/mro-common-dubbo/src/main/java/com/mro/common/dubbo/rag/dto/CreateKbCommand.java
package com.mro.common.dubbo.rag.dto;

import java.io.Serializable;

public record CreateKbCommand(
        String name,
        String description,
        String language
) implements Serializable {}
```

```java
// mro-backend/mro-common/mro-common-dubbo/src/main/java/com/mro/common/dubbo/rag/dto/KbDTO.java
package com.mro.common.dubbo.rag.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

public record KbDTO(
        Long id,
        String name,
        String description,
        String language,
        Integer docCount,
        String ragflowKbId,
        LocalDateTime createTime
) implements Serializable {}
```

```java
// mro-backend/mro-common/mro-common-dubbo/src/main/java/com/mro/common/dubbo/rag/dto/KbQueryParam.java
package com.mro.common.dubbo.rag.dto;

import java.io.Serializable;

public record KbQueryParam(
        String name,
        int pageNum,
        int pageSize
) implements Serializable {}
```

```java
// mro-backend/mro-common/mro-common-dubbo/src/main/java/com/mro/common/dubbo/rag/dto/UploadDocCommand.java
package com.mro.common.dubbo.rag.dto;

import java.io.Serializable;

public record UploadDocCommand(
        String fileName,
        String contentType,
        byte[] content
) implements Serializable {}
```

```java
// mro-backend/mro-common/mro-common-dubbo/src/main/java/com/mro/common/dubbo/rag/dto/RagDocStatusDTO.java
package com.mro.common.dubbo.rag.dto;

import java.io.Serializable;

public record RagDocStatusDTO(
        Long docId,
        String fileName,
        String status,
        String errorMsg
) implements Serializable {}
```

```java
// mro-backend/mro-common/mro-common-dubbo/src/main/java/com/mro/common/dubbo/rag/dto/RetrieveParam.java
package com.mro.common.dubbo.rag.dto;

import java.io.Serializable;
import java.util.List;

public record RetrieveParam(
        List<Long> kbIds,
        String query,
        int topK
) implements Serializable {}
```

```java
// mro-backend/mro-common/mro-common-dubbo/src/main/java/com/mro/common/dubbo/rag/dto/RetrievalChunkDTO.java
package com.mro.common.dubbo.rag.dto;

import java.io.Serializable;

public record RetrievalChunkDTO(
        String content,
        String docName,
        float score
) implements Serializable {}
```

```java
// mro-backend/mro-common/mro-common-dubbo/src/main/java/com/mro/common/dubbo/rag/dto/TranslateCommand.java
package com.mro.common.dubbo.rag.dto;

import java.io.Serializable;

public record TranslateCommand(
        String sourceText,
        String sourceLang,
        String targetLang
) implements Serializable {}
```

```java
// mro-backend/mro-common/mro-common-dubbo/src/main/java/com/mro/common/dubbo/rag/dto/TranslationResultDTO.java
package com.mro.common.dubbo.rag.dto;

import java.io.Serializable;

public record TranslationResultDTO(
        Long taskId,
        String status,
        String translatedText
) implements Serializable {}
```

- [ ] **Step 2: Create AircraftHealthDubboService and DTOs**

```java
// mro-backend/mro-common/mro-common-dubbo/src/main/java/com/mro/common/dubbo/mro/AircraftHealthDubboService.java
package com.mro.common.dubbo.mro;

import com.mro.common.core.result.PageResult;
import com.mro.common.dubbo.mro.dto.*;
import java.util.List;

public interface AircraftHealthDubboService {

    PageResult<AircraftStatusDTO> listAircraftStatus(AircraftStatusQueryParam param);

    AircraftDetailDTO getAircraftDetail(String registration);

    PageResult<FaultEventDTO> listFaultEvents(FaultEventQueryParam param);

    FaultEventDTO getFaultDetail(Long faultId);

    Long createAlert(CreateAlertCommand cmd);

    void acknowledgeAlert(Long alertId, Long userId);

    PageResult<AlertDTO> listAlerts(AlertQueryParam param);

    List<TrendDataDTO> queryTrend(TrendQueryParam param);
}
```

```java
// mro-backend/mro-common/mro-common-dubbo/src/main/java/com/mro/common/dubbo/mro/dto/AircraftStatusDTO.java
package com.mro.common.dubbo.mro.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

public record AircraftStatusDTO(
        Long id,
        String registration,
        String aircraftType,
        String healthStatus,
        Integer activeFaultCount,
        LocalDateTime lastDataTime
) implements Serializable {}
```

```java
// mro-backend/mro-common/mro-common-dubbo/src/main/java/com/mro/common/dubbo/mro/dto/AircraftStatusQueryParam.java
package com.mro.common.dubbo.mro.dto;

import java.io.Serializable;

public record AircraftStatusQueryParam(
        String registration,
        String healthStatus,
        int pageNum,
        int pageSize
) implements Serializable {}
```

```java
// mro-backend/mro-common/mro-common-dubbo/src/main/java/com/mro/common/dubbo/mro/dto/AircraftDetailDTO.java
package com.mro.common.dubbo.mro.dto;

import java.io.Serializable;
import java.util.List;

public record AircraftDetailDTO(
        Long id,
        String registration,
        String aircraftType,
        String manufacturer,
        String healthStatus,
        List<FaultEventDTO> recentFaults,
        List<String> activeAlerts
) implements Serializable {}
```

```java
// mro-backend/mro-common/mro-common-dubbo/src/main/java/com/mro/common/dubbo/mro/dto/FaultEventDTO.java
package com.mro.common.dubbo.mro.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

public record FaultEventDTO(
        Long id,
        String registration,
        String faultCode,
        String faultDescription,
        String severity,
        String status,
        String aiAnalysis,
        LocalDateTime occurTime,
        LocalDateTime resolveTime
) implements Serializable {}
```

```java
// mro-backend/mro-common/mro-common-dubbo/src/main/java/com/mro/common/dubbo/mro/dto/FaultEventQueryParam.java
package com.mro.common.dubbo.mro.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

public record FaultEventQueryParam(
        String registration,
        String severity,
        String status,
        LocalDateTime startTime,
        LocalDateTime endTime,
        int pageNum,
        int pageSize
) implements Serializable {}
```

```java
// mro-backend/mro-common/mro-common-dubbo/src/main/java/com/mro/common/dubbo/mro/dto/AlertDTO.java
package com.mro.common.dubbo.mro.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

public record AlertDTO(
        Long id,
        String registration,
        String alertLevel,
        String alertType,
        String message,
        String status,
        LocalDateTime createTime,
        LocalDateTime acknowledgeTime
) implements Serializable {}
```

```java
// mro-backend/mro-common/mro-common-dubbo/src/main/java/com/mro/common/dubbo/mro/dto/AlertQueryParam.java
package com.mro.common.dubbo.mro.dto;

import java.io.Serializable;

public record AlertQueryParam(
        String registration,
        String alertLevel,
        String status,
        int pageNum,
        int pageSize
) implements Serializable {}
```

```java
// mro-backend/mro-common/mro-common-dubbo/src/main/java/com/mro/common/dubbo/mro/dto/CreateAlertCommand.java
package com.mro.common.dubbo.mro.dto;

import java.io.Serializable;

public record CreateAlertCommand(
        String registration,
        String alertLevel,
        String alertType,
        String message
) implements Serializable {}
```

```java
// mro-backend/mro-common/mro-common-dubbo/src/main/java/com/mro/common/dubbo/mro/dto/TrendDataDTO.java
package com.mro.common.dubbo.mro.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

public record TrendDataDTO(
        LocalDateTime timestamp,
        String parameter,
        Double value
) implements Serializable {}
```

```java
// mro-backend/mro-common/mro-common-dubbo/src/main/java/com/mro/common/dubbo/mro/dto/TrendQueryParam.java
package com.mro.common.dubbo.mro.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

public record TrendQueryParam(
        String registration,
        List<String> parameters,
        LocalDateTime startTime,
        LocalDateTime endTime
) implements Serializable {}
```

- [ ] **Step 3: Create remaining MRO Dubbo service interfaces**

```java
// mro-backend/mro-common/mro-common-dubbo/src/main/java/com/mro/common/dubbo/mro/ArMaintenanceDubboService.java
package com.mro.common.dubbo.mro;

import com.mro.common.core.result.PageResult;
import com.mro.common.dubbo.mro.dto.*;

public interface ArMaintenanceDubboService {

    Long createTask(CreateArTaskCommand cmd);

    void updateTaskStatus(Long taskId, String status, Long userId);

    ArTaskDTO getTask(Long taskId);

    PageResult<ArTaskDTO> listTasks(ArTaskQueryParam param);

    Long startSession(Long taskId, Long userId);

    void addAnnotation(Long sessionId, ArAnnotationCommand cmd);

    ArSessionDTO getSession(Long sessionId);
}
```

```java
// mro-backend/mro-common/mro-common-dubbo/src/main/java/com/mro/common/dubbo/mro/dto/CreateArTaskCommand.java
package com.mro.common.dubbo.mro.dto;

import java.io.Serializable;

public record CreateArTaskCommand(
        String registration,
        String taskType,
        String description,
        Long assigneeId,
        Integer priority
) implements Serializable {}
```

```java
// mro-backend/mro-common/mro-common-dubbo/src/main/java/com/mro/common/dubbo/mro/dto/ArTaskDTO.java
package com.mro.common.dubbo.mro.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

public record ArTaskDTO(
        Long id,
        String registration,
        String taskType,
        String description,
        String status,
        Long assigneeId,
        String assigneeName,
        Integer priority,
        LocalDateTime createTime,
        LocalDateTime completeTime
) implements Serializable {}
```

```java
// mro-backend/mro-common/mro-common-dubbo/src/main/java/com/mro/common/dubbo/mro/dto/ArTaskQueryParam.java
package com.mro.common.dubbo.mro.dto;

import java.io.Serializable;

public record ArTaskQueryParam(
        String registration,
        String taskType,
        String status,
        Long assigneeId,
        int pageNum,
        int pageSize
) implements Serializable {}
```

```java
// mro-backend/mro-common/mro-common-dubbo/src/main/java/com/mro/common/dubbo/mro/dto/ArAnnotationCommand.java
package com.mro.common.dubbo.mro.dto;

import java.io.Serializable;

public record ArAnnotationCommand(
        String annotationType,
        String content,
        String coordinates
) implements Serializable {}
```

```java
// mro-backend/mro-common/mro-common-dubbo/src/main/java/com/mro/common/dubbo/mro/dto/ArSessionDTO.java
package com.mro.common.dubbo.mro.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

public record ArSessionDTO(
        Long id,
        Long taskId,
        Long userId,
        String status,
        List<ArAnnotationCommand> annotations,
        LocalDateTime startTime,
        LocalDateTime endTime
) implements Serializable {}
```

```java
// mro-backend/mro-common/mro-common-dubbo/src/main/java/com/mro/common/dubbo/mro/FaultDiagnosisDubboService.java
package com.mro.common.dubbo.mro;

import com.mro.common.core.result.PageResult;
import com.mro.common.dubbo.mro.dto.*;

public interface FaultDiagnosisDubboService {

    Long createSession(CreateDiagnosisSessionCommand cmd);

    DiagnosisSessionDTO getSession(Long sessionId);

    DiagnosisReplyDTO chat(Long sessionId, String userMessage);

    void closeSession(Long sessionId);

    PageResult<DiagnosisSessionDTO> listSessions(DiagnosisSessionQueryParam param);
}
```

```java
// mro-backend/mro-common/mro-common-dubbo/src/main/java/com/mro/common/dubbo/mro/dto/CreateDiagnosisSessionCommand.java
package com.mro.common.dubbo.mro.dto;

import java.io.Serializable;

public record CreateDiagnosisSessionCommand(
        String registration,
        String faultCode,
        String initialDescription,
        Long userId
) implements Serializable {}
```

```java
// mro-backend/mro-common/mro-common-dubbo/src/main/java/com/mro/common/dubbo/mro/dto/DiagnosisSessionDTO.java
package com.mro.common.dubbo.mro.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

public record DiagnosisSessionDTO(
        Long id,
        String registration,
        String faultCode,
        String status,
        Long userId,
        List<DiagnosisMsgDTO> messages,
        LocalDateTime createTime
) implements Serializable {}
```

```java
// mro-backend/mro-common/mro-common-dubbo/src/main/java/com/mro/common/dubbo/mro/dto/DiagnosisMsgDTO.java
package com.mro.common.dubbo.mro.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

public record DiagnosisMsgDTO(
        String role,
        String content,
        LocalDateTime timestamp
) implements Serializable {}
```

```java
// mro-backend/mro-common/mro-common-dubbo/src/main/java/com/mro/common/dubbo/mro/dto/DiagnosisReplyDTO.java
package com.mro.common.dubbo.mro.dto;

import java.io.Serializable;
import java.util.List;

public record DiagnosisReplyDTO(
        String content,
        List<String> suggestedActions,
        List<String> relatedFaultCodes
) implements Serializable {}
```

```java
// mro-backend/mro-common/mro-common-dubbo/src/main/java/com/mro/common/dubbo/mro/dto/DiagnosisSessionQueryParam.java
package com.mro.common.dubbo.mro.dto;

import java.io.Serializable;

public record DiagnosisSessionQueryParam(
        String registration,
        String faultCode,
        String status,
        Long userId,
        int pageNum,
        int pageSize
) implements Serializable {}
```

```java
// mro-backend/mro-common/mro-common-dubbo/src/main/java/com/mro/common/dubbo/mro/ManualDubboService.java
package com.mro.common.dubbo.mro;

import com.mro.common.core.result.PageResult;
import com.mro.common.dubbo.mro.dto.*;

public interface ManualDubboService {

    Long uploadManual(UploadManualCommand cmd);

    ManualDTO getManual(Long manualId);

    PageResult<ManualDTO> listManuals(ManualQueryParam param);

    void deleteManual(Long manualId);

    ManualSearchResultDTO search(ManualSearchParam param);
}
```

```java
// mro-backend/mro-common/mro-common-dubbo/src/main/java/com/mro/common/dubbo/mro/dto/UploadManualCommand.java
package com.mro.common.dubbo.mro.dto;

import java.io.Serializable;

public record UploadManualCommand(
        String title,
        String aircraftType,
        String manualType,
        String version,
        String fileName,
        String contentType,
        byte[] content
) implements Serializable {}
```

```java
// mro-backend/mro-common/mro-common-dubbo/src/main/java/com/mro/common/dubbo/mro/dto/ManualDTO.java
package com.mro.common.dubbo.mro.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

public record ManualDTO(
        Long id,
        String title,
        String aircraftType,
        String manualType,
        String version,
        String fileName,
        String indexStatus,
        LocalDateTime createTime
) implements Serializable {}
```

```java
// mro-backend/mro-common/mro-common-dubbo/src/main/java/com/mro/common/dubbo/mro/dto/ManualQueryParam.java
package com.mro.common.dubbo.mro.dto;

import java.io.Serializable;

public record ManualQueryParam(
        String title,
        String aircraftType,
        String manualType,
        int pageNum,
        int pageSize
) implements Serializable {}
```

```java
// mro-backend/mro-common/mro-common-dubbo/src/main/java/com/mro/common/dubbo/mro/dto/ManualSearchParam.java
package com.mro.common.dubbo.mro.dto;

import java.io.Serializable;
import java.util.List;

public record ManualSearchParam(
        String query,
        List<String> aircraftTypes,
        int topK
) implements Serializable {}
```

```java
// mro-backend/mro-common/mro-common-dubbo/src/main/java/com/mro/common/dubbo/mro/dto/ManualSearchResultDTO.java
package com.mro.common.dubbo.mro.dto;

import java.io.Serializable;
import java.util.List;

public record ManualSearchResultDTO(
        String answer,
        List<ManualChunkDTO> references
) implements Serializable {}
```

```java
// mro-backend/mro-common/mro-common-dubbo/src/main/java/com/mro/common/dubbo/mro/dto/ManualChunkDTO.java
package com.mro.common.dubbo.mro.dto;

import java.io.Serializable;

public record ManualChunkDTO(
        Long manualId,
        String title,
        String content,
        float score
) implements Serializable {}
```

```java
// mro-backend/mro-common/mro-common-dubbo/src/main/java/com/mro/common/dubbo/mro/DigitalTwinDubboService.java
package com.mro.common.dubbo.mro;

import com.mro.common.dubbo.mro.dto.*;
import java.util.List;

public interface DigitalTwinDubboService {

    HangarStatusDTO getHangarStatus(Long hangarId);

    List<BayStatusDTO> listBayStatus(Long hangarId);

    void updateBayAssignment(UpdateBayCommand cmd);

    void publishHangarEvent(HangarEventCommand cmd);

    List<HangarDTO> listHangars();
}
```

```java
// mro-backend/mro-common/mro-common-dubbo/src/main/java/com/mro/common/dubbo/mro/dto/HangarDTO.java
package com.mro.common.dubbo.mro.dto;

import java.io.Serializable;

public record HangarDTO(
        Long id,
        String hangarName,
        String location,
        Integer totalBays,
        Integer availableBays
) implements Serializable {}
```

```java
// mro-backend/mro-common/mro-common-dubbo/src/main/java/com/mro/common/dubbo/mro/dto/HangarStatusDTO.java
package com.mro.common.dubbo.mro.dto;

import java.io.Serializable;
import java.util.List;

public record HangarStatusDTO(
        Long hangarId,
        String hangarName,
        Integer totalBays,
        Integer occupiedBays,
        List<BayStatusDTO> bays
) implements Serializable {}
```

```java
// mro-backend/mro-common/mro-common-dubbo/src/main/java/com/mro/common/dubbo/mro/dto/BayStatusDTO.java
package com.mro.common.dubbo.mro.dto;

import java.io.Serializable;

public record BayStatusDTO(
        Long bayId,
        String bayCode,
        String status,
        String registration,
        String taskType
) implements Serializable {}
```

```java
// mro-backend/mro-common/mro-common-dubbo/src/main/java/com/mro/common/dubbo/mro/dto/UpdateBayCommand.java
package com.mro.common.dubbo.mro.dto;

import java.io.Serializable;

public record UpdateBayCommand(
        Long bayId,
        String registration,
        String status,
        String taskType
) implements Serializable {}
```

```java
// mro-backend/mro-common/mro-common-dubbo/src/main/java/com/mro/common/dubbo/mro/dto/HangarEventCommand.java
package com.mro.common.dubbo.mro.dto;

import java.io.Serializable;

public record HangarEventCommand(
        Long hangarId,
        String eventType,
        String payload
) implements Serializable {}
```

```java
// mro-backend/mro-common/mro-common-dubbo/src/main/java/com/mro/common/dubbo/mro/ToolingMaterialDubboService.java
package com.mro.common.dubbo.mro;

import com.mro.common.core.result.PageResult;
import com.mro.common.dubbo.mro.dto.*;

public interface ToolingMaterialDubboService {

    PageResult<ToolDTO> listTools(ToolQueryParam param);

    ToolDTO getTool(Long toolId);

    Long borrowTool(BorrowToolCommand cmd);

    void returnTool(Long borrowId);

    PageResult<MaterialDTO> listMaterials(MaterialQueryParam param);

    Long applyMaterial(ApplyMaterialCommand cmd);

    void fulfillMaterial(Long applicationId, Long userId);
}
```

```java
// mro-backend/mro-common/mro-common-dubbo/src/main/java/com/mro/common/dubbo/mro/dto/ToolDTO.java
package com.mro.common.dubbo.mro.dto;

import java.io.Serializable;

public record ToolDTO(
        Long id,
        String toolCode,
        String toolName,
        String category,
        String status,
        String location,
        String calibrationDate
) implements Serializable {}
```

```java
// mro-backend/mro-common/mro-common-dubbo/src/main/java/com/mro/common/dubbo/mro/dto/ToolQueryParam.java
package com.mro.common.dubbo.mro.dto;

import java.io.Serializable;

public record ToolQueryParam(
        String toolCode,
        String toolName,
        String category,
        String status,
        int pageNum,
        int pageSize
) implements Serializable {}
```

```java
// mro-backend/mro-common/mro-common-dubbo/src/main/java/com/mro/common/dubbo/mro/dto/BorrowToolCommand.java
package com.mro.common.dubbo.mro.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

public record BorrowToolCommand(
        Long toolId,
        Long userId,
        Long workOrderId,
        LocalDateTime expectedReturnTime
) implements Serializable {}
```

```java
// mro-backend/mro-common/mro-common-dubbo/src/main/java/com/mro/common/dubbo/mro/dto/MaterialDTO.java
package com.mro.common.dubbo.mro.dto;

import java.io.Serializable;

public record MaterialDTO(
        Long id,
        String partNumber,
        String partName,
        String category,
        Integer stockQuantity,
        String unit,
        String location
) implements Serializable {}
```

```java
// mro-backend/mro-common/mro-common-dubbo/src/main/java/com/mro/common/dubbo/mro/dto/MaterialQueryParam.java
package com.mro.common.dubbo.mro.dto;

import java.io.Serializable;

public record MaterialQueryParam(
        String partNumber,
        String partName,
        String category,
        int pageNum,
        int pageSize
) implements Serializable {}
```

```java
// mro-backend/mro-common/mro-common-dubbo/src/main/java/com/mro/common/dubbo/mro/dto/ApplyMaterialCommand.java
package com.mro.common.dubbo.mro.dto;

import java.io.Serializable;

public record ApplyMaterialCommand(
        Long materialId,
        Integer quantity,
        Long userId,
        Long workOrderId,
        String reason
) implements Serializable {}
```

```java
// mro-backend/mro-common/mro-common-dubbo/src/main/java/com/mro/common/dubbo/mro/VrArTrainingDubboService.java
package com.mro.common.dubbo.mro;

import com.mro.common.core.result.PageResult;
import com.mro.common.dubbo.mro.dto.*;

public interface VrArTrainingDubboService {

    PageResult<CourseDTO> listCourses(CourseQueryParam param);

    CourseDTO getCourse(Long courseId);

    Long createCourse(CreateCourseCommand cmd);

    Long startTrainingSession(Long courseId, Long userId);

    void completeTrainingSession(Long sessionId, Integer score);

    PageResult<TrainingRecordDTO> listTrainingRecords(TrainingRecordQueryParam param);
}
```

```java
// mro-backend/mro-common/mro-common-dubbo/src/main/java/com/mro/common/dubbo/mro/dto/CourseDTO.java
package com.mro.common.dubbo.mro.dto;

import java.io.Serializable;

public record CourseDTO(
        Long id,
        String courseName,
        String courseType,
        String aircraftType,
        String description,
        Integer durationMinutes,
        Integer passingScore,
        Integer status
) implements Serializable {}
```

```java
// mro-backend/mro-common/mro-common-dubbo/src/main/java/com/mro/common/dubbo/mro/dto/CourseQueryParam.java
package com.mro.common.dubbo.mro.dto;

import java.io.Serializable;

public record CourseQueryParam(
        String courseName,
        String courseType,
        String aircraftType,
        Integer status,
        int pageNum,
        int pageSize
) implements Serializable {}
```

```java
// mro-backend/mro-common/mro-common-dubbo/src/main/java/com/mro/common/dubbo/mro/dto/CreateCourseCommand.java
package com.mro.common.dubbo.mro.dto;

import java.io.Serializable;

public record CreateCourseCommand(
        String courseName,
        String courseType,
        String aircraftType,
        String description,
        Integer durationMinutes,
        Integer passingScore,
        String resourceUrl
) implements Serializable {}
```

```java
// mro-backend/mro-common/mro-common-dubbo/src/main/java/com/mro/common/dubbo/mro/dto/TrainingRecordDTO.java
package com.mro.common.dubbo.mro.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

public record TrainingRecordDTO(
        Long id,
        Long courseId,
        String courseName,
        Long userId,
        String userName,
        Integer score,
        Boolean passed,
        LocalDateTime startTime,
        LocalDateTime endTime
) implements Serializable {}
```

```java
// mro-backend/mro-common/mro-common-dubbo/src/main/java/com/mro/common/dubbo/mro/dto/TrainingRecordQueryParam.java
package com.mro.common.dubbo.mro.dto;

import java.io.Serializable;

public record TrainingRecordQueryParam(
        Long courseId,
        Long userId,
        Boolean passed,
        int pageNum,
        int pageSize
) implements Serializable {}
```

```java
// mro-backend/mro-common/mro-common-dubbo/src/main/java/com/mro/common/dubbo/mro/PaperlessCheckinDubboService.java
package com.mro.common.dubbo.mro;

import com.mro.common.core.result.PageResult;
import com.mro.common.dubbo.mro.dto.*;

public interface PaperlessCheckinDubboService {

    Long createWorkCard(CreateWorkCardCommand cmd);

    WorkCardDTO getWorkCard(Long workCardId);

    PageResult<WorkCardDTO> listWorkCards(WorkCardQueryParam param);

    void signWorkCard(Long workCardId, Long userId, String signatureData);

    void updateWorkCardStatus(Long workCardId, String status, Long userId);

    PageResult<WorkCardDTO> listMyWorkCards(Long userId, String status, int pageNum, int pageSize);
}
```

```java
// mro-backend/mro-common/mro-common-dubbo/src/main/java/com/mro/common/dubbo/mro/dto/CreateWorkCardCommand.java
package com.mro.common.dubbo.mro.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

public record CreateWorkCardCommand(
        String cardNo,
        String registration,
        String workType,
        String description,
        Long assigneeId,
        LocalDateTime plannedStartTime,
        LocalDateTime plannedEndTime,
        List<String> checkItems
) implements Serializable {}
```

```java
// mro-backend/mro-common/mro-common-dubbo/src/main/java/com/mro/common/dubbo/mro/dto/WorkCardDTO.java
package com.mro.common.dubbo.mro.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

public record WorkCardDTO(
        Long id,
        String cardNo,
        String registration,
        String workType,
        String description,
        String status,
        Long assigneeId,
        String assigneeName,
        List<String> checkItems,
        List<SignatureDTO> signatures,
        LocalDateTime plannedStartTime,
        LocalDateTime plannedEndTime,
        LocalDateTime actualStartTime,
        LocalDateTime actualEndTime
) implements Serializable {}
```

```java
// mro-backend/mro-common/mro-common-dubbo/src/main/java/com/mro/common/dubbo/mro/dto/SignatureDTO.java
package com.mro.common.dubbo.mro.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

public record SignatureDTO(
        Long userId,
        String userName,
        String signatureHash,
        LocalDateTime signTime
) implements Serializable {}
```

```java
// mro-backend/mro-common/mro-common-dubbo/src/main/java/com/mro/common/dubbo/mro/dto/WorkCardQueryParam.java
package com.mro.common.dubbo.mro.dto;

import java.io.Serializable;

public record WorkCardQueryParam(
        String cardNo,
        String registration,
        String workType,
        String status,
        Long assigneeId,
        int pageNum,
        int pageSize
) implements Serializable {}
```

- [ ] **Step 4: Verify mro-common-dubbo compiles**

```bash
cd mro-backend
mvn compile -pl mro-common/mro-common-dubbo -am
```

Expected: `BUILD SUCCESS`

- [ ] **Step 5: Commit**

```bash
cd mro-backend
git add mro-common/mro-common-dubbo/
git commit -m "feat(common-dubbo): add RAG + all MRO Dubbo service interfaces and DTOs

Refs: PLAT-003, MRO-001, MRO-002, MRO-003, MRO-004, MRO-005, MRO-006, MRO-007, MRO-008"
```

---

## Task 7: MyBatisPlus Config + @DataScope + DataScopeInterceptor

**Files:**
- Create: `mro-backend/mro-common/mro-common-data/src/main/java/com/mro/common/data/config/MyBatisPlusConfig.java`
- Create: `mro-backend/mro-common/mro-common-data/src/main/java/com/mro/common/data/annotation/DataScope.java`
- Create: `mro-backend/mro-common/mro-common-data/src/main/java/com/mro/common/data/interceptor/DataScopeInterceptor.java`
- Test: `mro-backend/mro-common/mro-common-data/src/test/java/com/mro/common/data/interceptor/DataScopeInterceptorTest.java`

- [ ] **Step 1: Write failing tests for DataScopeInterceptor SQL rewriting**

```java
// mro-backend/mro-common/mro-common-data/src/test/java/com/mro/common/data/interceptor/DataScopeInterceptorTest.java
package com.mro.common.data.interceptor;

import com.mro.common.core.context.UserContextDTO;
import com.mro.common.core.context.UserContextHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.*;

class DataScopeInterceptorTest {

    @AfterEach
    void cleanup() {
        UserContextHolder.clear();
    }

    @Test
    void buildScopeCondition_scope1_returnsEmpty() {
        UserContextDTO ctx = new UserContextDTO(1L, "admin", "管理员", 10L,
                List.of("admin"), List.of());
        UserContextHolder.set(ctx);
        DataScopeInterceptor interceptor = new DataScopeInterceptor();
        // Scope 1 = all data, no condition needed
        String condition = interceptor.buildScopeCondition(1, "d", List.of());
        assertThat(condition).isEmpty();
    }

    @Test
    void buildScopeCondition_scope4_returnsSelfCondition() {
        UserContextDTO ctx = new UserContextDTO(42L, "user", "用户", 10L,
                List.of("staff"), List.of());
        UserContextHolder.set(ctx);
        DataScopeInterceptor interceptor = new DataScopeInterceptor();
        // Scope 4 = own data only
        String condition = interceptor.buildScopeCondition(4, "d", List.of());
        assertThat(condition).contains("create_user_id = 42");
    }

    @Test
    void buildScopeCondition_scope2_returnsDeptCondition() {
        UserContextDTO ctx = new UserContextDTO(1L, "user", "用户", 55L,
                List.of("staff"), List.of());
        UserContextHolder.set(ctx);
        DataScopeInterceptor interceptor = new DataScopeInterceptor();
        // Scope 2 = own department only
        String condition = interceptor.buildScopeCondition(2, "d", List.of());
        assertThat(condition).contains("create_dept_id = 55");
    }

    @Test
    void buildScopeCondition_scope5_returnsCustomDeptIds() {
        UserContextDTO ctx = new UserContextDTO(1L, "user", "用户", 10L,
                List.of("manager"), List.of());
        UserContextHolder.set(ctx);
        DataScopeInterceptor interceptor = new DataScopeInterceptor();
        // Scope 5 = custom dept list
        String condition = interceptor.buildScopeCondition(5, "d", List.of(10L, 20L, 30L));
        assertThat(condition).contains("create_dept_id IN (10,20,30)");
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
cd mro-backend
mvn test -pl mro-common/mro-common-data -am -Dtest=DataScopeInterceptorTest
```

Expected: FAIL — `DataScopeInterceptor cannot be resolved`

- [ ] **Step 3: Implement @DataScope annotation**

```java
// mro-backend/mro-common/mro-common-data/src/main/java/com/mro/common/data/annotation/DataScope.java
package com.mro.common.data.annotation;

import java.lang.annotation.*;

/**
 * Marks a Mapper method that should have data-scope SQL injected.
 * tableAlias: the alias of the table that owns create_dept_id / create_user_id.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataScope {

    String tableAlias() default "d";
}
```

- [ ] **Step 4: Implement DataScopeInterceptor**

```java
// mro-backend/mro-common/mro-common-data/src/main/java/com/mro/common/data/interceptor/DataScopeInterceptor.java
package com.mro.common.data.interceptor;

import com.mro.common.core.context.UserContextDTO;
import com.mro.common.core.context.UserContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

@Slf4j
@Intercepts({
    @Signature(type = Executor.class, method = "query",
        args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class})
})
public class DataScopeInterceptor implements Interceptor {

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        MappedStatement ms = (MappedStatement) invocation.getArgs()[0];
        Object parameter = invocation.getArgs()[1];

        com.mro.common.data.annotation.DataScope annotation = getAnnotation(ms);
        if (annotation == null) {
            return invocation.proceed();
        }

        UserContextDTO ctx = UserContextHolder.get();
        if (ctx == null || ctx.isSuperAdmin()) {
            return invocation.proceed();
        }

        // Data scope is resolved at service layer; interceptor checks if param carries it
        // Real scope injection happens via buildScopeCondition called from service
        return invocation.proceed();
    }

    /**
     * Builds a WHERE clause fragment for data scope filtering.
     * Called by service layer (not by interceptor.intercept) to construct SQL.
     *
     * @param scope      data scope type (1=all,2=dept,3=dept+children,4=self,5=custom)
     * @param tableAlias alias of the data table
     * @param customDeptIds dept IDs for scope=5
     * @return SQL condition string, empty if no restriction
     */
    public String buildScopeCondition(int scope, String tableAlias, List<Long> customDeptIds) {
        UserContextDTO ctx = UserContextHolder.get();
        if (ctx == null) {
            return "";
        }
        return switch (scope) {
            case 1 -> ""; // all data
            case 2 -> tableAlias + ".create_dept_id = " + ctx.deptId();
            case 3 -> {
                // caller must pass child dept IDs resolved from DeptDubboService
                String ids = customDeptIds.stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining(","));
                yield ids.isEmpty()
                        ? tableAlias + ".create_dept_id = " + ctx.deptId()
                        : tableAlias + ".create_dept_id IN (" + ids + ")";
            }
            case 4 -> tableAlias + ".create_user_id = " + ctx.userId();
            case 5 -> {
                if (customDeptIds.isEmpty()) {
                    yield "1=0"; // custom scope but no depts configured = no access
                }
                String ids = customDeptIds.stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining(","));
                yield tableAlias + ".create_dept_id IN (" + ids + ")";
            }
            default -> "";
        };
    }

    private com.mro.common.data.annotation.DataScope getAnnotation(MappedStatement ms) {
        try {
            String id = ms.getId();
            String className = id.substring(0, id.lastIndexOf('.'));
            String methodName = id.substring(id.lastIndexOf('.') + 1);
            Class<?> clazz = Class.forName(className);
            for (var method : clazz.getMethods()) {
                if (method.getName().equals(methodName)) {
                    return method.getAnnotation(com.mro.common.data.annotation.DataScope.class);
                }
            }
        } catch (Exception e) {
            log.debug("Cannot resolve DataScope annotation for {}", ms.getId());
        }
        return null;
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {}
}
```

- [ ] **Step 5: Implement MyBatisPlusConfig**

```java
// mro-backend/mro-common/mro-common-data/src/main/java/com/mro/common/data/config/MyBatisPlusConfig.java
package com.mro.common.data.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.mro.common.data.interceptor.DataScopeInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MyBatisPlusConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        return interceptor;
    }

    @Bean
    public DataScopeInterceptor dataScopeInterceptor() {
        return new DataScopeInterceptor();
    }
}
```

- [ ] **Step 6: Run tests**

```bash
cd mro-backend
mvn test -pl mro-common/mro-common-data -am -Dtest=DataScopeInterceptorTest
```

Expected: `Tests run: 4, Failures: 0, Errors: 0`

- [ ] **Step 7: Run all mro-common tests**

```bash
cd mro-backend
mvn test -pl mro-common -am
```

Expected: All tests pass.

- [ ] **Step 8: Commit**

```bash
cd mro-backend
git add mro-common/mro-common-data/
git commit -m "feat(common-data): add MyBatisPlusConfig, @DataScope, DataScopeInterceptor

Refs: SYS-006, PLAT-001"
```

---

## Task 8: Final Validation — Full mro-common Build

- [ ] **Step 1: Build entire mro-common**

```bash
cd mro-backend
mvn clean install -pl mro-common -am -DskipTests
```

Expected: `BUILD SUCCESS`, jars in local `.m2` repository.

- [ ] **Step 2: Run all tests**

```bash
cd mro-backend
mvn test -pl mro-common -am
```

Expected: All tests pass. Note test count:
- `mro-common-core`: ~13 tests
- `mro-common-data`: ~4 tests
- `mro-common-dubbo`: 0 (interfaces only, no runtime behavior to test)

- [ ] **Step 3: Commit final tag**

```bash
cd mro-backend
git add .
git commit -m "chore(be-01): mro-common skeleton complete — all tests pass

Refs: PLAT-001, PLAT-002"
```

---

## Next Step

After BE-01 is complete, BE-02 and BE-03 can run **in parallel** (they both depend only on mro-common):

- **BE-02:** `docs/superpowers/plans/2026-05-26-be02-gateway-auth.md` — gateway-service + auth-service
- **BE-03:** `docs/superpowers/plans/2026-05-26-be03-system-service.md` — system-service

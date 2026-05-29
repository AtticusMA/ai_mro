# BE-03: system-service

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement `system-service` — a Dubbo provider on port 20881 that manages the `mro_system` MySQL schema. Covers six business modules: dept, user, role, menu, dict, and data-permission. Exposes `DeptDubboService`, `UserDubboService`, `RoleDubboService`, `MenuDubboService`, `DictDubboService` to be consumed by `manage-web`.

**Architecture:** Single Spring Boot service with Dubbo provider. MyBatis-Plus for ORM. Six domain packages, each with entity/mapper/service layers. Data-permission filtering injected at service layer using `DataScopeInterceptor.buildScopeCondition()`. No HTTP controllers — all public API is Dubbo.

**Tech Stack:** Spring Boot 3.3, Dubbo 3.3, MyBatis-Plus 3.5, MySQL 8 (`mro_system` schema), Flyway, Java 21 virtual threads, Lombok

**Prerequisites:** BE-01 complete (mro-common installed in local Maven).

**Refs:** SYS-001 (dept), SYS-002 (user), SYS-003 (role), SYS-004 (menu), SYS-005 (dict), SYS-006 (data-permission), PLAT-002

---

## File Structure

```
mro-backend/system-service/
├── pom.xml
└── src/
    ├── main/java/com/mro/system/
    │   ├── SystemServiceApplication.java
    │   ├── dept/
    │   │   ├── entity/SysDept.java
    │   │   ├── mapper/SysDeptMapper.java
    │   │   ├── service/DeptService.java
    │   │   └── service/DeptDubboServiceImpl.java
    │   ├── user/
    │   │   ├── entity/SysUser.java
    │   │   ├── mapper/SysUserMapper.java
    │   │   ├── service/UserService.java
    │   │   └── service/UserDubboServiceImpl.java
    │   ├── role/
    │   │   ├── entity/SysRole.java
    │   │   ├── entity/SysUserRole.java
    │   │   ├── entity/SysRoleMenu.java
    │   │   ├── mapper/SysRoleMapper.java
    │   │   ├── mapper/SysUserRoleMapper.java
    │   │   ├── mapper/SysRoleMenuMapper.java
    │   │   ├── service/RoleService.java
    │   │   └── service/RoleDubboServiceImpl.java
    │   ├── menu/
    │   │   ├── entity/SysMenu.java
    │   │   ├── mapper/SysMenuMapper.java
    │   │   ├── service/MenuService.java
    │   │   └── service/MenuDubboServiceImpl.java
    │   └── dict/
    │       ├── entity/SysDictType.java
    │       ├── entity/SysDictData.java
    │       ├── mapper/SysDictTypeMapper.java
    │       ├── mapper/SysDictDataMapper.java
    │       ├── service/DictService.java
    │       └── service/DictDubboServiceImpl.java
    ├── main/resources/
    │   ├── application.yml
    │   └── db/migration/V1__system_schema.sql
    └── test/java/com/mro/system/
        ├── dept/service/DeptServiceTest.java
        ├── user/service/UserServiceTest.java
        └── role/service/RoleServiceTest.java
```

---

## Task 1: system-service Module Setup

**Files:**
- Create: `mro-backend/system-service/pom.xml`
- Create: `mro-backend/system-service/src/main/java/com/mro/system/SystemServiceApplication.java`
- Create: `mro-backend/system-service/src/main/resources/application.yml`
- Create: `mro-backend/system-service/src/main/resources/db/migration/V1__system_schema.sql`

- [ ] **Step 1: Create system-service POM**

```xml
<!-- mro-backend/system-service/pom.xml -->
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

    <artifactId>system-service</artifactId>
    <name>MRO System Service</name>

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
        <dependency>
            <groupId>com.github.xiaoymin</groupId>
            <artifactId>knife4j-openapi3-jakarta-spring-boot-starter</artifactId>
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
# mro-backend/system-service/src/main/resources/application.yml
server:
  port: 8083

spring:
  application:
    name: system-service
  threads:
    virtual:
      enabled: true
  datasource:
    url: jdbc:mysql://${MYSQL_HOST:localhost}:3306/mro_system?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai
    username: ${MYSQL_USER:root}
    password: ${MYSQL_PASSWORD:root}
    driver-class-name: com.mysql.cj.jdbc.Driver
  flyway:
    locations: classpath:db/migration
    baseline-on-migrate: true
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
    name: system-service
  registry:
    address: nacos://${NACOS_ADDR:localhost:8848}?namespace=dev
  protocol:
    name: dubbo
    port: 20881
  provider:
    group: mro
    version: 1.0.0

mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
  global-config:
    db-config:
      logic-delete-field: isDeleted
      logic-delete-value: 1
      logic-not-delete-value: 0
```

- [ ] **Step 3: Create Flyway migration for mro_system schema**

```sql
-- mro-backend/system-service/src/main/resources/db/migration/V1__system_schema.sql
CREATE DATABASE IF NOT EXISTS mro_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE mro_system;

CREATE TABLE IF NOT EXISTS sys_dept (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    dept_name   VARCHAR(64)  NOT NULL,
    dept_code   VARCHAR(64)  NOT NULL,
    parent_id   BIGINT       NOT NULL DEFAULT 0,
    ancestors   VARCHAR(512) NOT NULL DEFAULT '',
    order_num   INT          NOT NULL DEFAULT 0,
    leader      VARCHAR(64)           DEFAULT NULL,
    phone       VARCHAR(20)           DEFAULT NULL,
    email       VARCHAR(100)          DEFAULT NULL,
    status      TINYINT      NOT NULL DEFAULT 1,
    is_deleted  TINYINT      NOT NULL DEFAULT 0,
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME              DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_dept_code (dept_code),
    KEY idx_parent_id (parent_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sys_user (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    username        VARCHAR(64)  NOT NULL,
    password        VARCHAR(100) NOT NULL,
    employee_no     VARCHAR(64)           DEFAULT NULL,
    real_name       VARCHAR(64)  NOT NULL,
    dept_id         BIGINT       NOT NULL,
    phone           VARCHAR(20)           DEFAULT NULL,
    email           VARCHAR(100)          DEFAULT NULL,
    avatar          VARCHAR(255)          DEFAULT NULL,
    status          TINYINT      NOT NULL DEFAULT 1,
    is_deleted      TINYINT      NOT NULL DEFAULT 0,
    create_user_id  BIGINT       NOT NULL DEFAULT 0,
    create_dept_id  BIGINT       NOT NULL DEFAULT 0,
    create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_user_id  BIGINT                DEFAULT NULL,
    update_time     DATETIME              DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username),
    UNIQUE KEY uk_phone (phone),
    UNIQUE KEY uk_employee_no (employee_no),
    KEY idx_dept_id (dept_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sys_role (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    role_name   VARCHAR(64)  NOT NULL,
    role_key    VARCHAR(100) NOT NULL,
    data_scope  TINYINT      NOT NULL DEFAULT 1,
    order_num   INT          NOT NULL DEFAULT 0,
    status      TINYINT      NOT NULL DEFAULT 1,
    is_deleted  TINYINT      NOT NULL DEFAULT 0,
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME              DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_key (role_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sys_user_role (
    id          BIGINT   NOT NULL AUTO_INCREMENT,
    user_id     BIGINT   NOT NULL,
    role_id     BIGINT   NOT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_role (user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sys_menu (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    parent_id   BIGINT       NOT NULL DEFAULT 0,
    menu_name   VARCHAR(64)  NOT NULL,
    menu_type   VARCHAR(10)  NOT NULL COMMENT 'M目录/C菜单/F按钮',
    path        VARCHAR(200)          DEFAULT NULL,
    component   VARCHAR(200)          DEFAULT NULL,
    perms       VARCHAR(100)          DEFAULT NULL,
    icon        VARCHAR(100)          DEFAULT NULL,
    order_num   INT          NOT NULL DEFAULT 0,
    visible     TINYINT      NOT NULL DEFAULT 1,
    status      TINYINT      NOT NULL DEFAULT 1,
    is_deleted  TINYINT      NOT NULL DEFAULT 0,
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME              DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sys_role_menu (
    id          BIGINT   NOT NULL AUTO_INCREMENT,
    role_id     BIGINT   NOT NULL,
    menu_id     BIGINT   NOT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_menu (role_id, menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sys_role_dept (
    id          BIGINT   NOT NULL AUTO_INCREMENT,
    role_id     BIGINT   NOT NULL,
    dept_id     BIGINT   NOT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_dept (role_id, dept_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sys_dict_type (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    dict_name   VARCHAR(100) NOT NULL,
    dict_type   VARCHAR(100) NOT NULL,
    status      TINYINT      NOT NULL DEFAULT 1,
    is_deleted  TINYINT      NOT NULL DEFAULT 0,
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME              DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_dict_type (dict_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sys_dict_data (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    dict_type   VARCHAR(100) NOT NULL,
    dict_label  VARCHAR(100) NOT NULL,
    dict_value  VARCHAR(100) NOT NULL,
    css_class   VARCHAR(100)          DEFAULT NULL,
    order_num   INT          NOT NULL DEFAULT 0,
    status      TINYINT      NOT NULL DEFAULT 1,
    is_deleted  TINYINT      NOT NULL DEFAULT 0,
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME              DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_dict_type (dict_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Default admin user (password = 'Admin@123', BCrypt)
INSERT IGNORE INTO sys_dept (id, dept_name, dept_code, parent_id, ancestors, order_num, status, create_time)
VALUES (1, '总公司', 'ROOT', 0, '0', 0, 1, NOW());

INSERT IGNORE INTO sys_user (id, username, password, real_name, dept_id, status,
                              create_user_id, create_dept_id, create_time)
VALUES (1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBpwTTyBumqnm2',
        '管理员', 1, 1, 0, 1, NOW());

INSERT IGNORE INTO sys_role (id, role_name, role_key, data_scope, order_num, status, create_time)
VALUES (1, '超级管理员', '*:*:*', 1, 0, 1, NOW());

INSERT IGNORE INTO sys_user_role (user_id, role_id, create_time) VALUES (1, 1, NOW());

-- Default menus (system module)
INSERT IGNORE INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, order_num, visible, status, create_time)
VALUES
(1,  0,  '系统管理', 'M', '/system',        NULL,                 NULL,          'Setting',  1, 1, 1, NOW()),
(2,  1,  '用户管理', 'C', 'user',           'system/user/index',  'user:list',   'User',     1, 1, 1, NOW()),
(3,  1,  '角色管理', 'C', 'role',           'system/role/index',  'role:list',   'Role',     2, 1, 1, NOW()),
(4,  1,  '菜单管理', 'C', 'menu',           'system/menu/index',  'menu:list',   'Menu',     3, 1, 1, NOW()),
(5,  1,  '部门管理', 'C', 'dept',           'system/dept/index',  'dept:list',   'Office',   4, 1, 1, NOW()),
(6,  1,  '字典管理', 'C', 'dict',           'system/dict/index',  'dict:list',   'Dict',     5, 1, 1, NOW()),
(10, 2,  '新增用户', 'F', NULL,             NULL,                 'user:add',    NULL,       1, 1, 1, NOW()),
(11, 2,  '编辑用户', 'F', NULL,             NULL,                 'user:edit',   NULL,       2, 1, 1, NOW()),
(12, 2,  '删除用户', 'F', NULL,             NULL,                 'user:delete', NULL,       3, 1, 1, NOW()),
(20, 5,  '新增部门', 'F', NULL,             NULL,                 'dept:add',    NULL,       1, 1, 1, NOW()),
(21, 5,  '编辑部门', 'F', NULL,             NULL,                 'dept:edit',   NULL,       2, 1, 1, NOW()),
(22, 5,  '删除部门', 'F', NULL,             NULL,                 'dept:delete', NULL,       3, 1, 1, NOW());

-- Grant all menus to super admin role
INSERT IGNORE INTO sys_role_menu (role_id, menu_id, create_time)
SELECT 1, id, NOW() FROM sys_menu;
```

- [ ] **Step 4: Create SystemServiceApplication**

```java
// mro-backend/system-service/src/main/java/com/mro/system/SystemServiceApplication.java
package com.mro.system;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
@EnableDubbo
public class SystemServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(SystemServiceApplication.class, args);
    }
}
```

- [ ] **Step 5: Verify compile**

```bash
cd mro-backend
mvn compile -pl system-service -am -DskipTests
```

Expected: `BUILD SUCCESS`

- [ ] **Step 6: Commit**

```bash
cd mro-backend
git add system-service/
git commit -m "chore(system): add system-service module, POM, config, Flyway migration

Refs: SYS-001, SYS-002, PLAT-002"
```

---

## Task 2: Dept Module

**Files:**
- Create: `mro-backend/system-service/src/main/java/com/mro/system/dept/entity/SysDept.java`
- Create: `mro-backend/system-service/src/main/java/com/mro/system/dept/mapper/SysDeptMapper.java`
- Create: `mro-backend/system-service/src/main/java/com/mro/system/dept/service/DeptService.java`
- Create: `mro-backend/system-service/src/main/java/com/mro/system/dept/service/DeptDubboServiceImpl.java`
- Test: `mro-backend/system-service/src/test/java/com/mro/system/dept/service/DeptServiceTest.java`

- [ ] **Step 1: Write failing tests**

```java
// mro-backend/system-service/src/test/java/com/mro/system/dept/service/DeptServiceTest.java
package com.mro.system.dept.service;

import com.mro.common.core.exception.BusinessException;
import com.mro.common.core.exception.ErrorCode;
import com.mro.common.dubbo.system.dto.CreateDeptCommand;
import com.mro.common.dubbo.system.dto.DeptDTO;
import com.mro.system.dept.entity.SysDept;
import com.mro.system.dept.mapper.SysDeptMapper;
import com.mro.system.user.mapper.SysUserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class DeptServiceTest {

    @Mock
    private SysDeptMapper deptMapper;

    @Mock
    private SysUserMapper userMapper;

    private DeptService deptService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        deptService = new DeptService(deptMapper, userMapper);
    }

    @Test
    void createDept_validCommand_returnsDeptId() {
        when(deptMapper.existsByDeptCode("HR")).thenReturn(false);
        SysDept parent = new SysDept();
        parent.setId(1L);
        parent.setAncestors("0");
        when(deptMapper.selectById(1L)).thenReturn(parent);
        when(deptMapper.insert(any())).thenReturn(1);

        CreateDeptCommand cmd = new CreateDeptCommand("人力资源", "HR", 1L, 1, "李总", null, null);

        // insert sets id via MyBatis-Plus, we stub the mapper to mimic ID=10
        doAnswer(inv -> {
            SysDept d = inv.getArgument(0);
            d.setId(10L);
            return 1;
        }).when(deptMapper).insert(any());

        Long id = deptService.createDept(cmd);
        assertThat(id).isEqualTo(10L);
    }

    @Test
    void createDept_duplicateCode_throwsException() {
        when(deptMapper.existsByDeptCode("HR")).thenReturn(true);

        CreateDeptCommand cmd = new CreateDeptCommand("人力资源", "HR", 0L, 1, null, null, null);
        assertThatThrownBy(() -> deptService.createDept(cmd))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getCode())
                        .isEqualTo(ErrorCode.DEPT_CODE_DUPLICATE.getCode()));
    }

    @Test
    void deleteDept_withChildren_throwsException() {
        when(deptMapper.countChildren(10L)).thenReturn(2L);

        assertThatThrownBy(() -> deptService.deleteDept(10L))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getCode())
                        .isEqualTo(ErrorCode.DEPT_HAS_CHILDREN.getCode()));
    }

    @Test
    void deleteDept_withUsers_throwsException() {
        when(deptMapper.countChildren(10L)).thenReturn(0L);
        when(userMapper.countByDeptId(10L)).thenReturn(3L);

        assertThatThrownBy(() -> deptService.deleteDept(10L))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getCode())
                        .isEqualTo(ErrorCode.DEPT_HAS_USERS.getCode()));
    }

    @Test
    void deleteDept_noChildrenNoUsers_succeeds() {
        when(deptMapper.countChildren(10L)).thenReturn(0L);
        when(userMapper.countByDeptId(10L)).thenReturn(0L);
        when(deptMapper.deleteById(10L)).thenReturn(1);

        assertThatCode(() -> deptService.deleteDept(10L)).doesNotThrowAnyException();
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
cd mro-backend
mvn test -pl system-service -am -Dtest=DeptServiceTest -DskipTests=false
```

Expected: FAIL — `DeptService cannot be resolved`

- [ ] **Step 3: Create SysDept entity**

```java
// mro-backend/system-service/src/main/java/com/mro/system/dept/entity/SysDept.java
package com.mro.system.dept.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_dept")
public class SysDept {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String deptName;
    private String deptCode;
    private Long parentId;
    private String ancestors;
    private Integer orderNum;
    private String leader;
    private String phone;
    private String email;
    private Integer status;

    @TableLogic
    private Integer isDeleted;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
```

- [ ] **Step 4: Create SysDeptMapper**

```java
// mro-backend/system-service/src/main/java/com/mro/system/dept/mapper/SysDeptMapper.java
package com.mro.system.dept.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mro.system.dept.entity.SysDept;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysDeptMapper extends BaseMapper<SysDept> {

    default boolean existsByDeptCode(String deptCode) {
        return selectCount(new LambdaQueryWrapper<SysDept>()
                .eq(SysDept::getDeptCode, deptCode)) > 0;
    }

    default boolean existsByDeptCodeAndIdNot(String deptCode, Long excludeId) {
        return selectCount(new LambdaQueryWrapper<SysDept>()
                .eq(SysDept::getDeptCode, deptCode)
                .ne(SysDept::getId, excludeId)) > 0;
    }

    default long countChildren(Long parentId) {
        return selectCount(new LambdaQueryWrapper<SysDept>()
                .eq(SysDept::getParentId, parentId));
    }

    default List<SysDept> selectAll(String deptName, Integer status) {
        LambdaQueryWrapper<SysDept> wrapper = new LambdaQueryWrapper<SysDept>()
                .like(deptName != null, SysDept::getDeptName, deptName)
                .eq(status != null, SysDept::getStatus, status)
                .orderByAsc(SysDept::getOrderNum);
        return selectList(wrapper);
    }

    @Select("SELECT id FROM sys_dept WHERE FIND_IN_SET(#{deptId}, ancestors) OR id = #{deptId}")
    List<Long> selectChildrenIds(Long deptId);
}
```

- [ ] **Step 5: Create SysUserMapper stub (needed by DeptService)**

```java
// mro-backend/system-service/src/main/java/com/mro/system/user/mapper/SysUserMapper.java
package com.mro.system.user.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mro.system.user.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    default long countByDeptId(Long deptId) {
        return selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getDeptId, deptId)
                .eq(SysUser::getStatus, 1));
    }

    default SysUser findByUsername(String username) {
        return selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username));
    }
}
```

- [ ] **Step 6: Create SysUser entity stub**

```java
// mro-backend/system-service/src/main/java/com/mro/system/user/entity/SysUser.java
package com.mro.system.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_user")
public class SysUser {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String password;
    private String employeeNo;
    private String realName;
    private Long deptId;
    private String phone;
    private String email;
    private String avatar;
    private Integer status;

    @TableLogic
    private Integer isDeleted;

    private Long createUserId;
    private Long createDeptId;
    private LocalDateTime createTime;
    private Long updateUserId;
    private LocalDateTime updateTime;
}
```

- [ ] **Step 7: Implement DeptService**

```java
// mro-backend/system-service/src/main/java/com/mro/system/dept/service/DeptService.java
package com.mro.system.dept.service;

import com.mro.common.core.exception.BusinessException;
import com.mro.common.core.exception.ErrorCode;
import com.mro.common.dubbo.system.dto.CreateDeptCommand;
import com.mro.common.dubbo.system.dto.DeptDTO;
import com.mro.common.dubbo.system.dto.UpdateDeptCommand;
import com.mro.system.dept.entity.SysDept;
import com.mro.system.dept.mapper.SysDeptMapper;
import com.mro.system.user.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeptService {

    private final SysDeptMapper deptMapper;
    private final SysUserMapper userMapper;

    @Transactional
    public Long createDept(CreateDeptCommand cmd) {
        if (deptMapper.existsByDeptCode(cmd.deptCode())) {
            throw new BusinessException(ErrorCode.DEPT_CODE_DUPLICATE);
        }

        SysDept dept = new SysDept();
        dept.setDeptName(cmd.deptName());
        dept.setDeptCode(cmd.deptCode());
        dept.setParentId(cmd.parentId() == null ? 0L : cmd.parentId());
        dept.setOrderNum(cmd.orderNum() == null ? 0 : cmd.orderNum());
        dept.setLeader(cmd.leader());
        dept.setPhone(cmd.phone());
        dept.setEmail(cmd.email());
        dept.setStatus(1);

        // Build ancestors
        if (dept.getParentId() == 0L) {
            dept.setAncestors("0");
        } else {
            SysDept parent = deptMapper.selectById(dept.getParentId());
            if (parent == null) {
                throw new BusinessException(ErrorCode.DEPT_NOT_FOUND);
            }
            dept.setAncestors(parent.getAncestors() + "," + parent.getId());
        }

        deptMapper.insert(dept);
        return dept.getId();
    }

    @Transactional
    public void updateDept(UpdateDeptCommand cmd) {
        SysDept existing = requireDept(cmd.id());
        if (deptMapper.existsByDeptCodeAndIdNot(cmd.deptCode(), cmd.id())) {
            throw new BusinessException(ErrorCode.DEPT_CODE_DUPLICATE);
        }
        existing.setDeptName(cmd.deptName());
        existing.setDeptCode(cmd.deptCode());
        existing.setParentId(cmd.parentId() == null ? 0L : cmd.parentId());
        existing.setOrderNum(cmd.orderNum() == null ? 0 : cmd.orderNum());
        existing.setLeader(cmd.leader());
        existing.setPhone(cmd.phone());
        existing.setEmail(cmd.email());
        deptMapper.updateById(existing);
    }

    @Transactional
    public void deleteDept(Long deptId) {
        if (deptMapper.countChildren(deptId) > 0) {
            throw new BusinessException(ErrorCode.DEPT_HAS_CHILDREN);
        }
        if (userMapper.countByDeptId(deptId) > 0) {
            throw new BusinessException(ErrorCode.DEPT_HAS_USERS);
        }
        deptMapper.deleteById(deptId);
    }

    public void enableDept(Long deptId) {
        SysDept dept = requireDept(deptId);
        dept.setStatus(1);
        deptMapper.updateById(dept);
    }

    public void disableDept(Long deptId) {
        SysDept dept = requireDept(deptId);
        dept.setStatus(0);
        deptMapper.updateById(dept);
    }

    public DeptDTO getById(Long deptId) {
        return toDTO(requireDept(deptId), List.of());
    }

    public List<DeptDTO> listTree(String deptName, Integer status) {
        List<SysDept> all = deptMapper.selectAll(deptName, status);
        return buildTree(all, 0L);
    }

    public List<Long> listChildrenIds(Long deptId) {
        return deptMapper.selectChildrenIds(deptId);
    }

    private List<DeptDTO> buildTree(List<SysDept> all, Long parentId) {
        Map<Long, List<SysDept>> byParent = all.stream()
                .collect(Collectors.groupingBy(SysDept::getParentId));
        List<SysDept> children = byParent.getOrDefault(parentId, List.of());
        return children.stream()
                .map(d -> toDTO(d, buildTree(all, d.getId())))
                .collect(Collectors.toList());
    }

    private DeptDTO toDTO(SysDept d, List<DeptDTO> children) {
        return new DeptDTO(d.getId(), d.getDeptName(), d.getDeptCode(), d.getParentId(),
                d.getAncestors(), d.getOrderNum(), d.getLeader(), d.getPhone(), d.getEmail(),
                d.getStatus(), children);
    }

    private SysDept requireDept(Long deptId) {
        SysDept dept = deptMapper.selectById(deptId);
        if (dept == null) {
            throw new BusinessException(ErrorCode.DEPT_NOT_FOUND);
        }
        return dept;
    }
}
```

- [ ] **Step 8: Create DeptDubboServiceImpl**

```java
// mro-backend/system-service/src/main/java/com/mro/system/dept/service/DeptDubboServiceImpl.java
package com.mro.system.dept.service;

import com.mro.common.dubbo.system.DeptDubboService;
import com.mro.common.dubbo.system.dto.CreateDeptCommand;
import com.mro.common.dubbo.system.dto.DeptDTO;
import com.mro.common.dubbo.system.dto.UpdateDeptCommand;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;

import java.util.List;

@DubboService(version = "1.0.0", group = "mro")
@RequiredArgsConstructor
public class DeptDubboServiceImpl implements DeptDubboService {

    private final DeptService deptService;

    @Override
    public Long createDept(CreateDeptCommand cmd) {
        return deptService.createDept(cmd);
    }

    @Override
    public void updateDept(UpdateDeptCommand cmd) {
        deptService.updateDept(cmd);
    }

    @Override
    public void deleteDept(Long deptId) {
        deptService.deleteDept(deptId);
    }

    @Override
    public void enableDept(Long deptId) {
        deptService.enableDept(deptId);
    }

    @Override
    public void disableDept(Long deptId) {
        deptService.disableDept(deptId);
    }

    @Override
    public DeptDTO getById(Long deptId) {
        return deptService.getById(deptId);
    }

    @Override
    public List<DeptDTO> listTree(String deptName, Integer status) {
        return deptService.listTree(deptName, status);
    }

    @Override
    public List<Long> listChildrenIds(Long deptId) {
        return deptService.listChildrenIds(deptId);
    }
}
```

- [ ] **Step 9: Run tests**

```bash
cd mro-backend
mvn test -pl system-service -am -Dtest=DeptServiceTest -DskipTests=false
```

Expected: `Tests run: 5, Failures: 0, Errors: 0`

- [ ] **Step 10: Commit**

```bash
cd mro-backend
git add system-service/src/
git commit -m "feat(system): add dept module — DeptService, DeptDubboServiceImpl, SysDept

Refs: SYS-001"
```

---

## Task 3: User Module

**Files:**
- Create: `mro-backend/system-service/src/main/java/com/mro/system/user/service/UserService.java`
- Create: `mro-backend/system-service/src/main/java/com/mro/system/user/service/UserDubboServiceImpl.java`
- Test: `mro-backend/system-service/src/test/java/com/mro/system/user/service/UserServiceTest.java`

- [ ] **Step 1: Write failing tests**

```java
// mro-backend/system-service/src/test/java/com/mro/system/user/service/UserServiceTest.java
package com.mro.system.user.service;

import com.mro.common.core.exception.BusinessException;
import com.mro.common.core.exception.ErrorCode;
import com.mro.common.dubbo.system.dto.CreateUserCommand;
import com.mro.common.dubbo.system.dto.UserDTO;
import com.mro.system.user.entity.SysUser;
import com.mro.system.user.mapper.SysUserMapper;
import com.mro.system.role.mapper.SysUserRoleMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private SysUserMapper userMapper;

    @Mock
    private SysUserRoleMapper userRoleMapper;

    private UserService userService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        userService = new UserService(userMapper, userRoleMapper, new BCryptPasswordEncoder());
    }

    @Test
    void createUser_uniqueUsername_returnsId() {
        when(userMapper.existsByUsername("newuser")).thenReturn(false);
        when(userMapper.existsByPhone(null)).thenReturn(false);
        when(userMapper.existsByEmployeeNo(null)).thenReturn(false);

        doAnswer(inv -> {
            SysUser u = inv.getArgument(0);
            u.setId(5L);
            return 1;
        }).when(userMapper).insert(any());

        CreateUserCommand cmd = new CreateUserCommand("newuser", "Pass@123", null,
                "新用户", 1L, null, null, List.of());

        Long id = userService.createUser(cmd);
        assertThat(id).isEqualTo(5L);
    }

    @Test
    void createUser_duplicateUsername_throwsException() {
        when(userMapper.existsByUsername("admin")).thenReturn(true);

        CreateUserCommand cmd = new CreateUserCommand("admin", "Pass@123", null,
                "管理员", 1L, null, null, List.of());

        assertThatThrownBy(() -> userService.createUser(cmd))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getCode())
                        .isEqualTo(ErrorCode.USERNAME_DUPLICATE.getCode()));
    }

    @Test
    void getById_userExists_returnsDTO() {
        SysUser user = new SysUser();
        user.setId(1L);
        user.setUsername("admin");
        user.setRealName("管理员");
        user.setDeptId(1L);
        user.setStatus(1);
        when(userMapper.selectById(1L)).thenReturn(user);
        when(userRoleMapper.findRoleKeysByUserId(1L)).thenReturn(List.of("admin"));

        UserDTO dto = userService.getById(1L);

        assertThat(dto.username()).isEqualTo("admin");
        assertThat(dto.roles()).containsExactly("admin");
    }

    @Test
    void getById_notFound_throwsException() {
        when(userMapper.selectById(99L)).thenReturn(null);

        assertThatThrownBy(() -> userService.getById(99L))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getCode())
                        .isEqualTo(ErrorCode.USER_NOT_FOUND.getCode()));
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
cd mro-backend
mvn test -pl system-service -am -Dtest=UserServiceTest -DskipTests=false
```

Expected: FAIL — `UserService cannot be resolved`

- [ ] **Step 3: Create SysRole, SysUserRole entities and mappers (needed by UserService)**

```java
// mro-backend/system-service/src/main/java/com/mro/system/role/entity/SysRole.java
package com.mro.system.role.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_role")
public class SysRole {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String roleName;
    private String roleKey;
    private Integer dataScope;
    private Integer orderNum;
    private Integer status;

    @TableLogic
    private Integer isDeleted;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
```

```java
// mro-backend/system-service/src/main/java/com/mro/system/role/entity/SysUserRole.java
package com.mro.system.role.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_user_role")
public class SysUserRole {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long roleId;
    private LocalDateTime createTime;
}
```

```java
// mro-backend/system-service/src/main/java/com/mro/system/role/mapper/SysUserRoleMapper.java
package com.mro.system.role.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mro.system.role.entity.SysUserRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {

    default List<Long> findRoleIdsByUserId(Long userId) {
        return selectList(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, userId))
                .stream().map(SysUserRole::getRoleId).toList();
    }

    @Select("SELECT r.role_key FROM sys_role r " +
            "INNER JOIN sys_user_role ur ON r.id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND r.is_deleted = 0")
    List<String> findRoleKeysByUserId(Long userId);

    default void deleteByUserId(Long userId) {
        delete(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, userId));
    }
}
```

- [ ] **Step 4: Extend SysUserMapper**

```java
// mro-backend/system-service/src/main/java/com/mro/system/user/mapper/SysUserMapper.java
package com.mro.system.user.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mro.system.user.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    default long countByDeptId(Long deptId) {
        return selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getDeptId, deptId)
                .eq(SysUser::getStatus, 1));
    }

    default SysUser findByUsername(String username) {
        return selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username));
    }

    default boolean existsByUsername(String username) {
        return selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username)) > 0;
    }

    default boolean existsByUsernameAndIdNot(String username, Long excludeId) {
        return selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username)
                .ne(SysUser::getId, excludeId)) > 0;
    }

    default boolean existsByPhone(String phone) {
        return phone != null && selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getPhone, phone)) > 0;
    }

    default boolean existsByPhoneAndIdNot(String phone, Long excludeId) {
        return phone != null && selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getPhone, phone)
                .ne(SysUser::getId, excludeId)) > 0;
    }

    default boolean existsByEmployeeNo(String employeeNo) {
        return employeeNo != null && selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getEmployeeNo, employeeNo)) > 0;
    }

    default boolean existsByEmployeeNoAndIdNot(String employeeNo, Long excludeId) {
        return employeeNo != null && selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getEmployeeNo, employeeNo)
                .ne(SysUser::getId, excludeId)) > 0;
    }

    default IPage<SysUser> pageUsers(Page<SysUser> page, String username,
                                     String realName, Long deptId, Integer status) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<SysUser>()
                .like(username != null, SysUser::getUsername, username)
                .like(realName != null, SysUser::getRealName, realName)
                .eq(deptId != null, SysUser::getDeptId, deptId)
                .eq(status != null, SysUser::getStatus, status)
                .orderByDesc(SysUser::getCreateTime);
        return selectPage(page, wrapper);
    }
}
```

- [ ] **Step 5: Implement UserService**

```java
// mro-backend/system-service/src/main/java/com/mro/system/user/service/UserService.java
package com.mro.system.user.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mro.common.core.exception.BusinessException;
import com.mro.common.core.exception.ErrorCode;
import com.mro.common.core.result.PageResult;
import com.mro.common.dubbo.system.dto.*;
import com.mro.system.role.entity.SysUserRole;
import com.mro.system.role.mapper.SysUserRoleMapper;
import com.mro.system.user.entity.SysUser;
import com.mro.system.user.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final SysUserMapper userMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Long createUser(CreateUserCommand cmd) {
        if (userMapper.existsByUsername(cmd.username())) {
            throw new BusinessException(ErrorCode.USERNAME_DUPLICATE);
        }
        if (userMapper.existsByPhone(cmd.phone())) {
            throw new BusinessException(ErrorCode.PHONE_DUPLICATE);
        }
        if (userMapper.existsByEmployeeNo(cmd.employeeNo())) {
            throw new BusinessException(ErrorCode.EMPLOYEE_NO_DUPLICATE);
        }

        SysUser user = new SysUser();
        user.setUsername(cmd.username());
        user.setPassword(passwordEncoder.encode(cmd.password()));
        user.setEmployeeNo(cmd.employeeNo());
        user.setRealName(cmd.realName());
        user.setDeptId(cmd.deptId());
        user.setPhone(cmd.phone());
        user.setEmail(cmd.email());
        user.setStatus(1);
        userMapper.insert(user);

        assignRoles(user.getId(), cmd.roleIds());
        return user.getId();
    }

    @Transactional
    public void updateUser(UpdateUserCommand cmd) {
        SysUser user = requireUser(cmd.id());
        if (userMapper.existsByPhoneAndIdNot(cmd.phone(), cmd.id())) {
            throw new BusinessException(ErrorCode.PHONE_DUPLICATE);
        }
        if (userMapper.existsByEmployeeNoAndIdNot(cmd.employeeNo(), cmd.id())) {
            throw new BusinessException(ErrorCode.EMPLOYEE_NO_DUPLICATE);
        }

        user.setEmployeeNo(cmd.employeeNo());
        user.setRealName(cmd.realName());
        user.setDeptId(cmd.deptId());
        user.setPhone(cmd.phone());
        user.setEmail(cmd.email());
        if (cmd.avatar() != null) {
            user.setAvatar(cmd.avatar());
        }
        userMapper.updateById(user);

        if (cmd.roleIds() != null) {
            userRoleMapper.deleteByUserId(cmd.id());
            assignRoles(cmd.id(), cmd.roleIds());
        }
    }

    @Transactional
    public void deleteUser(Long userId) {
        requireUser(userId);
        userRoleMapper.deleteByUserId(userId);
        userMapper.deleteById(userId);
    }

    public void enableUser(Long userId) {
        SysUser user = requireUser(userId);
        user.setStatus(1);
        userMapper.updateById(user);
    }

    public void disableUser(Long userId) {
        SysUser user = requireUser(userId);
        user.setStatus(0);
        userMapper.updateById(user);
    }

    public void resetPassword(Long userId, String newPassword) {
        SysUser user = requireUser(userId);
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
    }

    public UserDTO getById(Long userId) {
        SysUser user = requireUser(userId);
        List<String> roles = userRoleMapper.findRoleKeysByUserId(userId);
        return toDTO(user, roles);
    }

    public UserDTO getByUsername(String username) {
        SysUser user = userMapper.findByUsername(username);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        List<String> roles = userRoleMapper.findRoleKeysByUserId(user.getId());
        return toDTO(user, roles);
    }

    public boolean verifyPassword(Long userId, String rawPassword) {
        SysUser user = requireUser(userId);
        return passwordEncoder.matches(rawPassword, user.getPassword());
    }

    public PageResult<UserDTO> listUsers(UserQueryParam param) {
        Page<SysUser> page = new Page<>(param.pageNum(), param.pageSize());
        var result = userMapper.pageUsers(page, param.username(), param.realName(),
                param.deptId(), param.status());
        List<UserDTO> list = result.getRecords().stream()
                .map(u -> toDTO(u, userRoleMapper.findRoleKeysByUserId(u.getId())))
                .collect(Collectors.toList());
        return PageResult.of(list, result.getTotal(), param.pageNum(), param.pageSize());
    }

    private void assignRoles(Long userId, List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) return;
        roleIds.forEach(roleId -> {
            SysUserRole ur = new SysUserRole();
            ur.setUserId(userId);
            ur.setRoleId(roleId);
            userRoleMapper.insert(ur);
        });
    }

    private UserDTO toDTO(SysUser user, List<String> roles) {
        return new UserDTO(user.getId(), user.getUsername(), user.getEmployeeNo(),
                user.getRealName(), user.getDeptId(), null, user.getPhone(),
                user.getEmail(), user.getAvatar(), user.getStatus(), roles, user.getCreateTime());
    }

    private SysUser requireUser(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        return user;
    }
}
```

- [ ] **Step 6: Create UserDubboServiceImpl**

```java
// mro-backend/system-service/src/main/java/com/mro/system/user/service/UserDubboServiceImpl.java
package com.mro.system.user.service;

import com.mro.common.core.result.PageResult;
import com.mro.common.dubbo.system.UserDubboService;
import com.mro.common.dubbo.system.dto.*;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;

@DubboService(version = "1.0.0", group = "mro")
@RequiredArgsConstructor
public class UserDubboServiceImpl implements UserDubboService {

    private final UserService userService;

    @Override
    public Long createUser(CreateUserCommand cmd) { return userService.createUser(cmd); }

    @Override
    public void updateUser(UpdateUserCommand cmd) { userService.updateUser(cmd); }

    @Override
    public void deleteUser(Long userId) { userService.deleteUser(userId); }

    @Override
    public void enableUser(Long userId) { userService.enableUser(userId); }

    @Override
    public void disableUser(Long userId) { userService.disableUser(userId); }

    @Override
    public void resetPassword(Long userId, String newPassword) {
        userService.resetPassword(userId, newPassword);
    }

    @Override
    public UserDTO getById(Long userId) { return userService.getById(userId); }

    @Override
    public PageResult<UserDTO> listUsers(UserQueryParam param) {
        return userService.listUsers(param);
    }

    @Override
    public UserDTO getByUsername(String username) { return userService.getByUsername(username); }

    @Override
    public boolean verifyPassword(Long userId, String rawPassword) {
        return userService.verifyPassword(userId, rawPassword);
    }
}
```

- [ ] **Step 7: Run tests**

```bash
cd mro-backend
mvn test -pl system-service -am -Dtest=UserServiceTest -DskipTests=false
```

Expected: `Tests run: 4, Failures: 0, Errors: 0`

- [ ] **Step 8: Commit**

```bash
cd mro-backend
git add system-service/src/
git commit -m "feat(system): add user module — UserService, UserDubboServiceImpl, SysUser

Refs: SYS-002"
```

---

## Task 4: Role Module

**Files:**
- Create: `mro-backend/system-service/src/main/java/com/mro/system/role/entity/SysRoleMenu.java`
- Create: `mro-backend/system-service/src/main/java/com/mro/system/role/entity/SysRoleDept.java`
- Create: `mro-backend/system-service/src/main/java/com/mro/system/role/mapper/SysRoleMapper.java`
- Create: `mro-backend/system-service/src/main/java/com/mro/system/role/mapper/SysRoleMenuMapper.java`
- Create: `mro-backend/system-service/src/main/java/com/mro/system/role/mapper/SysRoleDeptMapper.java`
- Create: `mro-backend/system-service/src/main/java/com/mro/system/role/service/RoleService.java`
- Create: `mro-backend/system-service/src/main/java/com/mro/system/role/service/RoleDubboServiceImpl.java`
- Test: `mro-backend/system-service/src/test/java/com/mro/system/role/service/RoleServiceTest.java`

- [ ] **Step 1: Write failing tests**

```java
// mro-backend/system-service/src/test/java/com/mro/system/role/service/RoleServiceTest.java
package com.mro.system.role.service;

import com.mro.common.core.exception.BusinessException;
import com.mro.common.core.exception.ErrorCode;
import com.mro.common.dubbo.system.dto.CreateRoleCommand;
import com.mro.system.role.entity.SysRole;
import com.mro.system.role.mapper.SysRoleDeptMapper;
import com.mro.system.role.mapper.SysRoleMapper;
import com.mro.system.role.mapper.SysRoleMenuMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class RoleServiceTest {

    @Mock
    private SysRoleMapper roleMapper;
    @Mock
    private SysRoleMenuMapper roleMenuMapper;
    @Mock
    private SysRoleDeptMapper roleDeptMapper;

    private RoleService roleService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        roleService = new RoleService(roleMapper, roleMenuMapper, roleDeptMapper);
    }

    @Test
    void createRole_uniqueKey_returnsId() {
        when(roleMapper.existsByRoleKey("manager")).thenReturn(false);
        doAnswer(inv -> {
            SysRole r = inv.getArgument(0);
            r.setId(3L);
            return 1;
        }).when(roleMapper).insert(any());

        CreateRoleCommand cmd = new CreateRoleCommand("项目经理", "manager", 1, 1, List.of(), List.of());
        Long id = roleService.createRole(cmd);

        assertThat(id).isEqualTo(3L);
    }

    @Test
    void createRole_duplicateKey_throwsException() {
        when(roleMapper.existsByRoleKey("admin")).thenReturn(true);

        CreateRoleCommand cmd = new CreateRoleCommand("超管", "admin", 1, 0, List.of(), List.of());
        assertThatThrownBy(() -> roleService.createRole(cmd))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getCode())
                        .isEqualTo(ErrorCode.ROLE_KEY_DUPLICATE.getCode()));
    }

    @Test
    void getById_notFound_throwsException() {
        when(roleMapper.selectById(999L)).thenReturn(null);

        assertThatThrownBy(() -> roleService.getById(999L))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getCode())
                        .isEqualTo(ErrorCode.ROLE_NOT_FOUND.getCode()));
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
cd mro-backend
mvn test -pl system-service -am -Dtest=RoleServiceTest -DskipTests=false
```

Expected: FAIL — `RoleService cannot be resolved`

- [ ] **Step 3: Create remaining role entities and mappers**

```java
// mro-backend/system-service/src/main/java/com/mro/system/role/entity/SysRoleMenu.java
package com.mro.system.role.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_role_menu")
public class SysRoleMenu {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long roleId;
    private Long menuId;
    private LocalDateTime createTime;
}
```

```java
// mro-backend/system-service/src/main/java/com/mro/system/role/entity/SysRoleDept.java
package com.mro.system.role.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_role_dept")
public class SysRoleDept {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long roleId;
    private Long deptId;
    private LocalDateTime createTime;
}
```

```java
// mro-backend/system-service/src/main/java/com/mro/system/role/mapper/SysRoleMapper.java
package com.mro.system.role.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mro.system.role.entity.SysRole;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {

    default boolean existsByRoleKey(String roleKey) {
        return selectCount(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getRoleKey, roleKey)) > 0;
    }

    default boolean existsByRoleKeyAndIdNot(String roleKey, Long excludeId) {
        return selectCount(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getRoleKey, roleKey)
                .ne(SysRole::getId, excludeId)) > 0;
    }

    default IPage<SysRole> pageRoles(Page<SysRole> page, String roleName, Integer status) {
        return selectPage(page, new LambdaQueryWrapper<SysRole>()
                .like(roleName != null, SysRole::getRoleName, roleName)
                .eq(status != null, SysRole::getStatus, status)
                .orderByAsc(SysRole::getOrderNum));
    }

    default List<SysRole> findByIds(List<Long> ids) {
        return selectList(new LambdaQueryWrapper<SysRole>()
                .in(SysRole::getId, ids));
    }
}
```

```java
// mro-backend/system-service/src/main/java/com/mro/system/role/mapper/SysRoleMenuMapper.java
package com.mro.system.role.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mro.system.role.entity.SysRoleMenu;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SysRoleMenuMapper extends BaseMapper<SysRoleMenu> {

    default List<Long> findMenuIdsByRoleId(Long roleId) {
        return selectList(new LambdaQueryWrapper<SysRoleMenu>()
                .eq(SysRoleMenu::getRoleId, roleId))
                .stream().map(SysRoleMenu::getMenuId).toList();
    }

    default void deleteByRoleId(Long roleId) {
        delete(new LambdaQueryWrapper<SysRoleMenu>()
                .eq(SysRoleMenu::getRoleId, roleId));
    }
}
```

```java
// mro-backend/system-service/src/main/java/com/mro/system/role/mapper/SysRoleDeptMapper.java
package com.mro.system.role.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mro.system.role.entity.SysRoleDept;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SysRoleDeptMapper extends BaseMapper<SysRoleDept> {

    default List<Long> findDeptIdsByRoleId(Long roleId) {
        return selectList(new LambdaQueryWrapper<SysRoleDept>()
                .eq(SysRoleDept::getRoleId, roleId))
                .stream().map(SysRoleDept::getDeptId).toList();
    }

    default void deleteByRoleId(Long roleId) {
        delete(new LambdaQueryWrapper<SysRoleDept>()
                .eq(SysRoleDept::getRoleId, roleId));
    }
}
```

- [ ] **Step 4: Implement RoleService**

```java
// mro-backend/system-service/src/main/java/com/mro/system/role/service/RoleService.java
package com.mro.system.role.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mro.common.core.exception.BusinessException;
import com.mro.common.core.exception.ErrorCode;
import com.mro.common.core.result.PageResult;
import com.mro.common.dubbo.system.dto.CreateRoleCommand;
import com.mro.common.dubbo.system.dto.RoleDTO;
import com.mro.common.dubbo.system.dto.UpdateRoleCommand;
import com.mro.system.role.entity.SysRole;
import com.mro.system.role.entity.SysRoleDept;
import com.mro.system.role.entity.SysRoleMenu;
import com.mro.system.role.mapper.SysRoleDeptMapper;
import com.mro.system.role.mapper.SysRoleMapper;
import com.mro.system.role.mapper.SysRoleMenuMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final SysRoleMapper roleMapper;
    private final SysRoleMenuMapper roleMenuMapper;
    private final SysRoleDeptMapper roleDeptMapper;

    @Transactional
    public Long createRole(CreateRoleCommand cmd) {
        if (roleMapper.existsByRoleKey(cmd.roleKey())) {
            throw new BusinessException(ErrorCode.ROLE_KEY_DUPLICATE);
        }
        SysRole role = new SysRole();
        role.setRoleName(cmd.roleName());
        role.setRoleKey(cmd.roleKey());
        role.setDataScope(cmd.dataScope() == null ? 1 : cmd.dataScope());
        role.setOrderNum(cmd.orderNum() == null ? 0 : cmd.orderNum());
        role.setStatus(1);
        roleMapper.insert(role);

        assignMenus(role.getId(), cmd.menuIds());
        assignDepts(role.getId(), cmd.dataScope(), cmd.dataScopeDeptIds());
        return role.getId();
    }

    @Transactional
    public void updateRole(UpdateRoleCommand cmd) {
        SysRole role = requireRole(cmd.id());
        if (roleMapper.existsByRoleKeyAndIdNot(cmd.roleKey(), cmd.id())) {
            throw new BusinessException(ErrorCode.ROLE_KEY_DUPLICATE);
        }
        role.setRoleName(cmd.roleName());
        role.setRoleKey(cmd.roleKey());
        role.setDataScope(cmd.dataScope() == null ? 1 : cmd.dataScope());
        role.setOrderNum(cmd.orderNum() == null ? 0 : cmd.orderNum());
        roleMapper.updateById(role);

        roleMenuMapper.deleteByRoleId(cmd.id());
        assignMenus(cmd.id(), cmd.menuIds());
        roleDeptMapper.deleteByRoleId(cmd.id());
        assignDepts(cmd.id(), cmd.dataScope(), cmd.dataScopeDeptIds());
    }

    @Transactional
    public void deleteRole(Long roleId) {
        requireRole(roleId);
        roleMenuMapper.deleteByRoleId(roleId);
        roleDeptMapper.deleteByRoleId(roleId);
        roleMapper.deleteById(roleId);
    }

    public void enableRole(Long roleId) {
        SysRole role = requireRole(roleId);
        role.setStatus(1);
        roleMapper.updateById(role);
    }

    public void disableRole(Long roleId) {
        SysRole role = requireRole(roleId);
        role.setStatus(0);
        roleMapper.updateById(role);
    }

    public RoleDTO getById(Long roleId) {
        return toDTO(requireRole(roleId));
    }

    public PageResult<RoleDTO> listRoles(int pageNum, int pageSize, String roleName, Integer status) {
        Page<SysRole> page = new Page<>(pageNum, pageSize);
        var result = roleMapper.pageRoles(page, roleName, status);
        List<RoleDTO> list = result.getRecords().stream().map(this::toDTO).collect(Collectors.toList());
        return PageResult.of(list, result.getTotal(), pageNum, pageSize);
    }

    public List<RoleDTO> listAll() {
        return roleMapper.selectList(null).stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<Long> listMenuIdsByRoleId(Long roleId) {
        return roleMenuMapper.findMenuIdsByRoleId(roleId);
    }

    private void assignMenus(Long roleId, List<Long> menuIds) {
        if (menuIds == null || menuIds.isEmpty()) return;
        menuIds.forEach(menuId -> {
            SysRoleMenu rm = new SysRoleMenu();
            rm.setRoleId(roleId);
            rm.setMenuId(menuId);
            roleMenuMapper.insert(rm);
        });
    }

    private void assignDepts(Long roleId, Integer dataScope, List<Long> deptIds) {
        if (dataScope == null || dataScope != 5 || deptIds == null) return;
        deptIds.forEach(deptId -> {
            SysRoleDept rd = new SysRoleDept();
            rd.setRoleId(roleId);
            rd.setDeptId(deptId);
            roleDeptMapper.insert(rd);
        });
    }

    private RoleDTO toDTO(SysRole role) {
        return new RoleDTO(role.getId(), role.getRoleName(), role.getRoleKey(),
                role.getDataScope(), role.getOrderNum(), role.getStatus());
    }

    private SysRole requireRole(Long roleId) {
        SysRole role = roleMapper.selectById(roleId);
        if (role == null) {
            throw new BusinessException(ErrorCode.ROLE_NOT_FOUND);
        }
        return role;
    }
}
```

- [ ] **Step 5: Create RoleDubboServiceImpl**

```java
// mro-backend/system-service/src/main/java/com/mro/system/role/service/RoleDubboServiceImpl.java
package com.mro.system.role.service;

import com.mro.common.core.result.PageResult;
import com.mro.common.dubbo.system.RoleDubboService;
import com.mro.common.dubbo.system.dto.CreateRoleCommand;
import com.mro.common.dubbo.system.dto.RoleDTO;
import com.mro.common.dubbo.system.dto.UpdateRoleCommand;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;

import java.util.List;

@DubboService(version = "1.0.0", group = "mro")
@RequiredArgsConstructor
public class RoleDubboServiceImpl implements RoleDubboService {

    private final RoleService roleService;

    @Override
    public Long createRole(CreateRoleCommand cmd) { return roleService.createRole(cmd); }

    @Override
    public void updateRole(UpdateRoleCommand cmd) { roleService.updateRole(cmd); }

    @Override
    public void deleteRole(Long roleId) { roleService.deleteRole(roleId); }

    @Override
    public void enableRole(Long roleId) { roleService.enableRole(roleId); }

    @Override
    public void disableRole(Long roleId) { roleService.disableRole(roleId); }

    @Override
    public RoleDTO getById(Long roleId) { return roleService.getById(roleId); }

    @Override
    public PageResult<RoleDTO> listRoles(int pageNum, int pageSize, String roleName, Integer status) {
        return roleService.listRoles(pageNum, pageSize, roleName, status);
    }

    @Override
    public List<RoleDTO> listAll() { return roleService.listAll(); }

    @Override
    public List<Long> listMenuIdsByRoleId(Long roleId) {
        return roleService.listMenuIdsByRoleId(roleId);
    }
}
```

- [ ] **Step 6: Run tests**

```bash
cd mro-backend
mvn test -pl system-service -am -Dtest=RoleServiceTest -DskipTests=false
```

Expected: `Tests run: 3, Failures: 0, Errors: 0`

- [ ] **Step 7: Commit**

```bash
cd mro-backend
git add system-service/src/
git commit -m "feat(system): add role module — RoleService, RoleDubboServiceImpl

Refs: SYS-003"
```

---

## Task 5: Menu + Dict Modules

**Files:**
- Create: `mro-backend/system-service/src/main/java/com/mro/system/menu/entity/SysMenu.java`
- Create: `mro-backend/system-service/src/main/java/com/mro/system/menu/mapper/SysMenuMapper.java`
- Create: `mro-backend/system-service/src/main/java/com/mro/system/menu/service/MenuService.java`
- Create: `mro-backend/system-service/src/main/java/com/mro/system/menu/service/MenuDubboServiceImpl.java`
- Create: `mro-backend/system-service/src/main/java/com/mro/system/dict/entity/SysDictType.java`
- Create: `mro-backend/system-service/src/main/java/com/mro/system/dict/entity/SysDictData.java`
- Create: `mro-backend/system-service/src/main/java/com/mro/system/dict/mapper/SysDictTypeMapper.java`
- Create: `mro-backend/system-service/src/main/java/com/mro/system/dict/mapper/SysDictDataMapper.java`
- Create: `mro-backend/system-service/src/main/java/com/mro/system/dict/service/DictService.java`
- Create: `mro-backend/system-service/src/main/java/com/mro/system/dict/service/DictDubboServiceImpl.java`

- [ ] **Step 1: Create Menu entity and mapper**

```java
// mro-backend/system-service/src/main/java/com/mro/system/menu/entity/SysMenu.java
package com.mro.system.menu.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_menu")
public class SysMenu {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long parentId;
    private String menuName;
    private String menuType;
    private String path;
    private String component;
    private String perms;
    private String icon;
    private Integer orderNum;
    private Integer visible;
    private Integer status;

    @TableLogic
    private Integer isDeleted;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
```

```java
// mro-backend/system-service/src/main/java/com/mro/system/menu/mapper/SysMenuMapper.java
package com.mro.system.menu.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mro.system.menu.entity.SysMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysMenuMapper extends BaseMapper<SysMenu> {

    default List<SysMenu> selectAll(String menuName, Integer status) {
        return selectList(new LambdaQueryWrapper<SysMenu>()
                .like(menuName != null, SysMenu::getMenuName, menuName)
                .eq(status != null, SysMenu::getStatus, status)
                .orderByAsc(SysMenu::getOrderNum));
    }

    default long countChildren(Long parentId) {
        return selectCount(new LambdaQueryWrapper<SysMenu>()
                .eq(SysMenu::getParentId, parentId));
    }

    @Select("SELECT DISTINCT m.* FROM sys_menu m " +
            "INNER JOIN sys_role_menu rm ON m.id = rm.menu_id " +
            "WHERE rm.role_id IN (${roleIds}) AND m.is_deleted = 0 " +
            "ORDER BY m.order_num")
    List<SysMenu> findByRoleIds(String roleIds);

    @Select("SELECT DISTINCT m.perms FROM sys_menu m " +
            "INNER JOIN sys_role_menu rm ON m.id = rm.menu_id " +
            "WHERE rm.role_id IN (${roleIds}) AND m.menu_type = 'F' " +
            "AND m.is_deleted = 0 AND m.perms IS NOT NULL")
    List<String> findPermsByRoleIds(String roleIds);
}
```

- [ ] **Step 2: Implement MenuService**

```java
// mro-backend/system-service/src/main/java/com/mro/system/menu/service/MenuService.java
package com.mro.system.menu.service;

import com.mro.common.core.exception.BusinessException;
import com.mro.common.core.exception.ErrorCode;
import com.mro.common.dubbo.system.dto.MenuDTO;
import com.mro.system.menu.entity.SysMenu;
import com.mro.system.menu.mapper.SysMenuMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final SysMenuMapper menuMapper;

    @Transactional
    public Long createMenu(MenuDTO dto) {
        SysMenu menu = fromDTO(dto);
        menuMapper.insert(menu);
        return menu.getId();
    }

    @Transactional
    public void updateMenu(MenuDTO dto) {
        requireMenu(dto.id());
        menuMapper.updateById(fromDTO(dto));
    }

    @Transactional
    public void deleteMenu(Long menuId) {
        requireMenu(menuId);
        if (menuMapper.countChildren(menuId) > 0) {
            throw new BusinessException(ErrorCode.MENU_NOT_FOUND.getCode(), "该菜单有子菜单，禁止删除");
        }
        menuMapper.deleteById(menuId);
    }

    public MenuDTO getById(Long menuId) {
        return toDTO(requireMenu(menuId), List.of());
    }

    public List<MenuDTO> listTree(String menuName, Integer status) {
        List<SysMenu> all = menuMapper.selectAll(menuName, status);
        return buildTree(all, 0L);
    }

    public List<MenuDTO> listByRoleIds(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) return List.of();
        String ids = roleIds.stream().map(String::valueOf).collect(Collectors.joining(","));
        List<SysMenu> menus = menuMapper.findByRoleIds(ids);
        return buildTree(menus, 0L);
    }

    public List<String> listPermsByRoleIds(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) return List.of();
        String ids = roleIds.stream().map(String::valueOf).collect(Collectors.joining(","));
        return menuMapper.findPermsByRoleIds(ids);
    }

    private List<MenuDTO> buildTree(List<SysMenu> all, Long parentId) {
        Map<Long, List<SysMenu>> byParent = all.stream()
                .collect(Collectors.groupingBy(SysMenu::getParentId));
        List<SysMenu> children = byParent.getOrDefault(parentId, List.of());
        return children.stream()
                .map(m -> toDTO(m, buildTree(all, m.getId())))
                .collect(Collectors.toList());
    }

    private MenuDTO toDTO(SysMenu m, List<MenuDTO> children) {
        return new MenuDTO(m.getId(), m.getParentId(), m.getMenuName(), m.getMenuType(),
                m.getPath(), m.getComponent(), m.getPerms(), m.getIcon(),
                m.getOrderNum(), m.getVisible(), m.getStatus(), children);
    }

    private SysMenu fromDTO(MenuDTO dto) {
        SysMenu menu = new SysMenu();
        if (dto.id() != null) menu.setId(dto.id());
        menu.setParentId(dto.parentId() == null ? 0L : dto.parentId());
        menu.setMenuName(dto.menuName());
        menu.setMenuType(dto.menuType());
        menu.setPath(dto.path());
        menu.setComponent(dto.component());
        menu.setPerms(dto.perms());
        menu.setIcon(dto.icon());
        menu.setOrderNum(dto.orderNum() == null ? 0 : dto.orderNum());
        menu.setVisible(dto.visible() == null ? 1 : dto.visible());
        menu.setStatus(dto.status() == null ? 1 : dto.status());
        return menu;
    }

    private SysMenu requireMenu(Long menuId) {
        SysMenu menu = menuMapper.selectById(menuId);
        if (menu == null) throw new BusinessException(ErrorCode.MENU_NOT_FOUND);
        return menu;
    }
}
```

- [ ] **Step 3: Create MenuDubboServiceImpl**

```java
// mro-backend/system-service/src/main/java/com/mro/system/menu/service/MenuDubboServiceImpl.java
package com.mro.system.menu.service;

import com.mro.common.dubbo.system.MenuDubboService;
import com.mro.common.dubbo.system.dto.MenuDTO;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;

import java.util.List;

@DubboService(version = "1.0.0", group = "mro")
@RequiredArgsConstructor
public class MenuDubboServiceImpl implements MenuDubboService {

    private final MenuService menuService;

    @Override
    public Long createMenu(MenuDTO menu) { return menuService.createMenu(menu); }

    @Override
    public void updateMenu(MenuDTO menu) { menuService.updateMenu(menu); }

    @Override
    public void deleteMenu(Long menuId) { menuService.deleteMenu(menuId); }

    @Override
    public MenuDTO getById(Long menuId) { return menuService.getById(menuId); }

    @Override
    public List<MenuDTO> listTree(String menuName, Integer status) {
        return menuService.listTree(menuName, status);
    }

    @Override
    public List<MenuDTO> listByRoleIds(List<Long> roleIds) {
        return menuService.listByRoleIds(roleIds);
    }

    @Override
    public List<String> listPermsByRoleIds(List<Long> roleIds) {
        return menuService.listPermsByRoleIds(roleIds);
    }
}
```

- [ ] **Step 4: Create Dict entities, mappers, service, and Dubbo impl**

```java
// mro-backend/system-service/src/main/java/com/mro/system/dict/entity/SysDictType.java
package com.mro.system.dict.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_dict_type")
public class SysDictType {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String dictName;
    private String dictType;
    private Integer status;

    @TableLogic
    private Integer isDeleted;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
```

```java
// mro-backend/system-service/src/main/java/com/mro/system/dict/entity/SysDictData.java
package com.mro.system.dict.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_dict_data")
public class SysDictData {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String dictType;
    private String dictLabel;
    private String dictValue;
    private String cssClass;
    private Integer orderNum;
    private Integer status;

    @TableLogic
    private Integer isDeleted;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
```

```java
// mro-backend/system-service/src/main/java/com/mro/system/dict/mapper/SysDictTypeMapper.java
package com.mro.system.dict.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mro.system.dict.entity.SysDictType;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysDictTypeMapper extends BaseMapper<SysDictType> {}
```

```java
// mro-backend/system-service/src/main/java/com/mro/system/dict/mapper/SysDictDataMapper.java
package com.mro.system.dict.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mro.system.dict.entity.SysDictData;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SysDictDataMapper extends BaseMapper<SysDictData> {

    default List<SysDictData> findByDictType(String dictType) {
        return selectList(new LambdaQueryWrapper<SysDictData>()
                .eq(SysDictData::getDictType, dictType)
                .eq(SysDictData::getStatus, 1)
                .orderByAsc(SysDictData::getOrderNum));
    }
}
```

```java
// mro-backend/system-service/src/main/java/com/mro/system/dict/service/DictService.java
package com.mro.system.dict.service;

import com.mro.common.core.exception.BusinessException;
import com.mro.common.core.exception.ErrorCode;
import com.mro.common.dubbo.system.dto.DictDataDTO;
import com.mro.common.dubbo.system.dto.DictTypeDTO;
import com.mro.system.dict.entity.SysDictData;
import com.mro.system.dict.entity.SysDictType;
import com.mro.system.dict.mapper.SysDictDataMapper;
import com.mro.system.dict.mapper.SysDictTypeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DictService {

    private final SysDictTypeMapper dictTypeMapper;
    private final SysDictDataMapper dictDataMapper;

    public List<DictDataDTO> listByDictType(String dictType) {
        return dictDataMapper.findByDictType(dictType).stream()
                .map(d -> new DictDataDTO(d.getId(), d.getDictType(), d.getDictLabel(),
                        d.getDictValue(), d.getOrderNum(), d.getCssClass(), d.getStatus()))
                .collect(Collectors.toList());
    }

    public DictTypeDTO getDictType(Long id) {
        SysDictType type = dictTypeMapper.selectById(id);
        if (type == null) throw new BusinessException(ErrorCode.DICT_NOT_FOUND);
        return new DictTypeDTO(type.getId(), type.getDictName(), type.getDictType(), type.getStatus());
    }

    public List<DictTypeDTO> listDictTypes() {
        return dictTypeMapper.selectList(null).stream()
                .map(t -> new DictTypeDTO(t.getId(), t.getDictName(), t.getDictType(), t.getStatus()))
                .collect(Collectors.toList());
    }
}
```

```java
// mro-backend/system-service/src/main/java/com/mro/system/dict/service/DictDubboServiceImpl.java
package com.mro.system.dict.service;

import com.mro.common.dubbo.system.DictDubboService;
import com.mro.common.dubbo.system.dto.DictDataDTO;
import com.mro.common.dubbo.system.dto.DictTypeDTO;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;

import java.util.List;

@DubboService(version = "1.0.0", group = "mro")
@RequiredArgsConstructor
public class DictDubboServiceImpl implements DictDubboService {

    private final DictService dictService;

    @Override
    public List<DictDataDTO> listByDictType(String dictType) {
        return dictService.listByDictType(dictType);
    }

    @Override
    public DictTypeDTO getDictType(Long id) { return dictService.getDictType(id); }

    @Override
    public List<DictTypeDTO> listDictTypes() { return dictService.listDictTypes(); }
}
```

- [ ] **Step 5: Verify system-service compiles**

```bash
cd mro-backend
mvn compile -pl system-service -am -DskipTests
```

Expected: `BUILD SUCCESS`

- [ ] **Step 6: Run all system-service tests**

```bash
cd mro-backend
mvn test -pl system-service -am -DskipTests=false
```

Expected: All tests pass (`DeptServiceTest`, `UserServiceTest`, `RoleServiceTest`).

- [ ] **Step 7: Commit**

```bash
cd mro-backend
git add system-service/src/
git commit -m "feat(system): add menu + dict modules

Refs: SYS-004, SYS-005"
```

---

## Task 6: Final Build Validation

- [ ] **Step 1: Build system-service**

```bash
cd mro-backend
mvn clean package -pl system-service -am -DskipTests
```

Expected: `BUILD SUCCESS`. JAR in `system-service/target/`.

- [ ] **Step 2: Run all tests**

```bash
cd mro-backend
mvn test -pl system-service -am
```

Expected: All tests pass (~12 tests).

- [ ] **Step 3: Final commit**

```bash
cd mro-backend
git add .
git commit -m "chore(be-03): system-service complete — all modules and tests pass

Refs: SYS-001, SYS-002, SYS-003, SYS-004, SYS-005, SYS-006, PLAT-002"
```

---

## Next Step

After BE-03 completes, proceed to **BE-04** (`docs/superpowers/plans/2026-05-26-be04-manage-web-bff.md`) — manage-web BFF skeleton with system management HTTP controllers calling system-service and auth-service Dubbo interfaces.

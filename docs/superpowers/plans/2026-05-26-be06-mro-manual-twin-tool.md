# BE-06: MRO 服务组2 — maintenance-manual · digital-twin · tooling-material 实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现 maintenance-manual-service（维修手册智能检索）、digital-twin-service（数字孪生监控）、tooling-material-service（工具与物料管理）三个 MRO 微服务的完整 Dubbo Provider 端。

**Architecture:** 三个独立 Spring Boot 服务，各持有独立 MySQL Schema（mro_maintenance_manual / mro_digital_twin / mro_tooling_material），通过 Dubbo 暴露接口，不对外直接暴露 HTTP。

**Tech Stack:** Java 21 · Spring Boot 3.3 · Dubbo 3.3 · MyBatis-Plus 3.5 · Flyway 10 · MySQL 8 · Nacos · Maven Multi-module

**前置条件：** BE-01 完成（mro-common 模块可用）

---

## 文件结构

### maintenance-manual-service
```
mro-backend/maintenance-manual-service/
├── pom.xml
└── src/
    ├── main/
    │   ├── java/com/mro/maintenancemanual/
    │   │   ├── MaintenanceManualApplication.java
    │   │   ├── entity/
    │   │   │   ├── MaintenanceManual.java
    │   │   │   └── ManualSection.java
    │   │   ├── mapper/
    │   │   │   ├── MaintenanceManualMapper.java
    │   │   │   └── ManualSectionMapper.java
    │   │   ├── service/
    │   │   │   └── MaintenanceManualService.java
    │   │   └── dubbo/
    │   │       └── ManualDubboServiceImpl.java
    │   └── resources/
    │       ├── application.yml
    │       └── db/migration/V1__init_maintenance_manual.sql
    └── test/java/com/mro/maintenancemanual/service/
        └── MaintenanceManualServiceTest.java
```

### digital-twin-service
```
mro-backend/digital-twin-service/
├── pom.xml
└── src/
    ├── main/
    │   ├── java/com/mro/digitaltwin/
    │   │   ├── DigitalTwinApplication.java
    │   │   ├── entity/
    │   │   │   ├── DigitalTwinModel.java
    │   │   │   └── DigitalTwinDataPoint.java
    │   │   ├── mapper/
    │   │   │   ├── DigitalTwinModelMapper.java
    │   │   │   └── DigitalTwinDataPointMapper.java
    │   │   ├── service/
    │   │   │   └── DigitalTwinService.java
    │   │   └── dubbo/
    │   │       └── DigitalTwinDubboServiceImpl.java
    │   └── resources/
    │       ├── application.yml
    │       └── db/migration/V1__init_digital_twin.sql
    └── test/java/com/mro/digitaltwin/service/
        └── DigitalTwinServiceTest.java
```

### tooling-material-service
```
mro-backend/tooling-material-service/
├── pom.xml
└── src/
    ├── main/
    │   ├── java/com/mro/toolingmaterial/
    │   │   ├── ToolingMaterialApplication.java
    │   │   ├── entity/
    │   │   │   ├── ToolingItem.java
    │   │   │   └── MaterialItem.java
    │   │   ├── mapper/
    │   │   │   ├── ToolingItemMapper.java
    │   │   │   └── MaterialItemMapper.java
    │   │   ├── service/
    │   │   │   ├── ToolingItemService.java
    │   │   │   └── MaterialItemService.java
    │   │   └── dubbo/
    │   │       └── ToolingMaterialDubboServiceImpl.java
    │   └── resources/
    │       ├── application.yml
    │       └── db/migration/V1__init_tooling_material.sql
    └── test/java/com/mro/toolingmaterial/service/
        └── ToolingMaterialServiceTest.java
```

---

## Task 1: maintenance-manual-service — POM + 配置 + Schema

**Files:**
- Create: `mro-backend/maintenance-manual-service/pom.xml`
- Create: `mro-backend/maintenance-manual-service/src/main/java/com/mro/maintenancemanual/MaintenanceManualApplication.java`
- Create: `mro-backend/maintenance-manual-service/src/main/resources/application.yml`
- Create: `mro-backend/maintenance-manual-service/src/main/resources/db/migration/V1__init_maintenance_manual.sql`

- [ ] **Step 1: 创建 POM**

```xml
<!-- mro-backend/maintenance-manual-service/pom.xml -->
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <parent>
    <groupId>com.mro</groupId>
    <artifactId>mro-backend</artifactId>
    <version>1.0.0-SNAPSHOT</version>
  </parent>
  <artifactId>maintenance-manual-service</artifactId>

  <dependencies>
    <dependency><groupId>com.mro</groupId><artifactId>mro-common-core</artifactId></dependency>
    <dependency><groupId>com.mro</groupId><artifactId>mro-common-dubbo</artifactId></dependency>
    <dependency><groupId>com.mro</groupId><artifactId>mro-common-data</artifactId></dependency>
    <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-web</artifactId></dependency>
    <dependency><groupId>com.alibaba.cloud</groupId><artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId></dependency>
    <dependency><groupId>com.alibaba.cloud</groupId><artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId></dependency>
    <dependency><groupId>org.apache.dubbo</groupId><artifactId>dubbo-spring-boot-starter</artifactId></dependency>
    <dependency><groupId>com.mysql</groupId><artifactId>mysql-connector-j</artifactId></dependency>
    <dependency><groupId>com.baomidou</groupId><artifactId>mybatis-plus-spring-boot3-starter</artifactId></dependency>
    <dependency><groupId>org.flywaydb</groupId><artifactId>flyway-mysql</artifactId></dependency>
    <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-test</artifactId><scope>test</scope></dependency>
    <dependency><groupId>org.mockito</groupId><artifactId>mockito-core</artifactId><scope>test</scope></dependency>
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

- [ ] **Step 2: 注册到父 POM**

在 `mro-backend/pom.xml` `<modules>` 中追加：
```xml
<module>maintenance-manual-service</module>
```

- [ ] **Step 3: 创建启动类**

```java
package com.mro.maintenancemanual;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableDubbo
public class MaintenanceManualApplication {
    public static void main(String[] args) {
        SpringApplication.run(MaintenanceManualApplication.class, args);
    }
}
```

- [ ] **Step 4: 创建 application.yml**

```yaml
server:
  port: 8088

spring:
  application:
    name: maintenance-manual-service
  threads:
    virtual:
      enabled: true
  datasource:
    url: jdbc:mysql://localhost:3306/mro_maintenance_manual?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai
    username: root
    password: root
    driver-class-name: com.mysql.cj.jdbc.Driver
  flyway:
    locations: classpath:db/migration
    baseline-on-migrate: true
  cloud:
    nacos:
      server-addr: 127.0.0.1:8848
      discovery:
        namespace: dev
      config:
        namespace: dev
        file-extension: yaml

dubbo:
  application:
    name: maintenance-manual-service
  registry:
    address: nacos://127.0.0.1:8848
    parameters:
      namespace: dev
  protocol:
    name: dubbo
    port: 20886
  provider:
    timeout: 5000
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
      id-type: auto
```

- [ ] **Step 5: 创建 Flyway 脚本**

```sql
-- V1__init_maintenance_manual.sql
CREATE DATABASE IF NOT EXISTS mro_maintenance_manual DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE mro_maintenance_manual;

CREATE TABLE IF NOT EXISTS maintenance_manual (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    manual_code     VARCHAR(50)     NOT NULL UNIQUE COMMENT '手册编号',
    manual_name     VARCHAR(200)    NOT NULL COMMENT '手册名称',
    aircraft_type   VARCHAR(50)     COMMENT '适用机型',
    version         VARCHAR(20)     NOT NULL COMMENT '版本号',
    category        VARCHAR(50)     COMMENT '手册类别(AMM/CMM/TSM/FIM)',
    language        VARCHAR(10)     NOT NULL DEFAULT 'zh' COMMENT '语言',
    file_url        VARCHAR(500)    COMMENT '原始文件URL',
    status          TINYINT         NOT NULL DEFAULT 1 COMMENT '状态 1=有效 2=已废止',
    effective_date  DATE            COMMENT '生效日期',
    create_by       BIGINT,
    create_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted      TINYINT         NOT NULL DEFAULT 0,
    INDEX idx_manual_code (manual_code),
    INDEX idx_aircraft_type (aircraft_type),
    INDEX idx_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='维修手册表';

CREATE TABLE IF NOT EXISTS manual_section (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    manual_id       BIGINT          NOT NULL COMMENT '手册ID',
    section_no      VARCHAR(50)     NOT NULL COMMENT '章节编号',
    section_title   VARCHAR(200)    NOT NULL COMMENT '章节标题',
    parent_id       BIGINT          DEFAULT 0 COMMENT '父章节ID',
    content         LONGTEXT        COMMENT '章节内容',
    sort_order      INT             NOT NULL DEFAULT 0,
    create_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted      TINYINT         NOT NULL DEFAULT 0,
    INDEX idx_manual_id (manual_id),
    INDEX idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='手册章节表';
```

- [ ] **Step 6: Commit**

```bash
git add mro-backend/maintenance-manual-service/
git commit -m "feat: add maintenance-manual-service skeleton with DB schema

Refs: BE-06"
```

---

## Task 2: maintenance-manual-service — Entity + Mapper + Service + Dubbo Provider

**Files:**
- Create: entity, mapper, service, dubbo impl (见文件结构)
- Test: `src/test/java/com/mro/maintenancemanual/service/MaintenanceManualServiceTest.java`

- [ ] **Step 1: 写失败测试**

```java
// src/test/java/com/mro/maintenancemanual/service/MaintenanceManualServiceTest.java
package com.mro.maintenancemanual.service;

import com.mro.maintenancemanual.entity.MaintenanceManual;
import com.mro.maintenancemanual.mapper.MaintenanceManualMapper;
import com.mro.maintenancemanual.mapper.ManualSectionMapper;
import com.mro.common.core.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MaintenanceManualServiceTest {

    @Mock
    private MaintenanceManualMapper manualMapper;

    @Mock
    private ManualSectionMapper sectionMapper;

    @InjectMocks
    private MaintenanceManualService manualService;

    @Test
    void test_createManual_validInput_returnsId() {
        when(manualMapper.existsByManualCode("AMM-001")).thenReturn(false);
        when(manualMapper.insert(any())).thenAnswer(inv -> {
            MaintenanceManual m = inv.getArgument(0);
            m.setId(1L);
            return 1;
        });

        MaintenanceManual manual = new MaintenanceManual();
        manual.setManualCode("AMM-001");
        manual.setManualName("A320发动机维修手册");
        manual.setVersion("Rev.5");
        manual.setCategory("AMM");

        Long id = manualService.createManual(manual, 1L);
        assertEquals(1L, id);
        verify(manualMapper).insert(any());
    }

    @Test
    void test_createManual_duplicateCode_throwsBusinessException() {
        when(manualMapper.existsByManualCode("AMM-001")).thenReturn(true);

        MaintenanceManual manual = new MaintenanceManual();
        manual.setManualCode("AMM-001");
        manual.setManualName("测试手册");
        manual.setVersion("Rev.1");

        assertThrows(BusinessException.class, () -> manualService.createManual(manual, 1L));
        verify(manualMapper, never()).insert(any());
    }

    @Test
    void test_searchByKeyword_emptyKeyword_throwsBusinessException() {
        assertThrows(BusinessException.class, () -> manualService.searchSections("  ", 1, 10));
    }
}
```

- [ ] **Step 2: 运行测试确认失败**

```bash
mvn test -pl maintenance-manual-service -Dtest=MaintenanceManualServiceTest -q
```
期望：FAIL

- [ ] **Step 3: 创建 Entity 类**

```java
// src/main/java/com/mro/maintenancemanual/entity/MaintenanceManual.java
package com.mro.maintenancemanual.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("maintenance_manual")
public class MaintenanceManual {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String manualCode;
    private String manualName;
    private String aircraftType;
    private String version;
    private String category;
    private String language;
    private String fileUrl;
    private Integer status;
    private LocalDate effectiveDate;
    private Long createBy;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer isDeleted;
}
```

```java
// src/main/java/com/mro/maintenancemanual/entity/ManualSection.java
package com.mro.maintenancemanual.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("manual_section")
public class ManualSection {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long manualId;
    private String sectionNo;
    private String sectionTitle;
    private Long parentId;
    private String content;
    private Integer sortOrder;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer isDeleted;
}
```

- [ ] **Step 4: 创建 Mapper**

```java
// src/main/java/com/mro/maintenancemanual/mapper/MaintenanceManualMapper.java
package com.mro.maintenancemanual.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mro.maintenancemanual.entity.MaintenanceManual;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface MaintenanceManualMapper extends BaseMapper<MaintenanceManual> {

    @Select("SELECT COUNT(1) > 0 FROM maintenance_manual WHERE manual_code = #{manualCode} AND is_deleted = 0")
    boolean existsByManualCode(@Param("manualCode") String manualCode);
}
```

```java
// src/main/java/com/mro/maintenancemanual/mapper/ManualSectionMapper.java
package com.mro.maintenancemanual.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mro.maintenancemanual.entity.ManualSection;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface ManualSectionMapper extends BaseMapper<ManualSection> {

    @Select("SELECT * FROM manual_section WHERE manual_id = #{manualId} AND is_deleted = 0 ORDER BY sort_order ASC")
    List<ManualSection> findByManualId(@Param("manualId") Long manualId);

    @Select("SELECT * FROM manual_section WHERE is_deleted = 0 " +
            "AND (section_title LIKE CONCAT('%', #{keyword}, '%') OR content LIKE CONCAT('%', #{keyword}, '%'))")
    Page<ManualSection> searchByKeyword(Page<ManualSection> page, @Param("keyword") String keyword);
}
```

- [ ] **Step 5: 创建 Service**

```java
// src/main/java/com/mro/maintenancemanual/service/MaintenanceManualService.java
package com.mro.maintenancemanual.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mro.maintenancemanual.entity.MaintenanceManual;
import com.mro.maintenancemanual.entity.ManualSection;
import com.mro.maintenancemanual.mapper.MaintenanceManualMapper;
import com.mro.maintenancemanual.mapper.ManualSectionMapper;
import com.mro.common.core.exception.BusinessException;
import com.mro.common.core.result.ErrorCode;
import com.mro.common.core.result.PageResult;
import com.mro.common.dubbo.dto.manual.ManualDTO;
import com.mro.common.dubbo.dto.manual.ManualSectionDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MaintenanceManualService {

    private final MaintenanceManualMapper manualMapper;
    private final ManualSectionMapper sectionMapper;

    public Long createManual(MaintenanceManual manual, Long createBy) {
        if (manualMapper.existsByManualCode(manual.getManualCode())) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "手册编号已存在: " + manual.getManualCode());
        }
        manual.setStatus(1);
        manual.setCreateBy(createBy);
        manual.setCreateTime(LocalDateTime.now());
        manualMapper.insert(manual);
        return manual.getId();
    }

    public PageResult<ManualDTO> pageManuals(String aircraftType, String category, int pageNum, int pageSize) {
        Page<MaintenanceManual> page = new Page<>(pageNum, pageSize);
        manualMapper.selectPage(page, new LambdaQueryWrapper<MaintenanceManual>()
                .eq(StringUtils.hasText(aircraftType), MaintenanceManual::getAircraftType, aircraftType)
                .eq(StringUtils.hasText(category), MaintenanceManual::getCategory, category)
                .eq(MaintenanceManual::getStatus, 1)
                .orderByDesc(MaintenanceManual::getCreateTime));
        List<ManualDTO> list = page.getRecords().stream().map(m ->
                new ManualDTO(m.getId(), m.getManualCode(), m.getManualName(),
                        m.getAircraftType(), m.getVersion(), m.getCategory(),
                        m.getLanguage(), m.getFileUrl(), m.getStatus())
        ).toList();
        return PageResult.of(list, page.getTotal(), pageNum, pageSize);
    }

    public List<ManualSectionDTO> listSections(Long manualId) {
        return sectionMapper.findByManualId(manualId).stream().map(s ->
                new ManualSectionDTO(s.getId(), s.getManualId(), s.getSectionNo(),
                        s.getSectionTitle(), s.getParentId(), s.getSortOrder())
        ).toList();
    }

    public PageResult<ManualSectionDTO> searchSections(String keyword, int pageNum, int pageSize) {
        if (!StringUtils.hasText(keyword)) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "搜索关键词不能为空");
        }
        Page<ManualSection> page = new Page<>(pageNum, pageSize);
        sectionMapper.searchByKeyword(page, keyword.trim());
        List<ManualSectionDTO> list = page.getRecords().stream().map(s ->
                new ManualSectionDTO(s.getId(), s.getManualId(), s.getSectionNo(),
                        s.getSectionTitle(), s.getParentId(), s.getSortOrder())
        ).toList();
        return PageResult.of(list, page.getTotal(), pageNum, pageSize);
    }
}
```

- [ ] **Step 6: 创建 Dubbo Provider**

```java
// src/main/java/com/mro/maintenancemanual/dubbo/ManualDubboServiceImpl.java
package com.mro.maintenancemanual.dubbo;

import com.mro.maintenancemanual.entity.MaintenanceManual;
import com.mro.maintenancemanual.service.MaintenanceManualService;
import com.mro.common.core.result.PageResult;
import com.mro.common.dubbo.dto.manual.ManualDTO;
import com.mro.common.dubbo.dto.manual.ManualSectionDTO;
import com.mro.common.dubbo.service.ManualDubboService;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;
import java.util.List;

@DubboService(version = "1.0.0", group = "mro")
@RequiredArgsConstructor
public class ManualDubboServiceImpl implements ManualDubboService {

    private final MaintenanceManualService manualService;

    @Override
    public Long createManual(String manualCode, String manualName, String aircraftType,
                              String version, String category, String fileUrl, Long createBy) {
        MaintenanceManual manual = new MaintenanceManual();
        manual.setManualCode(manualCode);
        manual.setManualName(manualName);
        manual.setAircraftType(aircraftType);
        manual.setVersion(version);
        manual.setCategory(category);
        manual.setFileUrl(fileUrl);
        return manualService.createManual(manual, createBy);
    }

    @Override
    public PageResult<ManualDTO> pageManuals(String aircraftType, String category, int pageNum, int pageSize) {
        return manualService.pageManuals(aircraftType, category, pageNum, pageSize);
    }

    @Override
    public List<ManualSectionDTO> listSections(Long manualId) {
        return manualService.listSections(manualId);
    }

    @Override
    public PageResult<ManualSectionDTO> searchSections(String keyword, int pageNum, int pageSize) {
        return manualService.searchSections(keyword, pageNum, pageSize);
    }
}
```

- [ ] **Step 7: 运行测试**

```bash
mvn test -pl maintenance-manual-service -q
```
期望：BUILD SUCCESS

- [ ] **Step 8: Commit**

```bash
git add mro-backend/maintenance-manual-service/
git commit -m "feat: add maintenance-manual-service complete implementation

Refs: BE-06"
```

---

## Task 3: digital-twin-service — POM + 配置 + Schema

**Files:**
- Create: `mro-backend/digital-twin-service/pom.xml`
- Create: `mro-backend/digital-twin-service/src/main/java/com/mro/digitaltwin/DigitalTwinApplication.java`
- Create: `mro-backend/digital-twin-service/src/main/resources/application.yml`
- Create: `mro-backend/digital-twin-service/src/main/resources/db/migration/V1__init_digital_twin.sql`

- [ ] **Step 1: 创建 POM**

```xml
<!-- mro-backend/digital-twin-service/pom.xml -->
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <parent>
    <groupId>com.mro</groupId>
    <artifactId>mro-backend</artifactId>
    <version>1.0.0-SNAPSHOT</version>
  </parent>
  <artifactId>digital-twin-service</artifactId>

  <dependencies>
    <dependency><groupId>com.mro</groupId><artifactId>mro-common-core</artifactId></dependency>
    <dependency><groupId>com.mro</groupId><artifactId>mro-common-dubbo</artifactId></dependency>
    <dependency><groupId>com.mro</groupId><artifactId>mro-common-data</artifactId></dependency>
    <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-web</artifactId></dependency>
    <dependency><groupId>com.alibaba.cloud</groupId><artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId></dependency>
    <dependency><groupId>com.alibaba.cloud</groupId><artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId></dependency>
    <dependency><groupId>org.apache.dubbo</groupId><artifactId>dubbo-spring-boot-starter</artifactId></dependency>
    <dependency><groupId>com.mysql</groupId><artifactId>mysql-connector-j</artifactId></dependency>
    <dependency><groupId>com.baomidou</groupId><artifactId>mybatis-plus-spring-boot3-starter</artifactId></dependency>
    <dependency><groupId>org.flywaydb</groupId><artifactId>flyway-mysql</artifactId></dependency>
    <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-test</artifactId><scope>test</scope></dependency>
    <dependency><groupId>org.mockito</groupId><artifactId>mockito-core</artifactId><scope>test</scope></dependency>
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

- [ ] **Step 2: 注册到父 POM**

```xml
<module>digital-twin-service</module>
```

- [ ] **Step 3: 创建启动类**

```java
package com.mro.digitaltwin;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableDubbo
public class DigitalTwinApplication {
    public static void main(String[] args) {
        SpringApplication.run(DigitalTwinApplication.class, args);
    }
}
```

- [ ] **Step 4: 创建 application.yml**

```yaml
server:
  port: 8089

spring:
  application:
    name: digital-twin-service
  threads:
    virtual:
      enabled: true
  datasource:
    url: jdbc:mysql://localhost:3306/mro_digital_twin?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai
    username: root
    password: root
    driver-class-name: com.mysql.cj.jdbc.Driver
  flyway:
    locations: classpath:db/migration
    baseline-on-migrate: true
  cloud:
    nacos:
      server-addr: 127.0.0.1:8848
      discovery:
        namespace: dev
      config:
        namespace: dev
        file-extension: yaml

dubbo:
  application:
    name: digital-twin-service
  registry:
    address: nacos://127.0.0.1:8848
    parameters:
      namespace: dev
  protocol:
    name: dubbo
    port: 20887
  provider:
    timeout: 5000
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
      id-type: auto
```

- [ ] **Step 5: 创建 Flyway 脚本**

```sql
-- V1__init_digital_twin.sql
CREATE DATABASE IF NOT EXISTS mro_digital_twin DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE mro_digital_twin;

CREATE TABLE IF NOT EXISTS digital_twin_model (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    model_code      VARCHAR(50)     NOT NULL UNIQUE COMMENT '模型编号',
    model_name      VARCHAR(200)    NOT NULL COMMENT '模型名称',
    aircraft_no     VARCHAR(50)     NOT NULL COMMENT '飞机注册号',
    aircraft_type   VARCHAR(50)     COMMENT '飞机型号',
    model_file_url  VARCHAR(500)    COMMENT '3D模型文件URL',
    status          TINYINT         NOT NULL DEFAULT 1 COMMENT '状态 1=正常 2=异常 3=维修中',
    last_sync_time  DATETIME        COMMENT '最后同步时间',
    create_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted      TINYINT         NOT NULL DEFAULT 0,
    INDEX idx_aircraft_no (aircraft_no),
    INDEX idx_model_code (model_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数字孪生模型表';

CREATE TABLE IF NOT EXISTS digital_twin_data_point (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    model_id        BIGINT          NOT NULL COMMENT '模型ID',
    aircraft_no     VARCHAR(50)     NOT NULL COMMENT '飞机注册号',
    data_type       VARCHAR(100)    NOT NULL COMMENT '数据类型(temperature/pressure/vibration等)',
    component_code  VARCHAR(100)    COMMENT '部件编号',
    value           DECIMAL(15,4)   NOT NULL COMMENT '数据值',
    unit            VARCHAR(20)     COMMENT '单位',
    is_anomaly      TINYINT         NOT NULL DEFAULT 0 COMMENT '是否异常 0=正常 1=异常',
    record_time     DATETIME        NOT NULL COMMENT '采集时间',
    create_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_model_id (model_id),
    INDEX idx_aircraft_no (aircraft_no),
    INDEX idx_record_time (record_time),
    INDEX idx_data_type (data_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数字孪生数据点表';
```

- [ ] **Step 6: Commit**

```bash
git add mro-backend/digital-twin-service/
git commit -m "feat: add digital-twin-service skeleton with DB schema

Refs: BE-06"
```

---

## Task 4: digital-twin-service — Entity + Mapper + Service + Dubbo Provider

**Files:**
- Create: entity, mapper, service, dubbo impl (见文件结构)
- Test: `src/test/java/com/mro/digitaltwin/service/DigitalTwinServiceTest.java`

- [ ] **Step 1: 写失败测试**

```java
// src/test/java/com/mro/digitaltwin/service/DigitalTwinServiceTest.java
package com.mro.digitaltwin.service;

import com.mro.digitaltwin.entity.DigitalTwinModel;
import com.mro.digitaltwin.entity.DigitalTwinDataPoint;
import com.mro.digitaltwin.mapper.DigitalTwinModelMapper;
import com.mro.digitaltwin.mapper.DigitalTwinDataPointMapper;
import com.mro.common.core.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DigitalTwinServiceTest {

    @Mock
    private DigitalTwinModelMapper modelMapper;

    @Mock
    private DigitalTwinDataPointMapper dataPointMapper;

    @InjectMocks
    private DigitalTwinService twinService;

    @Test
    void test_registerModel_validInput_returnsId() {
        when(modelMapper.existsByModelCode("MODEL-001")).thenReturn(false);
        when(modelMapper.insert(any())).thenAnswer(inv -> {
            DigitalTwinModel m = inv.getArgument(0);
            m.setId(1L);
            return 1;
        });

        Long id = twinService.registerModel("MODEL-001", "A320数字孪生", "B-1234", "A320", "/models/a320.glb");
        assertEquals(1L, id);
    }

    @Test
    void test_registerModel_duplicateCode_throwsBusinessException() {
        when(modelMapper.existsByModelCode("MODEL-001")).thenReturn(true);
        assertThrows(BusinessException.class,
                () -> twinService.registerModel("MODEL-001", "测试", "B-0000", "A320", null));
    }

    @Test
    void test_recordDataPoint_invalidValue_throwsBusinessException() {
        assertThrows(BusinessException.class,
                () -> twinService.recordDataPoint(1L, "B-1234", "temperature", "ENG-1", null, "°C"));
    }
}
```

- [ ] **Step 2: 运行测试确认失败**

```bash
mvn test -pl digital-twin-service -Dtest=DigitalTwinServiceTest -q
```
期望：FAIL

- [ ] **Step 3: 创建 Entity 类**

```java
// src/main/java/com/mro/digitaltwin/entity/DigitalTwinModel.java
package com.mro.digitaltwin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("digital_twin_model")
public class DigitalTwinModel {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String modelCode;
    private String modelName;
    private String aircraftNo;
    private String aircraftType;
    private String modelFileUrl;
    private Integer status;
    private LocalDateTime lastSyncTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer isDeleted;
}
```

```java
// src/main/java/com/mro/digitaltwin/entity/DigitalTwinDataPoint.java
package com.mro.digitaltwin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("digital_twin_data_point")
public class DigitalTwinDataPoint {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long modelId;
    private String aircraftNo;
    private String dataType;
    private String componentCode;
    private BigDecimal value;
    private String unit;
    private Integer isAnomaly;
    private LocalDateTime recordTime;
    private LocalDateTime createTime;
}
```

- [ ] **Step 4: 创建 Mapper**

```java
// src/main/java/com/mro/digitaltwin/mapper/DigitalTwinModelMapper.java
package com.mro.digitaltwin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mro.digitaltwin.entity.DigitalTwinModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface DigitalTwinModelMapper extends BaseMapper<DigitalTwinModel> {

    @Select("SELECT COUNT(1) > 0 FROM digital_twin_model WHERE model_code = #{modelCode} AND is_deleted = 0")
    boolean existsByModelCode(@Param("modelCode") String modelCode);
}
```

```java
// src/main/java/com/mro/digitaltwin/mapper/DigitalTwinDataPointMapper.java
package com.mro.digitaltwin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mro.digitaltwin.entity.DigitalTwinDataPoint;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface DigitalTwinDataPointMapper extends BaseMapper<DigitalTwinDataPoint> {

    @Select("SELECT * FROM digital_twin_data_point WHERE aircraft_no = #{aircraftNo} " +
            "AND data_type = #{dataType} ORDER BY record_time DESC LIMIT #{limit}")
    List<DigitalTwinDataPoint> findLatestByAircraftAndType(
            @Param("aircraftNo") String aircraftNo,
            @Param("dataType") String dataType,
            @Param("limit") int limit);

    @Select("SELECT * FROM digital_twin_data_point WHERE aircraft_no = #{aircraftNo} " +
            "AND is_anomaly = 1 ORDER BY record_time DESC LIMIT 100")
    List<DigitalTwinDataPoint> findAnomaliesByAircraftNo(@Param("aircraftNo") String aircraftNo);
}
```

- [ ] **Step 5: 创建 Service**

```java
// src/main/java/com/mro/digitaltwin/service/DigitalTwinService.java
package com.mro.digitaltwin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mro.digitaltwin.entity.DigitalTwinDataPoint;
import com.mro.digitaltwin.entity.DigitalTwinModel;
import com.mro.digitaltwin.mapper.DigitalTwinDataPointMapper;
import com.mro.digitaltwin.mapper.DigitalTwinModelMapper;
import com.mro.common.core.exception.BusinessException;
import com.mro.common.core.result.ErrorCode;
import com.mro.common.dubbo.dto.digitaltwin.DigitalTwinDataPointDTO;
import com.mro.common.dubbo.dto.digitaltwin.DigitalTwinModelDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DigitalTwinService {

    private final DigitalTwinModelMapper modelMapper;
    private final DigitalTwinDataPointMapper dataPointMapper;

    public Long registerModel(String modelCode, String modelName, String aircraftNo,
                               String aircraftType, String modelFileUrl) {
        if (modelMapper.existsByModelCode(modelCode)) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "模型编号已存在: " + modelCode);
        }
        DigitalTwinModel model = new DigitalTwinModel();
        model.setModelCode(modelCode);
        model.setModelName(modelName);
        model.setAircraftNo(aircraftNo);
        model.setAircraftType(aircraftType);
        model.setModelFileUrl(modelFileUrl);
        model.setStatus(1);
        model.setCreateTime(LocalDateTime.now());
        modelMapper.insert(model);
        return model.getId();
    }

    public Long recordDataPoint(Long modelId, String aircraftNo, String dataType,
                                 String componentCode, BigDecimal value, String unit) {
        if (value == null) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "数据值不能为空");
        }
        DigitalTwinDataPoint point = new DigitalTwinDataPoint();
        point.setModelId(modelId);
        point.setAircraftNo(aircraftNo);
        point.setDataType(dataType);
        point.setComponentCode(componentCode);
        point.setValue(value);
        point.setUnit(unit);
        point.setIsAnomaly(0);
        point.setRecordTime(LocalDateTime.now());
        point.setCreateTime(LocalDateTime.now());
        dataPointMapper.insert(point);
        return point.getId();
    }

    public DigitalTwinModelDTO getModelByAircraftNo(String aircraftNo) {
        DigitalTwinModel model = modelMapper.selectOne(
                new LambdaQueryWrapper<DigitalTwinModel>()
                        .eq(DigitalTwinModel::getAircraftNo, aircraftNo)
                        .eq(DigitalTwinModel::getIsDeleted, 0)
                        .last("LIMIT 1")
        );
        if (model == null) return null;
        return new DigitalTwinModelDTO(model.getId(), model.getModelCode(), model.getModelName(),
                model.getAircraftNo(), model.getAircraftType(), model.getModelFileUrl(),
                model.getStatus(), model.getLastSyncTime());
    }

    public List<DigitalTwinDataPointDTO> getLatestDataPoints(String aircraftNo, String dataType, int limit) {
        return dataPointMapper.findLatestByAircraftAndType(aircraftNo, dataType, limit).stream().map(p ->
                new DigitalTwinDataPointDTO(p.getId(), p.getModelId(), p.getAircraftNo(),
                        p.getDataType(), p.getComponentCode(), p.getValue(), p.getUnit(),
                        p.getIsAnomaly(), p.getRecordTime())
        ).toList();
    }

    public List<DigitalTwinDataPointDTO> getAnomalies(String aircraftNo) {
        return dataPointMapper.findAnomaliesByAircraftNo(aircraftNo).stream().map(p ->
                new DigitalTwinDataPointDTO(p.getId(), p.getModelId(), p.getAircraftNo(),
                        p.getDataType(), p.getComponentCode(), p.getValue(), p.getUnit(),
                        p.getIsAnomaly(), p.getRecordTime())
        ).toList();
    }
}
```

- [ ] **Step 6: 创建 Dubbo Provider**

```java
// src/main/java/com/mro/digitaltwin/dubbo/DigitalTwinDubboServiceImpl.java
package com.mro.digitaltwin.dubbo;

import com.mro.digitaltwin.service.DigitalTwinService;
import com.mro.common.dubbo.dto.digitaltwin.DigitalTwinDataPointDTO;
import com.mro.common.dubbo.dto.digitaltwin.DigitalTwinModelDTO;
import com.mro.common.dubbo.service.DigitalTwinDubboService;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;

import java.math.BigDecimal;
import java.util.List;

@DubboService(version = "1.0.0", group = "mro")
@RequiredArgsConstructor
public class DigitalTwinDubboServiceImpl implements DigitalTwinDubboService {

    private final DigitalTwinService twinService;

    @Override
    public Long registerModel(String modelCode, String modelName, String aircraftNo,
                               String aircraftType, String modelFileUrl) {
        return twinService.registerModel(modelCode, modelName, aircraftNo, aircraftType, modelFileUrl);
    }

    @Override
    public Long recordDataPoint(Long modelId, String aircraftNo, String dataType,
                                 String componentCode, BigDecimal value, String unit) {
        return twinService.recordDataPoint(modelId, aircraftNo, dataType, componentCode, value, unit);
    }

    @Override
    public DigitalTwinModelDTO getModelByAircraftNo(String aircraftNo) {
        return twinService.getModelByAircraftNo(aircraftNo);
    }

    @Override
    public List<DigitalTwinDataPointDTO> getLatestDataPoints(String aircraftNo, String dataType, int limit) {
        return twinService.getLatestDataPoints(aircraftNo, dataType, limit);
    }

    @Override
    public List<DigitalTwinDataPointDTO> getAnomalies(String aircraftNo) {
        return twinService.getAnomalies(aircraftNo);
    }
}
```

- [ ] **Step 7: 运行测试**

```bash
mvn test -pl digital-twin-service -q
```
期望：BUILD SUCCESS

- [ ] **Step 8: Commit**

```bash
git add mro-backend/digital-twin-service/
git commit -m "feat: add digital-twin-service complete implementation

Refs: BE-06"
```

---

## Task 5: tooling-material-service — POM + 配置 + Schema

**Files:**
- Create: `mro-backend/tooling-material-service/pom.xml`
- Create: `mro-backend/tooling-material-service/src/main/java/com/mro/toolingmaterial/ToolingMaterialApplication.java`
- Create: `mro-backend/tooling-material-service/src/main/resources/application.yml`
- Create: `mro-backend/tooling-material-service/src/main/resources/db/migration/V1__init_tooling_material.sql`

- [ ] **Step 1: 创建 POM**

```xml
<!-- mro-backend/tooling-material-service/pom.xml -->
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <parent>
    <groupId>com.mro</groupId>
    <artifactId>mro-backend</artifactId>
    <version>1.0.0-SNAPSHOT</version>
  </parent>
  <artifactId>tooling-material-service</artifactId>

  <dependencies>
    <dependency><groupId>com.mro</groupId><artifactId>mro-common-core</artifactId></dependency>
    <dependency><groupId>com.mro</groupId><artifactId>mro-common-dubbo</artifactId></dependency>
    <dependency><groupId>com.mro</groupId><artifactId>mro-common-data</artifactId></dependency>
    <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-web</artifactId></dependency>
    <dependency><groupId>com.alibaba.cloud</groupId><artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId></dependency>
    <dependency><groupId>com.alibaba.cloud</groupId><artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId></dependency>
    <dependency><groupId>org.apache.dubbo</groupId><artifactId>dubbo-spring-boot-starter</artifactId></dependency>
    <dependency><groupId>com.mysql</groupId><artifactId>mysql-connector-j</artifactId></dependency>
    <dependency><groupId>com.baomidou</groupId><artifactId>mybatis-plus-spring-boot3-starter</artifactId></dependency>
    <dependency><groupId>org.flywaydb</groupId><artifactId>flyway-mysql</artifactId></dependency>
    <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-test</artifactId><scope>test</scope></dependency>
    <dependency><groupId>org.mockito</groupId><artifactId>mockito-core</artifactId><scope>test</scope></dependency>
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

- [ ] **Step 2: 注册到父 POM**

```xml
<module>tooling-material-service</module>
```

- [ ] **Step 3: 创建启动类**

```java
package com.mro.toolingmaterial;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableDubbo
public class ToolingMaterialApplication {
    public static void main(String[] args) {
        SpringApplication.run(ToolingMaterialApplication.class, args);
    }
}
```

- [ ] **Step 4: 创建 application.yml**

```yaml
server:
  port: 8090

spring:
  application:
    name: tooling-material-service
  threads:
    virtual:
      enabled: true
  datasource:
    url: jdbc:mysql://localhost:3306/mro_tooling_material?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai
    username: root
    password: root
    driver-class-name: com.mysql.cj.jdbc.Driver
  flyway:
    locations: classpath:db/migration
    baseline-on-migrate: true
  cloud:
    nacos:
      server-addr: 127.0.0.1:8848
      discovery:
        namespace: dev
      config:
        namespace: dev
        file-extension: yaml

dubbo:
  application:
    name: tooling-material-service
  registry:
    address: nacos://127.0.0.1:8848
    parameters:
      namespace: dev
  protocol:
    name: dubbo
    port: 20888
  provider:
    timeout: 5000
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
      id-type: auto
```

- [ ] **Step 5: 创建 Flyway 脚本**

```sql
-- V1__init_tooling_material.sql
CREATE DATABASE IF NOT EXISTS mro_tooling_material DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE mro_tooling_material;

CREATE TABLE IF NOT EXISTS tooling_item (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    tool_code       VARCHAR(50)     NOT NULL UNIQUE COMMENT '工具编号',
    tool_name       VARCHAR(200)    NOT NULL COMMENT '工具名称',
    tool_type       VARCHAR(50)     COMMENT '工具类别',
    specification   VARCHAR(200)    COMMENT '规格型号',
    total_qty       INT             NOT NULL DEFAULT 0 COMMENT '总数量',
    available_qty   INT             NOT NULL DEFAULT 0 COMMENT '可用数量',
    unit            VARCHAR(20)     COMMENT '计量单位',
    location        VARCHAR(100)    COMMENT '存放位置',
    status          TINYINT         NOT NULL DEFAULT 1 COMMENT '状态 1=正常 2=维修 3=报废',
    create_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted      TINYINT         NOT NULL DEFAULT 0,
    INDEX idx_tool_code (tool_code),
    INDEX idx_tool_type (tool_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工具台账表';

CREATE TABLE IF NOT EXISTS material_item (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    part_no         VARCHAR(50)     NOT NULL UNIQUE COMMENT '料件编号(P/N)',
    part_name       VARCHAR(200)    NOT NULL COMMENT '料件名称',
    aircraft_type   VARCHAR(50)     COMMENT '适用机型',
    category        VARCHAR(50)     COMMENT '料件类别',
    stock_qty       INT             NOT NULL DEFAULT 0 COMMENT '库存数量',
    min_stock       INT             NOT NULL DEFAULT 0 COMMENT '最低库存预警',
    unit            VARCHAR(20)     COMMENT '计量单位',
    unit_price      DECIMAL(12,2)   COMMENT '单价',
    supplier        VARCHAR(200)    COMMENT '供应商',
    status          TINYINT         NOT NULL DEFAULT 1 COMMENT '状态 1=正常 2=停产 3=报废',
    create_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted      TINYINT         NOT NULL DEFAULT 0,
    INDEX idx_part_no (part_no),
    INDEX idx_aircraft_type (aircraft_type),
    INDEX idx_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物料台账表';
```

- [ ] **Step 6: Commit**

```bash
git add mro-backend/tooling-material-service/
git commit -m "feat: add tooling-material-service skeleton with DB schema

Refs: BE-06"
```

---

## Task 6: tooling-material-service — Entity + Mapper + Service + Dubbo Provider

**Files:**
- Create: entity, mapper, service, dubbo impl (见文件结构)
- Test: `src/test/java/com/mro/toolingmaterial/service/ToolingMaterialServiceTest.java`

- [ ] **Step 1: 写失败测试**

```java
// src/test/java/com/mro/toolingmaterial/service/ToolingMaterialServiceTest.java
package com.mro.toolingmaterial.service;

import com.mro.toolingmaterial.entity.ToolingItem;
import com.mro.toolingmaterial.entity.MaterialItem;
import com.mro.toolingmaterial.mapper.ToolingItemMapper;
import com.mro.toolingmaterial.mapper.MaterialItemMapper;
import com.mro.common.core.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ToolingMaterialServiceTest {

    @Mock
    private ToolingItemMapper toolingMapper;

    @Mock
    private MaterialItemMapper materialMapper;

    @InjectMocks
    private ToolingItemService toolingService;

    @Test
    void test_createTooling_validInput_returnsId() {
        when(toolingMapper.existsByToolCode("TOOL-001")).thenReturn(false);
        when(toolingMapper.insert(any())).thenAnswer(inv -> {
            ToolingItem t = inv.getArgument(0);
            t.setId(1L);
            return 1;
        });

        ToolingItem tool = new ToolingItem();
        tool.setToolCode("TOOL-001");
        tool.setToolName("扭矩扳手");
        tool.setTotalQty(5);
        tool.setAvailableQty(5);

        Long id = toolingService.createTool(tool);
        assertEquals(1L, id);
    }

    @Test
    void test_createTooling_duplicateCode_throwsBusinessException() {
        when(toolingMapper.existsByToolCode("TOOL-001")).thenReturn(true);

        ToolingItem tool = new ToolingItem();
        tool.setToolCode("TOOL-001");
        tool.setToolName("扭矩扳手");
        tool.setTotalQty(5);
        tool.setAvailableQty(5);

        assertThrows(BusinessException.class, () -> toolingService.createTool(tool));
        verify(toolingMapper, never()).insert(any());
    }

    @Test
    void test_createTooling_negativeQty_throwsBusinessException() {
        ToolingItem tool = new ToolingItem();
        tool.setToolCode("TOOL-002");
        tool.setToolName("测试工具");
        tool.setTotalQty(-1);
        tool.setAvailableQty(0);

        assertThrows(BusinessException.class, () -> toolingService.createTool(tool));
    }
}
```

- [ ] **Step 2: 运行测试确认失败**

```bash
mvn test -pl tooling-material-service -Dtest=ToolingMaterialServiceTest -q
```
期望：FAIL

- [ ] **Step 3: 创建 Entity 类**

```java
// src/main/java/com/mro/toolingmaterial/entity/ToolingItem.java
package com.mro.toolingmaterial.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("tooling_item")
public class ToolingItem {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String toolCode;
    private String toolName;
    private String toolType;
    private String specification;
    private Integer totalQty;
    private Integer availableQty;
    private String unit;
    private String location;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer isDeleted;
}
```

```java
// src/main/java/com/mro/toolingmaterial/entity/MaterialItem.java
package com.mro.toolingmaterial.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("material_item")
public class MaterialItem {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String partNo;
    private String partName;
    private String aircraftType;
    private String category;
    private Integer stockQty;
    private Integer minStock;
    private String unit;
    private BigDecimal unitPrice;
    private String supplier;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer isDeleted;
}
```

- [ ] **Step 4: 创建 Mapper**

```java
// src/main/java/com/mro/toolingmaterial/mapper/ToolingItemMapper.java
package com.mro.toolingmaterial.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mro.toolingmaterial.entity.ToolingItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ToolingItemMapper extends BaseMapper<ToolingItem> {

    @Select("SELECT COUNT(1) > 0 FROM tooling_item WHERE tool_code = #{toolCode} AND is_deleted = 0")
    boolean existsByToolCode(@Param("toolCode") String toolCode);

    @Update("UPDATE tooling_item SET available_qty = available_qty - #{qty} WHERE id = #{id} AND available_qty >= #{qty}")
    int decreaseAvailableQty(@Param("id") Long id, @Param("qty") int qty);

    @Update("UPDATE tooling_item SET available_qty = available_qty + #{qty} WHERE id = #{id}")
    int increaseAvailableQty(@Param("id") Long id, @Param("qty") int qty);
}
```

```java
// src/main/java/com/mro/toolingmaterial/mapper/MaterialItemMapper.java
package com.mro.toolingmaterial.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mro.toolingmaterial.entity.MaterialItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import java.util.List;

@Mapper
public interface MaterialItemMapper extends BaseMapper<MaterialItem> {

    @Select("SELECT COUNT(1) > 0 FROM material_item WHERE part_no = #{partNo} AND is_deleted = 0")
    boolean existsByPartNo(@Param("partNo") String partNo);

    @Select("SELECT * FROM material_item WHERE stock_qty <= min_stock AND is_deleted = 0")
    List<MaterialItem> findLowStockItems();

    @Update("UPDATE material_item SET stock_qty = stock_qty - #{qty} WHERE id = #{id} AND stock_qty >= #{qty}")
    int decreaseStock(@Param("id") Long id, @Param("qty") int qty);

    @Update("UPDATE material_item SET stock_qty = stock_qty + #{qty} WHERE id = #{id}")
    int increaseStock(@Param("id") Long id, @Param("qty") int qty);
}
```

- [ ] **Step 5: 创建 Service 类**

```java
// src/main/java/com/mro/toolingmaterial/service/ToolingItemService.java
package com.mro.toolingmaterial.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mro.toolingmaterial.entity.ToolingItem;
import com.mro.toolingmaterial.mapper.ToolingItemMapper;
import com.mro.common.core.exception.BusinessException;
import com.mro.common.core.result.ErrorCode;
import com.mro.common.core.result.PageResult;
import com.mro.common.dubbo.dto.tooling.ToolingItemDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ToolingItemService {

    private final ToolingItemMapper toolingMapper;

    public Long createTool(ToolingItem tool) {
        if (toolingMapper.existsByToolCode(tool.getToolCode())) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "工具编号已存在: " + tool.getToolCode());
        }
        if (tool.getTotalQty() == null || tool.getTotalQty() < 0) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "工具数量不能为负数");
        }
        tool.setStatus(1);
        tool.setCreateTime(LocalDateTime.now());
        toolingMapper.insert(tool);
        return tool.getId();
    }

    public void borrowTool(Long toolId, int qty) {
        int affected = toolingMapper.decreaseAvailableQty(toolId, qty);
        if (affected == 0) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "工具库存不足或工具不存在");
        }
    }

    public void returnTool(Long toolId, int qty) {
        toolingMapper.increaseAvailableQty(toolId, qty);
    }

    public PageResult<ToolingItemDTO> pageTools(String toolType, int pageNum, int pageSize) {
        Page<ToolingItem> page = new Page<>(pageNum, pageSize);
        toolingMapper.selectPage(page, new LambdaQueryWrapper<ToolingItem>()
                .eq(toolType != null, ToolingItem::getToolType, toolType)
                .eq(ToolingItem::getStatus, 1)
                .orderByAsc(ToolingItem::getToolCode));
        List<ToolingItemDTO> list = page.getRecords().stream().map(t ->
                new ToolingItemDTO(t.getId(), t.getToolCode(), t.getToolName(),
                        t.getToolType(), t.getSpecification(), t.getTotalQty(),
                        t.getAvailableQty(), t.getUnit(), t.getLocation(), t.getStatus())
        ).toList();
        return PageResult.of(list, page.getTotal(), pageNum, pageSize);
    }
}
```

```java
// src/main/java/com/mro/toolingmaterial/service/MaterialItemService.java
package com.mro.toolingmaterial.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mro.toolingmaterial.entity.MaterialItem;
import com.mro.toolingmaterial.mapper.MaterialItemMapper;
import com.mro.common.core.exception.BusinessException;
import com.mro.common.core.result.ErrorCode;
import com.mro.common.core.result.PageResult;
import com.mro.common.dubbo.dto.tooling.MaterialItemDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MaterialItemService {

    private final MaterialItemMapper materialMapper;

    public Long createMaterial(MaterialItem material) {
        if (materialMapper.existsByPartNo(material.getPartNo())) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "料件编号已存在: " + material.getPartNo());
        }
        material.setStatus(1);
        material.setCreateTime(LocalDateTime.now());
        materialMapper.insert(material);
        return material.getId();
    }

    public void consumeMaterial(Long materialId, int qty) {
        int affected = materialMapper.decreaseStock(materialId, qty);
        if (affected == 0) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "物料库存不足或物料不存在");
        }
    }

    public void replenishMaterial(Long materialId, int qty) {
        materialMapper.increaseStock(materialId, qty);
    }

    public List<MaterialItemDTO> getLowStockItems() {
        return materialMapper.findLowStockItems().stream().map(m ->
                new MaterialItemDTO(m.getId(), m.getPartNo(), m.getPartName(),
                        m.getAircraftType(), m.getCategory(), m.getStockQty(),
                        m.getMinStock(), m.getUnit(), m.getUnitPrice(), m.getSupplier(), m.getStatus())
        ).toList();
    }

    public PageResult<MaterialItemDTO> pageMaterials(String aircraftType, String category, int pageNum, int pageSize) {
        Page<MaterialItem> page = new Page<>(pageNum, pageSize);
        materialMapper.selectPage(page, new LambdaQueryWrapper<MaterialItem>()
                .eq(StringUtils.hasText(aircraftType), MaterialItem::getAircraftType, aircraftType)
                .eq(StringUtils.hasText(category), MaterialItem::getCategory, category)
                .eq(MaterialItem::getStatus, 1)
                .orderByAsc(MaterialItem::getPartNo));
        List<MaterialItemDTO> list = page.getRecords().stream().map(m ->
                new MaterialItemDTO(m.getId(), m.getPartNo(), m.getPartName(),
                        m.getAircraftType(), m.getCategory(), m.getStockQty(),
                        m.getMinStock(), m.getUnit(), m.getUnitPrice(), m.getSupplier(), m.getStatus())
        ).toList();
        return PageResult.of(list, page.getTotal(), pageNum, pageSize);
    }
}
```

- [ ] **Step 6: 创建 Dubbo Provider**

```java
// src/main/java/com/mro/toolingmaterial/dubbo/ToolingMaterialDubboServiceImpl.java
package com.mro.toolingmaterial.dubbo;

import com.mro.toolingmaterial.entity.MaterialItem;
import com.mro.toolingmaterial.entity.ToolingItem;
import com.mro.toolingmaterial.service.MaterialItemService;
import com.mro.toolingmaterial.service.ToolingItemService;
import com.mro.common.core.result.PageResult;
import com.mro.common.dubbo.dto.tooling.MaterialItemDTO;
import com.mro.common.dubbo.dto.tooling.ToolingItemDTO;
import com.mro.common.dubbo.service.ToolingMaterialDubboService;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;
import java.math.BigDecimal;
import java.util.List;

@DubboService(version = "1.0.0", group = "mro")
@RequiredArgsConstructor
public class ToolingMaterialDubboServiceImpl implements ToolingMaterialDubboService {

    private final ToolingItemService toolingService;
    private final MaterialItemService materialService;

    @Override
    public Long createTool(String toolCode, String toolName, String toolType,
                            String specification, Integer totalQty, String unit, String location) {
        ToolingItem tool = new ToolingItem();
        tool.setToolCode(toolCode);
        tool.setToolName(toolName);
        tool.setToolType(toolType);
        tool.setSpecification(specification);
        tool.setTotalQty(totalQty);
        tool.setAvailableQty(totalQty);
        tool.setUnit(unit);
        tool.setLocation(location);
        return toolingService.createTool(tool);
    }

    @Override
    public void borrowTool(Long toolId, int qty) {
        toolingService.borrowTool(toolId, qty);
    }

    @Override
    public void returnTool(Long toolId, int qty) {
        toolingService.returnTool(toolId, qty);
    }

    @Override
    public PageResult<ToolingItemDTO> pageTools(String toolType, int pageNum, int pageSize) {
        return toolingService.pageTools(toolType, pageNum, pageSize);
    }

    @Override
    public Long createMaterial(String partNo, String partName, String aircraftType, String category,
                                Integer stockQty, Integer minStock, String unit,
                                BigDecimal unitPrice, String supplier) {
        MaterialItem material = new MaterialItem();
        material.setPartNo(partNo);
        material.setPartName(partName);
        material.setAircraftType(aircraftType);
        material.setCategory(category);
        material.setStockQty(stockQty);
        material.setMinStock(minStock);
        material.setUnit(unit);
        material.setUnitPrice(unitPrice);
        material.setSupplier(supplier);
        return materialService.createMaterial(material);
    }

    @Override
    public void consumeMaterial(Long materialId, int qty) {
        materialService.consumeMaterial(materialId, qty);
    }

    @Override
    public void replenishMaterial(Long materialId, int qty) {
        materialService.replenishMaterial(materialId, qty);
    }

    @Override
    public List<MaterialItemDTO> getLowStockItems() {
        return materialService.getLowStockItems();
    }

    @Override
    public PageResult<MaterialItemDTO> pageMaterials(String aircraftType, String category, int pageNum, int pageSize) {
        return materialService.pageMaterials(aircraftType, category, pageNum, pageSize);
    }
}
```

- [ ] **Step 7: 运行测试**

```bash
mvn test -pl tooling-material-service -q
```
期望：BUILD SUCCESS

- [ ] **Step 8: 验证三个 BE-06 服务编译通过**

```bash
cd mro-backend
mvn compile -pl maintenance-manual-service,digital-twin-service,tooling-material-service -q
```
期望：BUILD SUCCESS

- [ ] **Step 9: Commit**

```bash
git add mro-backend/tooling-material-service/
git commit -m "feat: add tooling-material-service complete implementation

Refs: BE-06"
```

---

## 关键提示

**Dubbo 接口对齐：** 所有 Dubbo Provider 实现类方法签名必须与 `mro-common-dubbo` 中接口完全一致。以 BE-01 中定义的接口为准，若有差异调整实现类，不修改接口。

**DTO 类位置：** `ManualDTO`、`ManualSectionDTO`、`DigitalTwinModelDTO`、`DigitalTwinDataPointDTO`、`ToolingItemDTO`、`MaterialItemDTO` 均为 Java 21 Record，定义在 `mro-common-dubbo` 中，直接 import。

**端口规划：**
- maintenance-manual-service: HTTP 8088 / Dubbo 20886
- digital-twin-service: HTTP 8089 / Dubbo 20887
- tooling-material-service: HTTP 8090 / Dubbo 20888

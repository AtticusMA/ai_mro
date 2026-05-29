# BE-05: MRO 服务组1 — aircraft-health · ar-maintenance · fault-diagnosis 实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现 aircraft-health-service（飞机健康监测）、ar-maintenance-service（AR 维修任务协同）、fault-diagnosis-service（AI 故障诊断会话）三个 MRO 微服务的完整 Dubbo Provider 端，包含数据库 Schema、Flyway 迁移、MyBatis-Plus Mapper、Service 业务逻辑和 Dubbo 接口实现。

**Architecture:** 三个独立 Spring Boot 服务，各自持有独立 MySQL Schema（mro_aircraft_health / mro_ar_maintenance / mro_fault_diagnosis），均通过 Dubbo 暴露接口（已在 mro-common-dubbo 中定义），不对外暴露 HTTP（仅 manage-web BFF 层路由，见 BE-08）。

**Tech Stack:** Java 21 · Spring Boot 3.3 · Dubbo 3.3 · MyBatis-Plus 3.5 · Flyway 10 · MySQL 8 · Nacos · Maven Multi-module

**前置条件：** BE-01 完成（mro-common 模块可用）

---

## 文件结构

### aircraft-health-service
```
mro-backend/aircraft-health-service/
├── pom.xml
└── src/
    ├── main/
    │   ├── java/com/mro/aircrafthealth/
    │   │   ├── AircraftHealthApplication.java
    │   │   ├── entity/
    │   │   │   ├── AircraftHealthRecord.java
    │   │   │   └── AircraftHealthAlert.java
    │   │   ├── mapper/
    │   │   │   ├── AircraftHealthRecordMapper.java
    │   │   │   └── AircraftHealthAlertMapper.java
    │   │   ├── service/
    │   │   │   ├── AircraftHealthRecordService.java
    │   │   │   └── AircraftHealthAlertService.java
    │   │   └── dubbo/
    │   │       └── AircraftHealthDubboServiceImpl.java
    │   └── resources/
    │       ├── application.yml
    │       └── db/migration/
    │           └── V1__init_aircraft_health.sql
    └── test/java/com/mro/aircrafthealth/
        ├── service/AircraftHealthRecordServiceTest.java
        └── service/AircraftHealthAlertServiceTest.java
```

### ar-maintenance-service
```
mro-backend/ar-maintenance-service/
├── pom.xml
└── src/
    ├── main/
    │   ├── java/com/mro/armaintenance/
    │   │   ├── ArMaintenanceApplication.java
    │   │   ├── entity/
    │   │   │   ├── ArMaintenanceTask.java
    │   │   │   └── ArMaintenanceSession.java
    │   │   ├── mapper/
    │   │   │   ├── ArMaintenanceTaskMapper.java
    │   │   │   └── ArMaintenanceSessionMapper.java
    │   │   ├── service/
    │   │   │   ├── ArMaintenanceTaskService.java
    │   │   │   └── ArMaintenanceSessionService.java
    │   │   └── dubbo/
    │   │       └── ArMaintenanceDubboServiceImpl.java
    │   └── resources/
    │       ├── application.yml
    │       └── db/migration/
    │           └── V1__init_ar_maintenance.sql
    └── test/java/com/mro/armaintenance/
        └── service/ArMaintenanceTaskServiceTest.java
```

### fault-diagnosis-service
```
mro-backend/fault-diagnosis-service/
├── pom.xml
└── src/
    ├── main/
    │   ├── java/com/mro/faultdiagnosis/
    │   │   ├── FaultDiagnosisApplication.java
    │   │   ├── entity/
    │   │   │   ├── FaultDiagnosisSession.java
    │   │   │   └── FaultDiagnosisMessage.java
    │   │   ├── mapper/
    │   │   │   ├── FaultDiagnosisSessionMapper.java
    │   │   │   └── FaultDiagnosisMessageMapper.java
    │   │   ├── service/
    │   │   │   ├── FaultDiagnosisSessionService.java
    │   │   │   └── FaultDiagnosisMessageService.java
    │   │   └── dubbo/
    │   │       └── FaultDiagnosisDubboServiceImpl.java
    │   └── resources/
    │       ├── application.yml
    │       └── db/migration/
    │           └── V1__init_fault_diagnosis.sql
    └── test/java/com/mro/faultdiagnosis/
        └── service/FaultDiagnosisSessionServiceTest.java
```

---

## Task 1: aircraft-health-service — POM + 配置 + Schema

**Files:**
- Create: `mro-backend/aircraft-health-service/pom.xml`
- Create: `mro-backend/aircraft-health-service/src/main/java/com/mro/aircrafthealth/AircraftHealthApplication.java`
- Create: `mro-backend/aircraft-health-service/src/main/resources/application.yml`
- Create: `mro-backend/aircraft-health-service/src/main/resources/db/migration/V1__init_aircraft_health.sql`

- [ ] **Step 1: 创建 aircraft-health-service POM**

```xml
<!-- mro-backend/aircraft-health-service/pom.xml -->
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
  <artifactId>aircraft-health-service</artifactId>

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
      <groupId>com.mysql</groupId>
      <artifactId>mysql-connector-j</artifactId>
    </dependency>
    <dependency>
      <groupId>com.baomidou</groupId>
      <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
    </dependency>
    <dependency>
      <groupId>org.flywaydb</groupId>
      <artifactId>flyway-mysql</artifactId>
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

- [ ] **Step 2: 注册到父 POM modules 列表**

在 `mro-backend/pom.xml` 的 `<modules>` 节点中追加：
```xml
<module>aircraft-health-service</module>
```

- [ ] **Step 3: 创建启动类**

```java
// mro-backend/aircraft-health-service/src/main/java/com/mro/aircrafthealth/AircraftHealthApplication.java
package com.mro.aircrafthealth;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableDubbo
public class AircraftHealthApplication {
    public static void main(String[] args) {
        SpringApplication.run(AircraftHealthApplication.class, args);
    }
}
```

- [ ] **Step 4: 创建 application.yml**

```yaml
# mro-backend/aircraft-health-service/src/main/resources/application.yml
server:
  port: 8085

spring:
  application:
    name: aircraft-health-service
  threads:
    virtual:
      enabled: true
  datasource:
    url: jdbc:mysql://localhost:3306/mro_aircraft_health?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai
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
    name: aircraft-health-service
  registry:
    address: nacos://127.0.0.1:8848
    parameters:
      namespace: dev
  protocol:
    name: dubbo
    port: 20883
  provider:
    timeout: 5000
    group: mro
    version: 1.0.0

mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
    default-enum-type-handler: org.apache.ibatis.type.EnumOrdinalTypeHandler
  global-config:
    db-config:
      logic-delete-field: isDeleted
      logic-delete-value: 1
      logic-not-delete-value: 0
      id-type: auto
```

- [ ] **Step 5: 创建 Flyway Schema 迁移脚本**

```sql
-- mro-backend/aircraft-health-service/src/main/resources/db/migration/V1__init_aircraft_health.sql
CREATE DATABASE IF NOT EXISTS mro_aircraft_health DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE mro_aircraft_health;

CREATE TABLE IF NOT EXISTS aircraft_health_record (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    aircraft_no     VARCHAR(50)     NOT NULL COMMENT '飞机注册号',
    record_time     DATETIME        NOT NULL COMMENT '记录时间',
    health_score    DECIMAL(5,2)    NOT NULL COMMENT '健康评分(0-100)',
    status          TINYINT         NOT NULL DEFAULT 1 COMMENT '状态: 1=正常 2=预警 3=故障',
    component_data  JSON            COMMENT '部件健康数据(JSON)',
    remark          VARCHAR(500)    COMMENT '备注',
    create_by       BIGINT          COMMENT '创建人',
    create_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted      TINYINT         NOT NULL DEFAULT 0,
    INDEX idx_aircraft_no (aircraft_no),
    INDEX idx_record_time (record_time),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='飞机健康记录表';

CREATE TABLE IF NOT EXISTS aircraft_health_alert (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    aircraft_no     VARCHAR(50)     NOT NULL COMMENT '飞机注册号',
    alert_type      VARCHAR(100)    NOT NULL COMMENT '告警类型',
    alert_level     TINYINT         NOT NULL COMMENT '告警级别: 1=低 2=中 3=高 4=紧急',
    alert_msg       VARCHAR(1000)   NOT NULL COMMENT '告警信息',
    component_code  VARCHAR(100)    COMMENT '部件代码',
    status          TINYINT         NOT NULL DEFAULT 1 COMMENT '状态: 1=未处理 2=处理中 3=已关闭',
    handle_by       BIGINT          COMMENT '处理人ID',
    handle_time     DATETIME        COMMENT '处理时间',
    handle_remark   VARCHAR(500)    COMMENT '处理备注',
    create_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted      TINYINT         NOT NULL DEFAULT 0,
    INDEX idx_aircraft_no (aircraft_no),
    INDEX idx_alert_level (alert_level),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='飞机健康告警表';
```

- [ ] **Step 6: 确认 Schema 迁移脚本语法正确（代码审查，无需运行）**

- [ ] **Step 7: Commit**

```bash
git add mro-backend/aircraft-health-service/
git commit -m "feat: add aircraft-health-service skeleton with DB schema

Refs: BE-05"
```

---

## Task 2: aircraft-health-service — Entity + Mapper + Service

**Files:**
- Create: `mro-backend/aircraft-health-service/src/main/java/com/mro/aircrafthealth/entity/AircraftHealthRecord.java`
- Create: `mro-backend/aircraft-health-service/src/main/java/com/mro/aircrafthealth/entity/AircraftHealthAlert.java`
- Create: `mro-backend/aircraft-health-service/src/main/java/com/mro/aircrafthealth/mapper/AircraftHealthRecordMapper.java`
- Create: `mro-backend/aircraft-health-service/src/main/java/com/mro/aircrafthealth/mapper/AircraftHealthAlertMapper.java`
- Create: `mro-backend/aircraft-health-service/src/main/java/com/mro/aircrafthealth/service/AircraftHealthRecordService.java`
- Create: `mro-backend/aircraft-health-service/src/main/java/com/mro/aircrafthealth/service/AircraftHealthAlertService.java`
- Test: `mro-backend/aircraft-health-service/src/test/java/com/mro/aircrafthealth/service/AircraftHealthRecordServiceTest.java`

- [ ] **Step 1: 写失败的单元测试**

```java
// src/test/java/com/mro/aircrafthealth/service/AircraftHealthRecordServiceTest.java
package com.mro.aircrafthealth.service;

import com.mro.aircrafthealth.entity.AircraftHealthRecord;
import com.mro.aircrafthealth.mapper.AircraftHealthRecordMapper;
import com.mro.common.core.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AircraftHealthRecordServiceTest {

    @Mock
    private AircraftHealthRecordMapper recordMapper;

    @InjectMocks
    private AircraftHealthRecordService recordService;

    private AircraftHealthRecord validRecord;

    @BeforeEach
    void setUp() {
        validRecord = new AircraftHealthRecord();
        validRecord.setAircraftNo("B-1234");
        validRecord.setRecordTime(LocalDateTime.now());
        validRecord.setHealthScore(new BigDecimal("85.50"));
        validRecord.setStatus(1);
    }

    @Test
    void test_saveRecord_validInput_returnsSavedId() {
        when(recordMapper.insert(any())).thenAnswer(inv -> {
            AircraftHealthRecord r = inv.getArgument(0);
            r.setId(1L);
            return 1;
        });

        Long id = recordService.saveRecord(validRecord);

        assertNotNull(id);
        assertEquals(1L, id);
        verify(recordMapper, times(1)).insert(validRecord);
    }

    @Test
    void test_saveRecord_nullAircraftNo_throwsBusinessException() {
        validRecord.setAircraftNo(null);

        assertThrows(BusinessException.class, () -> recordService.saveRecord(validRecord));
        verify(recordMapper, never()).insert(any());
    }

    @Test
    void test_saveRecord_invalidHealthScore_throwsBusinessException() {
        validRecord.setHealthScore(new BigDecimal("150.00")); // > 100

        assertThrows(BusinessException.class, () -> recordService.saveRecord(validRecord));
        verify(recordMapper, never()).insert(any());
    }
}
```

- [ ] **Step 2: 运行测试确认失败**

```bash
cd mro-backend
mvn test -pl aircraft-health-service -Dtest=AircraftHealthRecordServiceTest -q
```
期望：FAIL — `AircraftHealthRecordService` 类不存在

- [ ] **Step 3: 创建 Entity 类**

```java
// src/main/java/com/mro/aircrafthealth/entity/AircraftHealthRecord.java
package com.mro.aircrafthealth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("aircraft_health_record")
public class AircraftHealthRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String aircraftNo;
    private LocalDateTime recordTime;
    private BigDecimal healthScore;
    private Integer status;
    private String componentData;
    private String remark;
    private Long createBy;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer isDeleted;
}
```

```java
// src/main/java/com/mro/aircrafthealth/entity/AircraftHealthAlert.java
package com.mro.aircrafthealth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("aircraft_health_alert")
public class AircraftHealthAlert {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String aircraftNo;
    private String alertType;
    private Integer alertLevel;
    private String alertMsg;
    private String componentCode;
    private Integer status;
    private Long handleBy;
    private LocalDateTime handleTime;
    private String handleRemark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer isDeleted;
}
```

- [ ] **Step 4: 创建 Mapper 接口**

```java
// src/main/java/com/mro/aircrafthealth/mapper/AircraftHealthRecordMapper.java
package com.mro.aircrafthealth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mro.aircrafthealth.entity.AircraftHealthRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AircraftHealthRecordMapper extends BaseMapper<AircraftHealthRecord> {

    @Select("SELECT * FROM aircraft_health_record WHERE aircraft_no = #{aircraftNo} " +
            "AND is_deleted = 0 ORDER BY record_time DESC")
    IPage<AircraftHealthRecord> pageByAircraftNo(Page<AircraftHealthRecord> page,
                                                  @Param("aircraftNo") String aircraftNo);
}
```

```java
// src/main/java/com/mro/aircrafthealth/mapper/AircraftHealthAlertMapper.java
package com.mro.aircrafthealth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mro.aircrafthealth.entity.AircraftHealthAlert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface AircraftHealthAlertMapper extends BaseMapper<AircraftHealthAlert> {

    @Select("SELECT * FROM aircraft_health_alert WHERE aircraft_no = #{aircraftNo} " +
            "AND status = 1 AND is_deleted = 0 ORDER BY alert_level DESC, create_time DESC")
    List<AircraftHealthAlert> findActiveAlertsByAircraftNo(@Param("aircraftNo") String aircraftNo);
}
```

- [ ] **Step 5: 创建 Service 类**

```java
// src/main/java/com/mro/aircrafthealth/service/AircraftHealthRecordService.java
package com.mro.aircrafthealth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mro.aircrafthealth.entity.AircraftHealthRecord;
import com.mro.aircrafthealth.mapper.AircraftHealthRecordMapper;
import com.mro.common.core.exception.BusinessException;
import com.mro.common.core.result.ErrorCode;
import com.mro.common.core.result.PageResult;
import com.mro.common.dubbo.dto.aircraft.AircraftHealthRecordDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AircraftHealthRecordService {

    private final AircraftHealthRecordMapper recordMapper;

    public Long saveRecord(AircraftHealthRecord record) {
        if (!StringUtils.hasText(record.getAircraftNo())) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "飞机注册号不能为空");
        }
        if (record.getHealthScore() == null
                || record.getHealthScore().compareTo(BigDecimal.ZERO) < 0
                || record.getHealthScore().compareTo(new BigDecimal("100")) > 0) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "健康评分必须在0-100之间");
        }
        record.setCreateTime(LocalDateTime.now());
        recordMapper.insert(record);
        return record.getId();
    }

    public AircraftHealthRecordDTO getLatestByAircraftNo(String aircraftNo) {
        AircraftHealthRecord record = recordMapper.selectOne(
                new LambdaQueryWrapper<AircraftHealthRecord>()
                        .eq(AircraftHealthRecord::getAircraftNo, aircraftNo)
                        .orderByDesc(AircraftHealthRecord::getRecordTime)
                        .last("LIMIT 1")
        );
        if (record == null) return null;
        return toDTO(record);
    }

    public PageResult<AircraftHealthRecordDTO> pageByAircraftNo(String aircraftNo, int pageNum, int pageSize) {
        Page<AircraftHealthRecord> page = new Page<>(pageNum, pageSize);
        recordMapper.pageByAircraftNo(page, aircraftNo);
        List<AircraftHealthRecordDTO> list = page.getRecords().stream().map(this::toDTO).toList();
        return PageResult.of(list, page.getTotal(), pageNum, pageSize);
    }

    private AircraftHealthRecordDTO toDTO(AircraftHealthRecord r) {
        return new AircraftHealthRecordDTO(
                r.getId(), r.getAircraftNo(), r.getRecordTime(),
                r.getHealthScore(), r.getStatus(), r.getComponentData(), r.getRemark()
        );
    }
}
```

```java
// src/main/java/com/mro/aircrafthealth/service/AircraftHealthAlertService.java
package com.mro.aircrafthealth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mro.aircrafthealth.entity.AircraftHealthAlert;
import com.mro.aircrafthealth.mapper.AircraftHealthAlertMapper;
import com.mro.common.core.exception.BusinessException;
import com.mro.common.core.result.ErrorCode;
import com.mro.common.dubbo.dto.aircraft.AircraftHealthAlertDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AircraftHealthAlertService {

    private final AircraftHealthAlertMapper alertMapper;

    public Long createAlert(AircraftHealthAlert alert) {
        if (!StringUtils.hasText(alert.getAircraftNo())) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "飞机注册号不能为空");
        }
        if (alert.getAlertLevel() == null || alert.getAlertLevel() < 1 || alert.getAlertLevel() > 4) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "告警级别必须在1-4之间");
        }
        alert.setStatus(1);
        alert.setCreateTime(LocalDateTime.now());
        alertMapper.insert(alert);
        return alert.getId();
    }

    public List<AircraftHealthAlertDTO> listActiveAlerts(String aircraftNo) {
        List<AircraftHealthAlert> alerts = alertMapper.findActiveAlertsByAircraftNo(aircraftNo);
        return alerts.stream().map(this::toDTO).toList();
    }

    public void closeAlert(Long alertId, Long handleBy, String handleRemark) {
        AircraftHealthAlert alert = alertMapper.selectById(alertId);
        if (alert == null) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "告警记录不存在");
        }
        alert.setStatus(3);
        alert.setHandleBy(handleBy);
        alert.setHandleTime(LocalDateTime.now());
        alert.setHandleRemark(handleRemark);
        alertMapper.updateById(alert);
    }

    private AircraftHealthAlertDTO toDTO(AircraftHealthAlert a) {
        return new AircraftHealthAlertDTO(
                a.getId(), a.getAircraftNo(), a.getAlertType(),
                a.getAlertLevel(), a.getAlertMsg(), a.getComponentCode(),
                a.getStatus(), a.getHandleBy(), a.getHandleTime()
        );
    }
}
```

- [ ] **Step 6: 运行测试确认通过**

```bash
cd mro-backend
mvn test -pl aircraft-health-service -Dtest=AircraftHealthRecordServiceTest -q
```
期望：BUILD SUCCESS，3/3 tests passed

- [ ] **Step 7: Commit**

```bash
git add mro-backend/aircraft-health-service/src/
git commit -m "feat: add aircraft-health entity/mapper/service

Refs: BE-05"
```

---

## Task 3: aircraft-health-service — Dubbo Provider 实现

**Files:**
- Create: `mro-backend/aircraft-health-service/src/main/java/com/mro/aircrafthealth/dubbo/AircraftHealthDubboServiceImpl.java`
- Test: `mro-backend/aircraft-health-service/src/test/java/com/mro/aircrafthealth/service/AircraftHealthAlertServiceTest.java`

- [ ] **Step 1: 写告警 Service 测试**

```java
// src/test/java/com/mro/aircrafthealth/service/AircraftHealthAlertServiceTest.java
package com.mro.aircrafthealth.service;

import com.mro.aircrafthealth.entity.AircraftHealthAlert;
import com.mro.aircrafthealth.mapper.AircraftHealthAlertMapper;
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
class AircraftHealthAlertServiceTest {

    @Mock
    private AircraftHealthAlertMapper alertMapper;

    @InjectMocks
    private AircraftHealthAlertService alertService;

    @Test
    void test_createAlert_validInput_returnsId() {
        when(alertMapper.insert(any())).thenAnswer(inv -> {
            AircraftHealthAlert a = inv.getArgument(0);
            a.setId(10L);
            return 1;
        });

        AircraftHealthAlert alert = new AircraftHealthAlert();
        alert.setAircraftNo("B-9999");
        alert.setAlertType("ENGINE_TEMP");
        alert.setAlertLevel(3);
        alert.setAlertMsg("发动机温度异常");

        Long id = alertService.createAlert(alert);
        assertEquals(10L, id);
        verify(alertMapper).insert(alert);
    }

    @Test
    void test_createAlert_invalidLevel_throwsBusinessException() {
        AircraftHealthAlert alert = new AircraftHealthAlert();
        alert.setAircraftNo("B-9999");
        alert.setAlertLevel(5); // invalid

        assertThrows(BusinessException.class, () -> alertService.createAlert(alert));
        verify(alertMapper, never()).insert(any());
    }

    @Test
    void test_closeAlert_nonExistentId_throwsBusinessException() {
        when(alertMapper.selectById(999L)).thenReturn(null);

        assertThrows(BusinessException.class, () -> alertService.closeAlert(999L, 1L, "test"));
    }
}
```

- [ ] **Step 2: 运行测试确认失败**

```bash
mvn test -pl aircraft-health-service -Dtest=AircraftHealthAlertServiceTest -q
```
期望：FAIL

- [ ] **Step 3: 创建 Dubbo Provider**

```java
// src/main/java/com/mro/aircrafthealth/dubbo/AircraftHealthDubboServiceImpl.java
package com.mro.aircrafthealth.dubbo;

import com.mro.aircrafthealth.entity.AircraftHealthAlert;
import com.mro.aircrafthealth.entity.AircraftHealthRecord;
import com.mro.aircrafthealth.service.AircraftHealthAlertService;
import com.mro.aircrafthealth.service.AircraftHealthRecordService;
import com.mro.common.core.result.PageResult;
import com.mro.common.dubbo.dto.aircraft.AircraftHealthAlertDTO;
import com.mro.common.dubbo.dto.aircraft.AircraftHealthRecordDTO;
import com.mro.common.dubbo.service.AircraftHealthDubboService;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@DubboService(version = "1.0.0", group = "mro")
@RequiredArgsConstructor
public class AircraftHealthDubboServiceImpl implements AircraftHealthDubboService {

    private final AircraftHealthRecordService recordService;
    private final AircraftHealthAlertService alertService;

    @Override
    public Long saveHealthRecord(String aircraftNo, BigDecimal healthScore,
                                  Integer status, String componentData, String remark) {
        AircraftHealthRecord record = new AircraftHealthRecord();
        record.setAircraftNo(aircraftNo);
        record.setRecordTime(LocalDateTime.now());
        record.setHealthScore(healthScore);
        record.setStatus(status);
        record.setComponentData(componentData);
        record.setRemark(remark);
        return recordService.saveRecord(record);
    }

    @Override
    public AircraftHealthRecordDTO getLatestRecord(String aircraftNo) {
        return recordService.getLatestByAircraftNo(aircraftNo);
    }

    @Override
    public PageResult<AircraftHealthRecordDTO> pageRecords(String aircraftNo, int pageNum, int pageSize) {
        return recordService.pageByAircraftNo(aircraftNo, pageNum, pageSize);
    }

    @Override
    public Long createAlert(String aircraftNo, String alertType, Integer alertLevel,
                             String alertMsg, String componentCode) {
        AircraftHealthAlert alert = new AircraftHealthAlert();
        alert.setAircraftNo(aircraftNo);
        alert.setAlertType(alertType);
        alert.setAlertLevel(alertLevel);
        alert.setAlertMsg(alertMsg);
        alert.setComponentCode(componentCode);
        return alertService.createAlert(alert);
    }

    @Override
    public List<AircraftHealthAlertDTO> listActiveAlerts(String aircraftNo) {
        return alertService.listActiveAlerts(aircraftNo);
    }

    @Override
    public void closeAlert(Long alertId, Long handleBy, String handleRemark) {
        alertService.closeAlert(alertId, handleBy, handleRemark);
    }
}
```

**注意：** `AircraftHealthDubboService` 接口在 `mro-common-dubbo` 中已定义（BE-01 Task 6）。若接口方法签名与上方实现不匹配，以 BE-01 中 mro-common-dubbo 里的接口定义为准，调整此实现类的方法签名。

- [ ] **Step 4: 运行所有 aircraft-health 测试**

```bash
mvn test -pl aircraft-health-service -q
```
期望：BUILD SUCCESS

- [ ] **Step 5: Commit**

```bash
git add mro-backend/aircraft-health-service/src/main/java/com/mro/aircrafthealth/dubbo/
git add mro-backend/aircraft-health-service/src/test/
git commit -m "feat: add aircraft-health dubbo provider implementation

Refs: BE-05"
```

---

## Task 4: ar-maintenance-service — POM + 配置 + Schema

**Files:**
- Create: `mro-backend/ar-maintenance-service/pom.xml`
- Create: `mro-backend/ar-maintenance-service/src/main/java/com/mro/armaintenance/ArMaintenanceApplication.java`
- Create: `mro-backend/ar-maintenance-service/src/main/resources/application.yml`
- Create: `mro-backend/ar-maintenance-service/src/main/resources/db/migration/V1__init_ar_maintenance.sql`

- [ ] **Step 1: 创建 ar-maintenance-service POM**

```xml
<!-- mro-backend/ar-maintenance-service/pom.xml -->
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
  <artifactId>ar-maintenance-service</artifactId>

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
<module>ar-maintenance-service</module>
```

- [ ] **Step 3: 创建启动类**

```java
// src/main/java/com/mro/armaintenance/ArMaintenanceApplication.java
package com.mro.armaintenance;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableDubbo
public class ArMaintenanceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ArMaintenanceApplication.class, args);
    }
}
```

- [ ] **Step 4: 创建 application.yml**

```yaml
server:
  port: 8086

spring:
  application:
    name: ar-maintenance-service
  threads:
    virtual:
      enabled: true
  datasource:
    url: jdbc:mysql://localhost:3306/mro_ar_maintenance?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai
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
    name: ar-maintenance-service
  registry:
    address: nacos://127.0.0.1:8848
    parameters:
      namespace: dev
  protocol:
    name: dubbo
    port: 20884
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
-- V1__init_ar_maintenance.sql
CREATE DATABASE IF NOT EXISTS mro_ar_maintenance DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE mro_ar_maintenance;

CREATE TABLE IF NOT EXISTS ar_maintenance_task (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_no         VARCHAR(50)     NOT NULL UNIQUE COMMENT 'AR任务编号',
    aircraft_no     VARCHAR(50)     NOT NULL COMMENT '飞机注册号',
    task_title      VARCHAR(200)    NOT NULL COMMENT '任务标题',
    task_type       VARCHAR(50)     NOT NULL COMMENT '任务类型(INSPECTION/REPAIR/REPLACE)',
    priority        TINYINT         NOT NULL DEFAULT 2 COMMENT '优先级 1=低 2=中 3=高',
    status          TINYINT         NOT NULL DEFAULT 1 COMMENT '状态 1=待分配 2=进行中 3=已完成 4=已取消',
    assigned_user   BIGINT          COMMENT '指派工程师ID',
    plan_start_time DATETIME        COMMENT '计划开始时间',
    plan_end_time   DATETIME        COMMENT '计划结束时间',
    actual_start    DATETIME        COMMENT '实际开始时间',
    actual_end      DATETIME        COMMENT '实际结束时间',
    ar_guide_url    VARCHAR(500)    COMMENT 'AR引导资源URL',
    description     TEXT            COMMENT '任务描述',
    create_by       BIGINT,
    create_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted      TINYINT         NOT NULL DEFAULT 0,
    INDEX idx_aircraft_no (aircraft_no),
    INDEX idx_status (status),
    INDEX idx_assigned_user (assigned_user)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AR维修任务表';

CREATE TABLE IF NOT EXISTS ar_maintenance_session (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_id         BIGINT          NOT NULL COMMENT '关联任务ID',
    session_token   VARCHAR(100)    NOT NULL UNIQUE COMMENT 'AR会话Token',
    user_id         BIGINT          NOT NULL COMMENT '操作用户ID',
    device_type     VARCHAR(50)     COMMENT 'AR设备类型',
    status          TINYINT         NOT NULL DEFAULT 1 COMMENT '状态 1=活跃 2=已结束',
    start_time      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    end_time        DATETIME,
    session_data    JSON            COMMENT '会话操作记录',
    create_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted      TINYINT         NOT NULL DEFAULT 0,
    INDEX idx_task_id (task_id),
    INDEX idx_user_id (user_id),
    INDEX idx_session_token (session_token)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AR维修会话表';
```

- [ ] **Step 6: Commit**

```bash
git add mro-backend/ar-maintenance-service/
git commit -m "feat: add ar-maintenance-service skeleton with DB schema

Refs: BE-05"
```

---

## Task 5: ar-maintenance-service — Entity + Mapper + Service + Dubbo Provider

**Files:**
- Create: entity, mapper, service, dubbo impl files (见文件结构)
- Test: `src/test/java/com/mro/armaintenance/service/ArMaintenanceTaskServiceTest.java`

- [ ] **Step 1: 写失败测试**

```java
// src/test/java/com/mro/armaintenance/service/ArMaintenanceTaskServiceTest.java
package com.mro.armaintenance.service;

import com.mro.armaintenance.entity.ArMaintenanceTask;
import com.mro.armaintenance.mapper.ArMaintenanceTaskMapper;
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
class ArMaintenanceTaskServiceTest {

    @Mock
    private ArMaintenanceTaskMapper taskMapper;

    @InjectMocks
    private ArMaintenanceTaskService taskService;

    @Test
    void test_createTask_validInput_returnsId() {
        when(taskMapper.existsByTaskNo("TASK-001")).thenReturn(false);
        when(taskMapper.insert(any())).thenAnswer(inv -> {
            ArMaintenanceTask t = inv.getArgument(0);
            t.setId(1L);
            return 1;
        });

        ArMaintenanceTask task = new ArMaintenanceTask();
        task.setTaskNo("TASK-001");
        task.setAircraftNo("B-1234");
        task.setTaskTitle("发动机检查");
        task.setTaskType("INSPECTION");
        task.setPriority(2);

        Long id = taskService.createTask(task, 1L);
        assertEquals(1L, id);
    }

    @Test
    void test_createTask_duplicateTaskNo_throwsBusinessException() {
        when(taskMapper.existsByTaskNo("TASK-001")).thenReturn(true);

        ArMaintenanceTask task = new ArMaintenanceTask();
        task.setTaskNo("TASK-001");
        task.setAircraftNo("B-1234");
        task.setTaskTitle("发动机检查");
        task.setTaskType("INSPECTION");
        task.setPriority(2);

        assertThrows(BusinessException.class, () -> taskService.createTask(task, 1L));
        verify(taskMapper, never()).insert(any());
    }

    @Test
    void test_assignTask_validUser_updatesStatus() {
        ArMaintenanceTask existing = new ArMaintenanceTask();
        existing.setId(1L);
        existing.setStatus(1);
        when(taskMapper.selectById(1L)).thenReturn(existing);
        when(taskMapper.updateById(any())).thenReturn(1);

        taskService.assignTask(1L, 5L);

        verify(taskMapper).updateById(argThat(t ->
                t.getStatus().equals(2) && t.getAssignedUser().equals(5L)
        ));
    }
}
```

- [ ] **Step 2: 运行测试确认失败**

```bash
mvn test -pl ar-maintenance-service -Dtest=ArMaintenanceTaskServiceTest -q
```
期望：FAIL

- [ ] **Step 3: 创建 Entity 类**

```java
// src/main/java/com/mro/armaintenance/entity/ArMaintenanceTask.java
package com.mro.armaintenance.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("ar_maintenance_task")
public class ArMaintenanceTask {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String taskNo;
    private String aircraftNo;
    private String taskTitle;
    private String taskType;
    private Integer priority;
    private Integer status;
    private Long assignedUser;
    private LocalDateTime planStartTime;
    private LocalDateTime planEndTime;
    private LocalDateTime actualStart;
    private LocalDateTime actualEnd;
    private String arGuideUrl;
    private String description;
    private Long createBy;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer isDeleted;
}
```

```java
// src/main/java/com/mro/armaintenance/entity/ArMaintenanceSession.java
package com.mro.armaintenance.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("ar_maintenance_session")
public class ArMaintenanceSession {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long taskId;
    private String sessionToken;
    private Long userId;
    private String deviceType;
    private Integer status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String sessionData;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer isDeleted;
}
```

- [ ] **Step 4: 创建 Mapper**

```java
// src/main/java/com/mro/armaintenance/mapper/ArMaintenanceTaskMapper.java
package com.mro.armaintenance.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mro.armaintenance.entity.ArMaintenanceTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ArMaintenanceTaskMapper extends BaseMapper<ArMaintenanceTask> {

    @Select("SELECT COUNT(1) > 0 FROM ar_maintenance_task WHERE task_no = #{taskNo} AND is_deleted = 0")
    boolean existsByTaskNo(@Param("taskNo") String taskNo);
}
```

```java
// src/main/java/com/mro/armaintenance/mapper/ArMaintenanceSessionMapper.java
package com.mro.armaintenance.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mro.armaintenance.entity.ArMaintenanceSession;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface ArMaintenanceSessionMapper extends BaseMapper<ArMaintenanceSession> {

    @Select("SELECT * FROM ar_maintenance_session WHERE task_id = #{taskId} AND is_deleted = 0 ORDER BY start_time DESC")
    List<ArMaintenanceSession> findByTaskId(@Param("taskId") Long taskId);
}
```

- [ ] **Step 5: 创建 Service**

```java
// src/main/java/com/mro/armaintenance/service/ArMaintenanceTaskService.java
package com.mro.armaintenance.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mro.armaintenance.entity.ArMaintenanceTask;
import com.mro.armaintenance.mapper.ArMaintenanceTaskMapper;
import com.mro.common.core.exception.BusinessException;
import com.mro.common.core.result.ErrorCode;
import com.mro.common.core.result.PageResult;
import com.mro.common.dubbo.dto.armaintenance.ArMaintenanceTaskDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ArMaintenanceTaskService {

    private final ArMaintenanceTaskMapper taskMapper;

    public Long createTask(ArMaintenanceTask task, Long createBy) {
        if (taskMapper.existsByTaskNo(task.getTaskNo())) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "AR任务编号已存在: " + task.getTaskNo());
        }
        task.setStatus(1);
        task.setCreateBy(createBy);
        task.setCreateTime(LocalDateTime.now());
        taskMapper.insert(task);
        return task.getId();
    }

    public void assignTask(Long taskId, Long assignedUser) {
        ArMaintenanceTask task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "AR任务不存在");
        }
        task.setAssignedUser(assignedUser);
        task.setStatus(2);
        taskMapper.updateById(task);
    }

    public void completeTask(Long taskId) {
        ArMaintenanceTask task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "AR任务不存在");
        }
        task.setStatus(3);
        task.setActualEnd(LocalDateTime.now());
        taskMapper.updateById(task);
    }

    public PageResult<ArMaintenanceTaskDTO> pageByAircraftNo(String aircraftNo, Integer status, int pageNum, int pageSize) {
        Page<ArMaintenanceTask> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ArMaintenanceTask> wrapper = new LambdaQueryWrapper<ArMaintenanceTask>()
                .eq(aircraftNo != null, ArMaintenanceTask::getAircraftNo, aircraftNo)
                .eq(status != null, ArMaintenanceTask::getStatus, status)
                .orderByDesc(ArMaintenanceTask::getCreateTime);
        taskMapper.selectPage(page, wrapper);
        List<ArMaintenanceTaskDTO> list = page.getRecords().stream().map(this::toDTO).toList();
        return PageResult.of(list, page.getTotal(), pageNum, pageSize);
    }

    private ArMaintenanceTaskDTO toDTO(ArMaintenanceTask t) {
        return new ArMaintenanceTaskDTO(
                t.getId(), t.getTaskNo(), t.getAircraftNo(), t.getTaskTitle(),
                t.getTaskType(), t.getPriority(), t.getStatus(), t.getAssignedUser(),
                t.getPlanStartTime(), t.getPlanEndTime(), t.getArGuideUrl()
        );
    }
}
```

```java
// src/main/java/com/mro/armaintenance/service/ArMaintenanceSessionService.java
package com.mro.armaintenance.service;

import com.mro.armaintenance.entity.ArMaintenanceSession;
import com.mro.armaintenance.mapper.ArMaintenanceSessionMapper;
import com.mro.common.dubbo.dto.armaintenance.ArMaintenanceSessionDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ArMaintenanceSessionService {

    private final ArMaintenanceSessionMapper sessionMapper;

    public String startSession(Long taskId, Long userId, String deviceType) {
        ArMaintenanceSession session = new ArMaintenanceSession();
        session.setTaskId(taskId);
        session.setUserId(userId);
        session.setDeviceType(deviceType);
        session.setSessionToken(UUID.randomUUID().toString().replace("-", ""));
        session.setStatus(1);
        session.setStartTime(LocalDateTime.now());
        sessionMapper.insert(session);
        return session.getSessionToken();
    }

    public void endSession(String sessionToken) {
        ArMaintenanceSession session = sessionMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ArMaintenanceSession>()
                        .eq(ArMaintenanceSession::getSessionToken, sessionToken)
        );
        if (session != null) {
            session.setStatus(2);
            session.setEndTime(LocalDateTime.now());
            sessionMapper.updateById(session);
        }
    }

    public List<ArMaintenanceSessionDTO> listByTaskId(Long taskId) {
        return sessionMapper.findByTaskId(taskId).stream().map(s ->
                new ArMaintenanceSessionDTO(s.getId(), s.getTaskId(), s.getSessionToken(),
                        s.getUserId(), s.getDeviceType(), s.getStatus(), s.getStartTime(), s.getEndTime())
        ).toList();
    }
}
```

- [ ] **Step 6: 创建 Dubbo Provider**

```java
// src/main/java/com/mro/armaintenance/dubbo/ArMaintenanceDubboServiceImpl.java
package com.mro.armaintenance.dubbo;

import com.mro.armaintenance.entity.ArMaintenanceTask;
import com.mro.armaintenance.service.ArMaintenanceSessionService;
import com.mro.armaintenance.service.ArMaintenanceTaskService;
import com.mro.common.core.result.PageResult;
import com.mro.common.dubbo.dto.armaintenance.ArMaintenanceSessionDTO;
import com.mro.common.dubbo.dto.armaintenance.ArMaintenanceTaskDTO;
import com.mro.common.dubbo.service.ArMaintenanceDubboService;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;
import java.time.LocalDateTime;
import java.util.List;

@DubboService(version = "1.0.0", group = "mro")
@RequiredArgsConstructor
public class ArMaintenanceDubboServiceImpl implements ArMaintenanceDubboService {

    private final ArMaintenanceTaskService taskService;
    private final ArMaintenanceSessionService sessionService;

    @Override
    public Long createTask(String taskNo, String aircraftNo, String taskTitle,
                            String taskType, Integer priority, String arGuideUrl,
                            String description, Long createBy) {
        ArMaintenanceTask task = new ArMaintenanceTask();
        task.setTaskNo(taskNo);
        task.setAircraftNo(aircraftNo);
        task.setTaskTitle(taskTitle);
        task.setTaskType(taskType);
        task.setPriority(priority);
        task.setArGuideUrl(arGuideUrl);
        task.setDescription(description);
        return taskService.createTask(task, createBy);
    }

    @Override
    public void assignTask(Long taskId, Long assignedUser) {
        taskService.assignTask(taskId, assignedUser);
    }

    @Override
    public void completeTask(Long taskId) {
        taskService.completeTask(taskId);
    }

    @Override
    public PageResult<ArMaintenanceTaskDTO> pageByAircraftNo(String aircraftNo, Integer status, int pageNum, int pageSize) {
        return taskService.pageByAircraftNo(aircraftNo, status, pageNum, pageSize);
    }

    @Override
    public String startSession(Long taskId, Long userId, String deviceType) {
        return sessionService.startSession(taskId, userId, deviceType);
    }

    @Override
    public void endSession(String sessionToken) {
        sessionService.endSession(sessionToken);
    }

    @Override
    public List<ArMaintenanceSessionDTO> listSessions(Long taskId) {
        return sessionService.listByTaskId(taskId);
    }
}
```

- [ ] **Step 7: 运行测试**

```bash
mvn test -pl ar-maintenance-service -q
```
期望：BUILD SUCCESS

- [ ] **Step 8: Commit**

```bash
git add mro-backend/ar-maintenance-service/
git commit -m "feat: add ar-maintenance-service complete implementation

Refs: BE-05"
```

---

## Task 6: fault-diagnosis-service — POM + 配置 + Schema

**Files:**
- Create: `mro-backend/fault-diagnosis-service/pom.xml`
- Create: `mro-backend/fault-diagnosis-service/src/main/java/com/mro/faultdiagnosis/FaultDiagnosisApplication.java`
- Create: `mro-backend/fault-diagnosis-service/src/main/resources/application.yml`
- Create: `mro-backend/fault-diagnosis-service/src/main/resources/db/migration/V1__init_fault_diagnosis.sql`

- [ ] **Step 1: 创建 POM**

```xml
<!-- mro-backend/fault-diagnosis-service/pom.xml -->
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
  <artifactId>fault-diagnosis-service</artifactId>

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
<module>fault-diagnosis-service</module>
```

- [ ] **Step 3: 创建启动类**

```java
package com.mro.faultdiagnosis;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableDubbo
public class FaultDiagnosisApplication {
    public static void main(String[] args) {
        SpringApplication.run(FaultDiagnosisApplication.class, args);
    }
}
```

- [ ] **Step 4: 创建 application.yml**

```yaml
server:
  port: 8087

spring:
  application:
    name: fault-diagnosis-service
  threads:
    virtual:
      enabled: true
  datasource:
    url: jdbc:mysql://localhost:3306/mro_fault_diagnosis?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai
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
    name: fault-diagnosis-service
  registry:
    address: nacos://127.0.0.1:8848
    parameters:
      namespace: dev
  protocol:
    name: dubbo
    port: 20885
  provider:
    timeout: 10000
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
-- V1__init_fault_diagnosis.sql
CREATE DATABASE IF NOT EXISTS mro_fault_diagnosis DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE mro_fault_diagnosis;

CREATE TABLE IF NOT EXISTS fault_diagnosis_session (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_no      VARCHAR(50)     NOT NULL UNIQUE COMMENT '会话编号',
    user_id         BIGINT          NOT NULL COMMENT '用户ID',
    aircraft_no     VARCHAR(50)     COMMENT '飞机注册号',
    fault_category  VARCHAR(100)    COMMENT '故障类别',
    status          TINYINT         NOT NULL DEFAULT 1 COMMENT '状态 1=进行中 2=已结束',
    title           VARCHAR(200)    COMMENT '会话标题',
    conclusion      TEXT            COMMENT 'AI诊断结论',
    create_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted      TINYINT         NOT NULL DEFAULT 0,
    INDEX idx_user_id (user_id),
    INDEX idx_aircraft_no (aircraft_no),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='故障诊断会话表';

CREATE TABLE IF NOT EXISTS fault_diagnosis_message (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id      BIGINT          NOT NULL COMMENT '会话ID',
    role            VARCHAR(20)     NOT NULL COMMENT '消息角色(user/assistant)',
    content         TEXT            NOT NULL COMMENT '消息内容',
    token_count     INT             COMMENT 'Token消耗数',
    create_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_session_id (session_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='故障诊断消息表';
```

- [ ] **Step 6: Commit**

```bash
git add mro-backend/fault-diagnosis-service/
git commit -m "feat: add fault-diagnosis-service skeleton with DB schema

Refs: BE-05"
```

---

## Task 7: fault-diagnosis-service — Entity + Mapper + Service + Dubbo Provider

**Files:**
- Create: entity, mapper, service, dubbo impl (见文件结构)
- Test: `src/test/java/com/mro/faultdiagnosis/service/FaultDiagnosisSessionServiceTest.java`

- [ ] **Step 1: 写失败测试**

```java
// src/test/java/com/mro/faultdiagnosis/service/FaultDiagnosisSessionServiceTest.java
package com.mro.faultdiagnosis.service;

import com.mro.faultdiagnosis.entity.FaultDiagnosisSession;
import com.mro.faultdiagnosis.mapper.FaultDiagnosisSessionMapper;
import com.mro.faultdiagnosis.mapper.FaultDiagnosisMessageMapper;
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
class FaultDiagnosisSessionServiceTest {

    @Mock
    private FaultDiagnosisSessionMapper sessionMapper;

    @Mock
    private FaultDiagnosisMessageMapper messageMapper;

    @InjectMocks
    private FaultDiagnosisSessionService sessionService;

    @Test
    void test_createSession_validInput_returnsSessionNo() {
        when(sessionMapper.insert(any())).thenAnswer(inv -> {
            FaultDiagnosisSession s = inv.getArgument(0);
            s.setId(1L);
            return 1;
        });

        String sessionNo = sessionService.createSession(1L, "B-1234", "ENGINE", "发动机故障排查");
        assertNotNull(sessionNo);
        assertTrue(sessionNo.startsWith("FD-"));
        verify(sessionMapper).insert(any());
    }

    @Test
    void test_createSession_nullUserId_throwsBusinessException() {
        assertThrows(BusinessException.class,
                () -> sessionService.createSession(null, "B-1234", "ENGINE", "title"));
    }

    @Test
    void test_endSession_nonExistentSession_throwsBusinessException() {
        when(sessionMapper.selectById(999L)).thenReturn(null);
        assertThrows(BusinessException.class, () -> sessionService.endSession(999L, "结论"));
    }
}
```

- [ ] **Step 2: 运行测试确认失败**

```bash
mvn test -pl fault-diagnosis-service -Dtest=FaultDiagnosisSessionServiceTest -q
```
期望：FAIL

- [ ] **Step 3: 创建 Entity 类**

```java
// src/main/java/com/mro/faultdiagnosis/entity/FaultDiagnosisSession.java
package com.mro.faultdiagnosis.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("fault_diagnosis_session")
public class FaultDiagnosisSession {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String sessionNo;
    private Long userId;
    private String aircraftNo;
    private String faultCategory;
    private Integer status;
    private String title;
    private String conclusion;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer isDeleted;
}
```

```java
// src/main/java/com/mro/faultdiagnosis/entity/FaultDiagnosisMessage.java
package com.mro.faultdiagnosis.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("fault_diagnosis_message")
public class FaultDiagnosisMessage {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long sessionId;
    private String role;
    private String content;
    private Integer tokenCount;
    private LocalDateTime createTime;
}
```

- [ ] **Step 4: 创建 Mapper**

```java
// src/main/java/com/mro/faultdiagnosis/mapper/FaultDiagnosisSessionMapper.java
package com.mro.faultdiagnosis.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mro.faultdiagnosis.entity.FaultDiagnosisSession;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FaultDiagnosisSessionMapper extends BaseMapper<FaultDiagnosisSession> {}
```

```java
// src/main/java/com/mro/faultdiagnosis/mapper/FaultDiagnosisMessageMapper.java
package com.mro.faultdiagnosis.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mro.faultdiagnosis.entity.FaultDiagnosisMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface FaultDiagnosisMessageMapper extends BaseMapper<FaultDiagnosisMessage> {

    @Select("SELECT * FROM fault_diagnosis_message WHERE session_id = #{sessionId} ORDER BY create_time ASC")
    List<FaultDiagnosisMessage> findBySessionId(@Param("sessionId") Long sessionId);
}
```

- [ ] **Step 5: 创建 Service**

```java
// src/main/java/com/mro/faultdiagnosis/service/FaultDiagnosisSessionService.java
package com.mro.faultdiagnosis.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mro.faultdiagnosis.entity.FaultDiagnosisMessage;
import com.mro.faultdiagnosis.entity.FaultDiagnosisSession;
import com.mro.faultdiagnosis.mapper.FaultDiagnosisMessageMapper;
import com.mro.faultdiagnosis.mapper.FaultDiagnosisSessionMapper;
import com.mro.common.core.exception.BusinessException;
import com.mro.common.core.result.ErrorCode;
import com.mro.common.core.result.PageResult;
import com.mro.common.dubbo.dto.faultdiagnosis.FaultDiagnosisMessageDTO;
import com.mro.common.dubbo.dto.faultdiagnosis.FaultDiagnosisSessionDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FaultDiagnosisSessionService {

    private final FaultDiagnosisSessionMapper sessionMapper;
    private final FaultDiagnosisMessageMapper messageMapper;

    public String createSession(Long userId, String aircraftNo, String faultCategory, String title) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "用户ID不能为空");
        }
        FaultDiagnosisSession session = new FaultDiagnosisSession();
        session.setSessionNo("FD-" + DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(LocalDateTime.now())
                + "-" + userId);
        session.setUserId(userId);
        session.setAircraftNo(aircraftNo);
        session.setFaultCategory(faultCategory);
        session.setTitle(title);
        session.setStatus(1);
        session.setCreateTime(LocalDateTime.now());
        sessionMapper.insert(session);
        return session.getSessionNo();
    }

    public void endSession(Long sessionId, String conclusion) {
        FaultDiagnosisSession session = sessionMapper.selectById(sessionId);
        if (session == null) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "诊断会话不存在");
        }
        session.setStatus(2);
        session.setConclusion(conclusion);
        sessionMapper.updateById(session);
    }

    @Transactional
    public Long addMessage(Long sessionId, String role, String content, Integer tokenCount) {
        FaultDiagnosisMessage message = new FaultDiagnosisMessage();
        message.setSessionId(sessionId);
        message.setRole(role);
        message.setContent(content);
        message.setTokenCount(tokenCount);
        message.setCreateTime(LocalDateTime.now());
        messageMapper.insert(message);
        return message.getId();
    }

    public List<FaultDiagnosisMessageDTO> listMessages(Long sessionId) {
        return messageMapper.findBySessionId(sessionId).stream().map(m ->
                new FaultDiagnosisMessageDTO(m.getId(), m.getSessionId(), m.getRole(),
                        m.getContent(), m.getTokenCount(), m.getCreateTime())
        ).toList();
    }

    public PageResult<FaultDiagnosisSessionDTO> pageByUserId(Long userId, int pageNum, int pageSize) {
        Page<FaultDiagnosisSession> page = new Page<>(pageNum, pageSize);
        sessionMapper.selectPage(page, new LambdaQueryWrapper<FaultDiagnosisSession>()
                .eq(FaultDiagnosisSession::getUserId, userId)
                .orderByDesc(FaultDiagnosisSession::getCreateTime));
        List<FaultDiagnosisSessionDTO> list = page.getRecords().stream().map(s ->
                new FaultDiagnosisSessionDTO(s.getId(), s.getSessionNo(), s.getUserId(),
                        s.getAircraftNo(), s.getFaultCategory(), s.getStatus(),
                        s.getTitle(), s.getCreateTime())
        ).toList();
        return PageResult.of(list, page.getTotal(), pageNum, pageSize);
    }
}
```

- [ ] **Step 6: 创建 Dubbo Provider**

```java
// src/main/java/com/mro/faultdiagnosis/dubbo/FaultDiagnosisDubboServiceImpl.java
package com.mro.faultdiagnosis.dubbo;

import com.mro.faultdiagnosis.service.FaultDiagnosisSessionService;
import com.mro.common.core.result.PageResult;
import com.mro.common.dubbo.dto.faultdiagnosis.FaultDiagnosisMessageDTO;
import com.mro.common.dubbo.dto.faultdiagnosis.FaultDiagnosisSessionDTO;
import com.mro.common.dubbo.service.FaultDiagnosisDubboService;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;
import java.util.List;

@DubboService(version = "1.0.0", group = "mro")
@RequiredArgsConstructor
public class FaultDiagnosisDubboServiceImpl implements FaultDiagnosisDubboService {

    private final FaultDiagnosisSessionService sessionService;

    @Override
    public String createSession(Long userId, String aircraftNo, String faultCategory, String title) {
        return sessionService.createSession(userId, aircraftNo, faultCategory, title);
    }

    @Override
    public void endSession(Long sessionId, String conclusion) {
        sessionService.endSession(sessionId, conclusion);
    }

    @Override
    public Long addMessage(Long sessionId, String role, String content, Integer tokenCount) {
        return sessionService.addMessage(sessionId, role, content, tokenCount);
    }

    @Override
    public List<FaultDiagnosisMessageDTO> listMessages(Long sessionId) {
        return sessionService.listMessages(sessionId);
    }

    @Override
    public PageResult<FaultDiagnosisSessionDTO> pageByUserId(Long userId, int pageNum, int pageSize) {
        return sessionService.pageByUserId(userId, pageNum, pageSize);
    }
}
```

- [ ] **Step 7: 运行所有 fault-diagnosis 测试**

```bash
mvn test -pl fault-diagnosis-service -q
```
期望：BUILD SUCCESS

- [ ] **Step 8: 验证三个服务均可编译**

```bash
cd mro-backend
mvn compile -pl aircraft-health-service,ar-maintenance-service,fault-diagnosis-service -q
```
期望：BUILD SUCCESS（三个服务全部编译通过）

- [ ] **Step 9: Commit**

```bash
git add mro-backend/fault-diagnosis-service/
git commit -m "feat: add fault-diagnosis-service complete implementation

Refs: BE-05"
```

---

## 关键提示

**Dubbo 接口对齐：** 三个服务的 Dubbo Provider 实现类（`*DubboServiceImpl`）所实现的方法必须与 `mro-common-dubbo` 中对应接口定义完全匹配。若 BE-01 中接口方法签名与本计划中的实现不一致，**以 mro-common-dubbo 的接口为准**，调整本计划中实现类的方法签名，不要修改接口。

**DTO 类：** `AircraftHealthRecordDTO`、`AircraftHealthAlertDTO`、`ArMaintenanceTaskDTO`、`ArMaintenanceSessionDTO`、`FaultDiagnosisSessionDTO`、`FaultDiagnosisMessageDTO` 均为 Java 21 Record，已在 BE-01 Task 6 中定义于 `mro-common-dubbo`。直接 import 使用，不要重复定义。

**端口规划：**
- aircraft-health-service: HTTP 8085 / Dubbo 20883
- ar-maintenance-service: HTTP 8086 / Dubbo 20884
- fault-diagnosis-service: HTTP 8087 / Dubbo 20885

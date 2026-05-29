# BE-07: MRO 服务组3 — vr-ar-training · paperless-checkin · rag-service 实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现 vr-ar-training-service（VR/AR 培训管理）、paperless-checkin-service（无纸化工卡签到）、rag-service（RAG 知识库检索）三个 MRO 微服务的完整 Dubbo Provider 端。

**Architecture:** 三个独立 Spring Boot 服务，各持有独立 MySQL Schema（mro_vr_ar_training / mro_paperless_checkin / mro_rag），通过 Dubbo 暴露接口。rag-service 额外依赖外部向量数据库（通过 HTTP 调用），其 MySQL 存储知识库元数据和检索日志。

**Tech Stack:** Java 21 · Spring Boot 3.3 · Dubbo 3.3 · MyBatis-Plus 3.5 · Flyway 10 · MySQL 8 · Nacos · Maven Multi-module

**前置条件：** BE-01 完成（mro-common 模块可用）

---

## 文件结构

### vr-ar-training-service
```
mro-backend/vr-ar-training-service/
├── pom.xml
└── src/
    ├── main/
    │   ├── java/com/mro/vrartraining/
    │   │   ├── VrArTrainingApplication.java
    │   │   ├── entity/
    │   │   │   ├── TrainingCourse.java
    │   │   │   └── TrainingRecord.java
    │   │   ├── mapper/
    │   │   │   ├── TrainingCourseMapper.java
    │   │   │   └── TrainingRecordMapper.java
    │   │   ├── service/
    │   │   │   └── TrainingService.java
    │   │   └── dubbo/
    │   │       └── VrArTrainingDubboServiceImpl.java
    │   └── resources/
    │       ├── application.yml
    │       └── db/migration/V1__init_vr_ar_training.sql
    └── test/java/com/mro/vrartraining/service/
        └── TrainingServiceTest.java
```

### paperless-checkin-service
```
mro-backend/paperless-checkin-service/
├── pom.xml
└── src/
    ├── main/
    │   ├── java/com/mro/paperlesscheckin/
    │   │   ├── PaperlessCheckinApplication.java
    │   │   ├── entity/
    │   │   │   ├── WorkCard.java
    │   │   │   └── WorkCardCheckin.java
    │   │   ├── mapper/
    │   │   │   ├── WorkCardMapper.java
    │   │   │   └── WorkCardCheckinMapper.java
    │   │   ├── service/
    │   │   │   └── WorkCardService.java
    │   │   └── dubbo/
    │   │       └── PaperlessCheckinDubboServiceImpl.java
    │   └── resources/
    │       ├── application.yml
    │       └── db/migration/V1__init_paperless_checkin.sql
    └── test/java/com/mro/paperlesscheckin/service/
        └── WorkCardServiceTest.java
```

### rag-service
```
mro-backend/rag-service/
├── pom.xml
└── src/
    ├── main/
    │   ├── java/com/mro/rag/
    │   │   ├── RagApplication.java
    │   │   ├── entity/
    │   │   │   ├── KnowledgeBase.java
    │   │   │   └── RagQueryLog.java
    │   │   ├── mapper/
    │   │   │   ├── KnowledgeBaseMapper.java
    │   │   │   └── RagQueryLogMapper.java
    │   │   ├── service/
    │   │   │   └── RagService.java
    │   │   └── dubbo/
    │   │       └── RagDubboServiceImpl.java
    │   └── resources/
    │       ├── application.yml
    │       └── db/migration/V1__init_rag.sql
    └── test/java/com/mro/rag/service/
        └── RagServiceTest.java
```

---

## Task 1: vr-ar-training-service — POM + 配置 + Schema

**Files:**
- Create: `mro-backend/vr-ar-training-service/pom.xml`
- Create: `mro-backend/vr-ar-training-service/src/main/java/com/mro/vrartraining/VrArTrainingApplication.java`
- Create: `mro-backend/vr-ar-training-service/src/main/resources/application.yml`
- Create: `mro-backend/vr-ar-training-service/src/main/resources/db/migration/V1__init_vr_ar_training.sql`

- [ ] **Step 1: 创建 POM**

```xml
<!-- mro-backend/vr-ar-training-service/pom.xml -->
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
  <artifactId>vr-ar-training-service</artifactId>

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
<module>vr-ar-training-service</module>
```

- [ ] **Step 3: 创建启动类**

```java
package com.mro.vrartraining;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableDubbo
public class VrArTrainingApplication {
    public static void main(String[] args) {
        SpringApplication.run(VrArTrainingApplication.class, args);
    }
}
```

- [ ] **Step 4: 创建 application.yml**

```yaml
server:
  port: 8091

spring:
  application:
    name: vr-ar-training-service
  threads:
    virtual:
      enabled: true
  datasource:
    url: jdbc:mysql://localhost:3306/mro_vr_ar_training?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai
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
    name: vr-ar-training-service
  registry:
    address: nacos://127.0.0.1:8848
    parameters:
      namespace: dev
  protocol:
    name: dubbo
    port: 20889
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
-- V1__init_vr_ar_training.sql
CREATE DATABASE IF NOT EXISTS mro_vr_ar_training DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE mro_vr_ar_training;

CREATE TABLE IF NOT EXISTS training_course (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_code     VARCHAR(50)     NOT NULL UNIQUE COMMENT '课程编号',
    course_name     VARCHAR(200)    NOT NULL COMMENT '课程名称',
    course_type     VARCHAR(50)     NOT NULL COMMENT '课程类型(VR/AR/MIXED)',
    aircraft_type   VARCHAR(50)     COMMENT '适用机型',
    category        VARCHAR(50)     COMMENT '培训类别(INITIAL/RECURRENT/SPECIALIZED)',
    duration_min    INT             COMMENT '课程时长(分钟)',
    pass_score      DECIMAL(5,2)    NOT NULL DEFAULT 60.00 COMMENT '合格分数线',
    resource_url    VARCHAR(500)    COMMENT 'VR/AR资源URL',
    description     TEXT            COMMENT '课程描述',
    status          TINYINT         NOT NULL DEFAULT 1 COMMENT '状态 1=上架 2=下架',
    create_by       BIGINT,
    create_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted      TINYINT         NOT NULL DEFAULT 0,
    INDEX idx_course_type (course_type),
    INDEX idx_aircraft_type (aircraft_type),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='VR/AR培训课程表';

CREATE TABLE IF NOT EXISTS training_record (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_id       BIGINT          NOT NULL COMMENT '课程ID',
    user_id         BIGINT          NOT NULL COMMENT '学员用户ID',
    status          TINYINT         NOT NULL DEFAULT 1 COMMENT '状态 1=进行中 2=已完成 3=已过期',
    score           DECIMAL(5,2)    COMMENT '考核得分',
    pass            TINYINT         COMMENT '是否通过 0=未通过 1=通过',
    start_time      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    complete_time   DATETIME        COMMENT '完成时间',
    duration_min    INT             COMMENT '实际用时(分钟)',
    create_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted      TINYINT         NOT NULL DEFAULT 0,
    INDEX idx_course_id (course_id),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='培训记录表';
```

- [ ] **Step 6: Commit**

```bash
git add mro-backend/vr-ar-training-service/
git commit -m "feat: add vr-ar-training-service skeleton with DB schema

Refs: BE-07"
```

---

## Task 2: vr-ar-training-service — Entity + Mapper + Service + Dubbo Provider

**Files:**
- Create: entity, mapper, service, dubbo impl (见文件结构)
- Test: `src/test/java/com/mro/vrartraining/service/TrainingServiceTest.java`

- [ ] **Step 1: 写失败测试**

```java
// src/test/java/com/mro/vrartraining/service/TrainingServiceTest.java
package com.mro.vrartraining.service;

import com.mro.vrartraining.entity.TrainingCourse;
import com.mro.vrartraining.entity.TrainingRecord;
import com.mro.vrartraining.mapper.TrainingCourseMapper;
import com.mro.vrartraining.mapper.TrainingRecordMapper;
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
class TrainingServiceTest {

    @Mock
    private TrainingCourseMapper courseMapper;

    @Mock
    private TrainingRecordMapper recordMapper;

    @InjectMocks
    private TrainingService trainingService;

    @Test
    void test_createCourse_validInput_returnsId() {
        when(courseMapper.existsByCourseCode("VR-001")).thenReturn(false);
        when(courseMapper.insert(any())).thenAnswer(inv -> {
            TrainingCourse c = inv.getArgument(0);
            c.setId(1L);
            return 1;
        });

        TrainingCourse course = new TrainingCourse();
        course.setCourseCode("VR-001");
        course.setCourseName("发动机VR检查培训");
        course.setCourseType("VR");
        course.setPassScore(new BigDecimal("70.00"));

        Long id = trainingService.createCourse(course, 1L);
        assertEquals(1L, id);
    }

    @Test
    void test_createCourse_duplicateCode_throwsBusinessException() {
        when(courseMapper.existsByCourseCode("VR-001")).thenReturn(true);

        TrainingCourse course = new TrainingCourse();
        course.setCourseCode("VR-001");
        course.setCourseName("测试");
        course.setCourseType("VR");
        course.setPassScore(new BigDecimal("60.00"));

        assertThrows(BusinessException.class, () -> trainingService.createCourse(course, 1L));
        verify(courseMapper, never()).insert(any());
    }

    @Test
    void test_submitResult_passingScore_setsPassTrue() {
        TrainingRecord existing = new TrainingRecord();
        existing.setId(1L);
        existing.setStatus(1);

        TrainingCourse course = new TrainingCourse();
        course.setId(10L);
        course.setPassScore(new BigDecimal("60.00"));

        when(recordMapper.selectById(1L)).thenReturn(existing);
        when(courseMapper.selectById(existing.getCourseId())).thenReturn(course);
        when(recordMapper.updateById(any())).thenReturn(1);

        trainingService.submitResult(1L, new BigDecimal("85.00"), 45);

        verify(recordMapper).updateById(argThat(r ->
                r.getPass().equals(1) && r.getScore().compareTo(new BigDecimal("85.00")) == 0
        ));
    }
}
```

- [ ] **Step 2: 运行测试确认失败**

```bash
mvn test -pl vr-ar-training-service -Dtest=TrainingServiceTest -q
```
期望：FAIL

- [ ] **Step 3: 创建 Entity 类**

```java
// src/main/java/com/mro/vrartraining/entity/TrainingCourse.java
package com.mro.vrartraining.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("training_course")
public class TrainingCourse {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String courseCode;
    private String courseName;
    private String courseType;
    private String aircraftType;
    private String category;
    private Integer durationMin;
    private BigDecimal passScore;
    private String resourceUrl;
    private String description;
    private Integer status;
    private Long createBy;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer isDeleted;
}
```

```java
// src/main/java/com/mro/vrartraining/entity/TrainingRecord.java
package com.mro.vrartraining.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("training_record")
public class TrainingRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long courseId;
    private Long userId;
    private Integer status;
    private BigDecimal score;
    private Integer pass;
    private LocalDateTime startTime;
    private LocalDateTime completeTime;
    private Integer durationMin;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer isDeleted;
}
```

- [ ] **Step 4: 创建 Mapper**

```java
// src/main/java/com/mro/vrartraining/mapper/TrainingCourseMapper.java
package com.mro.vrartraining.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mro.vrartraining.entity.TrainingCourse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface TrainingCourseMapper extends BaseMapper<TrainingCourse> {

    @Select("SELECT COUNT(1) > 0 FROM training_course WHERE course_code = #{courseCode} AND is_deleted = 0")
    boolean existsByCourseCode(@Param("courseCode") String courseCode);
}
```

```java
// src/main/java/com/mro/vrartraining/mapper/TrainingRecordMapper.java
package com.mro.vrartraining.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mro.vrartraining.entity.TrainingRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface TrainingRecordMapper extends BaseMapper<TrainingRecord> {

    @Select("SELECT * FROM training_record WHERE user_id = #{userId} AND is_deleted = 0 ORDER BY create_time DESC")
    List<TrainingRecord> findByUserId(@Param("userId") Long userId);

    @Select("SELECT COUNT(1) FROM training_record WHERE course_id = #{courseId} AND pass = 1 AND is_deleted = 0")
    int countPassedByCourseId(@Param("courseId") Long courseId);
}
```

- [ ] **Step 5: 创建 Service**

```java
// src/main/java/com/mro/vrartraining/service/TrainingService.java
package com.mro.vrartraining.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mro.vrartraining.entity.TrainingCourse;
import com.mro.vrartraining.entity.TrainingRecord;
import com.mro.vrartraining.mapper.TrainingCourseMapper;
import com.mro.vrartraining.mapper.TrainingRecordMapper;
import com.mro.common.core.exception.BusinessException;
import com.mro.common.core.result.ErrorCode;
import com.mro.common.core.result.PageResult;
import com.mro.common.dubbo.dto.training.TrainingCourseDTO;
import com.mro.common.dubbo.dto.training.TrainingRecordDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TrainingService {

    private final TrainingCourseMapper courseMapper;
    private final TrainingRecordMapper recordMapper;

    public Long createCourse(TrainingCourse course, Long createBy) {
        if (courseMapper.existsByCourseCode(course.getCourseCode())) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "课程编号已存在: " + course.getCourseCode());
        }
        course.setStatus(1);
        course.setCreateBy(createBy);
        course.setCreateTime(LocalDateTime.now());
        courseMapper.insert(course);
        return course.getId();
    }

    public Long startTraining(Long courseId, Long userId) {
        TrainingCourse course = courseMapper.selectById(courseId);
        if (course == null || course.getStatus() != 1) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "课程不存在或已下架");
        }
        TrainingRecord record = new TrainingRecord();
        record.setCourseId(courseId);
        record.setUserId(userId);
        record.setStatus(1);
        record.setStartTime(LocalDateTime.now());
        record.setCreateTime(LocalDateTime.now());
        recordMapper.insert(record);
        return record.getId();
    }

    public void submitResult(Long recordId, BigDecimal score, Integer durationMin) {
        TrainingRecord record = recordMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "培训记录不存在");
        }
        TrainingCourse course = courseMapper.selectById(record.getCourseId());
        int pass = (course != null && score.compareTo(course.getPassScore()) >= 0) ? 1 : 0;
        record.setScore(score);
        record.setPass(pass);
        record.setDurationMin(durationMin);
        record.setStatus(2);
        record.setCompleteTime(LocalDateTime.now());
        recordMapper.updateById(record);
    }

    public PageResult<TrainingCourseDTO> pageCourses(String courseType, String aircraftType, int pageNum, int pageSize) {
        Page<TrainingCourse> page = new Page<>(pageNum, pageSize);
        courseMapper.selectPage(page, new LambdaQueryWrapper<TrainingCourse>()
                .eq(StringUtils.hasText(courseType), TrainingCourse::getCourseType, courseType)
                .eq(StringUtils.hasText(aircraftType), TrainingCourse::getAircraftType, aircraftType)
                .eq(TrainingCourse::getStatus, 1)
                .orderByDesc(TrainingCourse::getCreateTime));
        List<TrainingCourseDTO> list = page.getRecords().stream().map(c ->
                new TrainingCourseDTO(c.getId(), c.getCourseCode(), c.getCourseName(),
                        c.getCourseType(), c.getAircraftType(), c.getCategory(),
                        c.getDurationMin(), c.getPassScore(), c.getResourceUrl(), c.getStatus())
        ).toList();
        return PageResult.of(list, page.getTotal(), pageNum, pageSize);
    }

    public List<TrainingRecordDTO> listUserRecords(Long userId) {
        return recordMapper.findByUserId(userId).stream().map(r ->
                new TrainingRecordDTO(r.getId(), r.getCourseId(), r.getUserId(),
                        r.getStatus(), r.getScore(), r.getPass(),
                        r.getStartTime(), r.getCompleteTime(), r.getDurationMin())
        ).toList();
    }
}
```

- [ ] **Step 6: 创建 Dubbo Provider**

```java
// src/main/java/com/mro/vrartraining/dubbo/VrArTrainingDubboServiceImpl.java
package com.mro.vrartraining.dubbo;

import com.mro.vrartraining.entity.TrainingCourse;
import com.mro.vrartraining.service.TrainingService;
import com.mro.common.core.result.PageResult;
import com.mro.common.dubbo.dto.training.TrainingCourseDTO;
import com.mro.common.dubbo.dto.training.TrainingRecordDTO;
import com.mro.common.dubbo.service.VrArTrainingDubboService;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;

import java.math.BigDecimal;
import java.util.List;

@DubboService(version = "1.0.0", group = "mro")
@RequiredArgsConstructor
public class VrArTrainingDubboServiceImpl implements VrArTrainingDubboService {

    private final TrainingService trainingService;

    @Override
    public Long createCourse(String courseCode, String courseName, String courseType,
                              String aircraftType, String category, Integer durationMin,
                              BigDecimal passScore, String resourceUrl, Long createBy) {
        TrainingCourse course = new TrainingCourse();
        course.setCourseCode(courseCode);
        course.setCourseName(courseName);
        course.setCourseType(courseType);
        course.setAircraftType(aircraftType);
        course.setCategory(category);
        course.setDurationMin(durationMin);
        course.setPassScore(passScore);
        course.setResourceUrl(resourceUrl);
        return trainingService.createCourse(course, createBy);
    }

    @Override
    public Long startTraining(Long courseId, Long userId) {
        return trainingService.startTraining(courseId, userId);
    }

    @Override
    public void submitResult(Long recordId, BigDecimal score, Integer durationMin) {
        trainingService.submitResult(recordId, score, durationMin);
    }

    @Override
    public PageResult<TrainingCourseDTO> pageCourses(String courseType, String aircraftType, int pageNum, int pageSize) {
        return trainingService.pageCourses(courseType, aircraftType, pageNum, pageSize);
    }

    @Override
    public List<TrainingRecordDTO> listUserRecords(Long userId) {
        return trainingService.listUserRecords(userId);
    }
}
```

- [ ] **Step 7: 运行测试**

```bash
mvn test -pl vr-ar-training-service -q
```
期望：BUILD SUCCESS

- [ ] **Step 8: Commit**

```bash
git add mro-backend/vr-ar-training-service/
git commit -m "feat: add vr-ar-training-service complete implementation

Refs: BE-07"
```

---

## Task 3: paperless-checkin-service — POM + 配置 + Schema

**Files:**
- Create: `mro-backend/paperless-checkin-service/pom.xml`
- Create: `mro-backend/paperless-checkin-service/src/main/java/com/mro/paperlesscheckin/PaperlessCheckinApplication.java`
- Create: `mro-backend/paperless-checkin-service/src/main/resources/application.yml`
- Create: `mro-backend/paperless-checkin-service/src/main/resources/db/migration/V1__init_paperless_checkin.sql`

- [ ] **Step 1: 创建 POM**

```xml
<!-- mro-backend/paperless-checkin-service/pom.xml -->
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
  <artifactId>paperless-checkin-service</artifactId>

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
<module>paperless-checkin-service</module>
```

- [ ] **Step 3: 创建启动类**

```java
package com.mro.paperlesscheckin;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableDubbo
public class PaperlessCheckinApplication {
    public static void main(String[] args) {
        SpringApplication.run(PaperlessCheckinApplication.class, args);
    }
}
```

- [ ] **Step 4: 创建 application.yml**

```yaml
server:
  port: 8092

spring:
  application:
    name: paperless-checkin-service
  threads:
    virtual:
      enabled: true
  datasource:
    url: jdbc:mysql://localhost:3306/mro_paperless_checkin?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai
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
    name: paperless-checkin-service
  registry:
    address: nacos://127.0.0.1:8848
    parameters:
      namespace: dev
  protocol:
    name: dubbo
    port: 20890
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
-- V1__init_paperless_checkin.sql
CREATE DATABASE IF NOT EXISTS mro_paperless_checkin DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE mro_paperless_checkin;

CREATE TABLE IF NOT EXISTS work_card (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    card_no         VARCHAR(50)     NOT NULL UNIQUE COMMENT '工卡编号',
    aircraft_no     VARCHAR(50)     NOT NULL COMMENT '飞机注册号',
    task_title      VARCHAR(200)    NOT NULL COMMENT '工作任务标题',
    task_type       VARCHAR(50)     COMMENT '任务类型(SCHEDULED/UNSCHEDULED/INSPECTION)',
    work_area       VARCHAR(100)    COMMENT '工作区域',
    assigned_team   VARCHAR(100)    COMMENT '负责班组',
    plan_date       DATE            COMMENT '计划执行日期',
    status          TINYINT         NOT NULL DEFAULT 1 COMMENT '状态 1=待执行 2=执行中 3=已完成 4=已取消',
    description     TEXT            COMMENT '工作说明',
    create_by       BIGINT,
    create_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted      TINYINT         NOT NULL DEFAULT 0,
    INDEX idx_aircraft_no (aircraft_no),
    INDEX idx_status (status),
    INDEX idx_plan_date (plan_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='无纸化工卡表';

CREATE TABLE IF NOT EXISTS work_card_checkin (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    card_id         BIGINT          NOT NULL COMMENT '工卡ID',
    user_id         BIGINT          NOT NULL COMMENT '签到用户ID',
    checkin_type    VARCHAR(20)     NOT NULL COMMENT '签到类型(START/STEP/COMPLETE)',
    step_name       VARCHAR(100)    COMMENT '工步名称',
    checkin_time    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    location        VARCHAR(200)    COMMENT '签到位置',
    remark          VARCHAR(500)    COMMENT '备注',
    signature_url   VARCHAR(500)    COMMENT '电子签名URL',
    create_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_card_id (card_id),
    INDEX idx_user_id (user_id),
    INDEX idx_checkin_time (checkin_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工卡签到记录表';
```

- [ ] **Step 6: Commit**

```bash
git add mro-backend/paperless-checkin-service/
git commit -m "feat: add paperless-checkin-service skeleton with DB schema

Refs: BE-07"
```

---

## Task 4: paperless-checkin-service — Entity + Mapper + Service + Dubbo Provider

**Files:**
- Create: entity, mapper, service, dubbo impl (见文件结构)
- Test: `src/test/java/com/mro/paperlesscheckin/service/WorkCardServiceTest.java`

- [ ] **Step 1: 写失败测试**

```java
// src/test/java/com/mro/paperlesscheckin/service/WorkCardServiceTest.java
package com.mro.paperlesscheckin.service;

import com.mro.paperlesscheckin.entity.WorkCard;
import com.mro.paperlesscheckin.entity.WorkCardCheckin;
import com.mro.paperlesscheckin.mapper.WorkCardMapper;
import com.mro.paperlesscheckin.mapper.WorkCardCheckinMapper;
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
class WorkCardServiceTest {

    @Mock
    private WorkCardMapper workCardMapper;

    @Mock
    private WorkCardCheckinMapper checkinMapper;

    @InjectMocks
    private WorkCardService workCardService;

    @Test
    void test_createWorkCard_validInput_returnsId() {
        when(workCardMapper.existsByCardNo("WC-2026-001")).thenReturn(false);
        when(workCardMapper.insert(any())).thenAnswer(inv -> {
            WorkCard wc = inv.getArgument(0);
            wc.setId(1L);
            return 1;
        });

        WorkCard card = new WorkCard();
        card.setCardNo("WC-2026-001");
        card.setAircraftNo("B-1234");
        card.setTaskTitle("A检定检");
        card.setTaskType("SCHEDULED");

        Long id = workCardService.createWorkCard(card, 1L);
        assertEquals(1L, id);
    }

    @Test
    void test_createWorkCard_duplicateCardNo_throwsBusinessException() {
        when(workCardMapper.existsByCardNo("WC-2026-001")).thenReturn(true);

        WorkCard card = new WorkCard();
        card.setCardNo("WC-2026-001");
        card.setAircraftNo("B-1234");
        card.setTaskTitle("测试工卡");
        card.setTaskType("SCHEDULED");

        assertThrows(BusinessException.class, () -> workCardService.createWorkCard(card, 1L));
        verify(workCardMapper, never()).insert(any());
    }

    @Test
    void test_checkin_nonExistentCard_throwsBusinessException() {
        when(workCardMapper.selectById(999L)).thenReturn(null);

        assertThrows(BusinessException.class,
                () -> workCardService.checkin(999L, 1L, "START", null, null, null));
    }
}
```

- [ ] **Step 2: 运行测试确认失败**

```bash
mvn test -pl paperless-checkin-service -Dtest=WorkCardServiceTest -q
```
期望：FAIL

- [ ] **Step 3: 创建 Entity 类**

```java
// src/main/java/com/mro/paperlesscheckin/entity/WorkCard.java
package com.mro.paperlesscheckin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("work_card")
public class WorkCard {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String cardNo;
    private String aircraftNo;
    private String taskTitle;
    private String taskType;
    private String workArea;
    private String assignedTeam;
    private LocalDate planDate;
    private Integer status;
    private String description;
    private Long createBy;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer isDeleted;
}
```

```java
// src/main/java/com/mro/paperlesscheckin/entity/WorkCardCheckin.java
package com.mro.paperlesscheckin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("work_card_checkin")
public class WorkCardCheckin {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long cardId;
    private Long userId;
    private String checkinType;
    private String stepName;
    private LocalDateTime checkinTime;
    private String location;
    private String remark;
    private String signatureUrl;
    private LocalDateTime createTime;
}
```

- [ ] **Step 4: 创建 Mapper**

```java
// src/main/java/com/mro/paperlesscheckin/mapper/WorkCardMapper.java
package com.mro.paperlesscheckin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mro.paperlesscheckin.entity.WorkCard;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface WorkCardMapper extends BaseMapper<WorkCard> {

    @Select("SELECT COUNT(1) > 0 FROM work_card WHERE card_no = #{cardNo} AND is_deleted = 0")
    boolean existsByCardNo(@Param("cardNo") String cardNo);
}
```

```java
// src/main/java/com/mro/paperlesscheckin/mapper/WorkCardCheckinMapper.java
package com.mro.paperlesscheckin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mro.paperlesscheckin.entity.WorkCardCheckin;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface WorkCardCheckinMapper extends BaseMapper<WorkCardCheckin> {

    @Select("SELECT * FROM work_card_checkin WHERE card_id = #{cardId} ORDER BY checkin_time ASC")
    List<WorkCardCheckin> findByCardId(@Param("cardId") Long cardId);
}
```

- [ ] **Step 5: 创建 Service**

```java
// src/main/java/com/mro/paperlesscheckin/service/WorkCardService.java
package com.mro.paperlesscheckin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mro.paperlesscheckin.entity.WorkCard;
import com.mro.paperlesscheckin.entity.WorkCardCheckin;
import com.mro.paperlesscheckin.mapper.WorkCardCheckinMapper;
import com.mro.paperlesscheckin.mapper.WorkCardMapper;
import com.mro.common.core.exception.BusinessException;
import com.mro.common.core.result.ErrorCode;
import com.mro.common.core.result.PageResult;
import com.mro.common.dubbo.dto.checkin.WorkCardCheckinDTO;
import com.mro.common.dubbo.dto.checkin.WorkCardDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkCardService {

    private final WorkCardMapper workCardMapper;
    private final WorkCardCheckinMapper checkinMapper;

    public Long createWorkCard(WorkCard card, Long createBy) {
        if (workCardMapper.existsByCardNo(card.getCardNo())) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "工卡编号已存在: " + card.getCardNo());
        }
        card.setStatus(1);
        card.setCreateBy(createBy);
        card.setCreateTime(LocalDateTime.now());
        workCardMapper.insert(card);
        return card.getId();
    }

    public Long checkin(Long cardId, Long userId, String checkinType,
                         String stepName, String location, String signatureUrl) {
        WorkCard card = workCardMapper.selectById(cardId);
        if (card == null) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "工卡不存在");
        }
        WorkCardCheckin checkin = new WorkCardCheckin();
        checkin.setCardId(cardId);
        checkin.setUserId(userId);
        checkin.setCheckinType(checkinType);
        checkin.setStepName(stepName);
        checkin.setCheckinTime(LocalDateTime.now());
        checkin.setLocation(location);
        checkin.setSignatureUrl(signatureUrl);
        checkin.setCreateTime(LocalDateTime.now());
        checkinMapper.insert(checkin);

        if ("COMPLETE".equals(checkinType)) {
            card.setStatus(3);
            workCardMapper.updateById(card);
        } else if ("START".equals(checkinType) && card.getStatus() == 1) {
            card.setStatus(2);
            workCardMapper.updateById(card);
        }
        return checkin.getId();
    }

    public List<WorkCardCheckinDTO> listCheckins(Long cardId) {
        return checkinMapper.findByCardId(cardId).stream().map(c ->
                new WorkCardCheckinDTO(c.getId(), c.getCardId(), c.getUserId(),
                        c.getCheckinType(), c.getStepName(), c.getCheckinTime(),
                        c.getLocation(), c.getRemark(), c.getSignatureUrl())
        ).toList();
    }

    public PageResult<WorkCardDTO> pageByAircraftNo(String aircraftNo, Integer status, int pageNum, int pageSize) {
        Page<WorkCard> page = new Page<>(pageNum, pageSize);
        workCardMapper.selectPage(page, new LambdaQueryWrapper<WorkCard>()
                .eq(StringUtils.hasText(aircraftNo), WorkCard::getAircraftNo, aircraftNo)
                .eq(status != null, WorkCard::getStatus, status)
                .orderByDesc(WorkCard::getCreateTime));
        List<WorkCardDTO> list = page.getRecords().stream().map(wc ->
                new WorkCardDTO(wc.getId(), wc.getCardNo(), wc.getAircraftNo(), wc.getTaskTitle(),
                        wc.getTaskType(), wc.getWorkArea(), wc.getAssignedTeam(),
                        wc.getPlanDate(), wc.getStatus())
        ).toList();
        return PageResult.of(list, page.getTotal(), pageNum, pageSize);
    }
}
```

- [ ] **Step 6: 创建 Dubbo Provider**

```java
// src/main/java/com/mro/paperlesscheckin/dubbo/PaperlessCheckinDubboServiceImpl.java
package com.mro.paperlesscheckin.dubbo;

import com.mro.paperlesscheckin.entity.WorkCard;
import com.mro.paperlesscheckin.service.WorkCardService;
import com.mro.common.core.result.PageResult;
import com.mro.common.dubbo.dto.checkin.WorkCardCheckinDTO;
import com.mro.common.dubbo.dto.checkin.WorkCardDTO;
import com.mro.common.dubbo.service.PaperlessCheckinDubboService;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;

import java.time.LocalDate;
import java.util.List;

@DubboService(version = "1.0.0", group = "mro")
@RequiredArgsConstructor
public class PaperlessCheckinDubboServiceImpl implements PaperlessCheckinDubboService {

    private final WorkCardService workCardService;

    @Override
    public Long createWorkCard(String cardNo, String aircraftNo, String taskTitle,
                                String taskType, String workArea, String assignedTeam,
                                LocalDate planDate, String description, Long createBy) {
        WorkCard card = new WorkCard();
        card.setCardNo(cardNo);
        card.setAircraftNo(aircraftNo);
        card.setTaskTitle(taskTitle);
        card.setTaskType(taskType);
        card.setWorkArea(workArea);
        card.setAssignedTeam(assignedTeam);
        card.setPlanDate(planDate);
        card.setDescription(description);
        return workCardService.createWorkCard(card, createBy);
    }

    @Override
    public Long checkin(Long cardId, Long userId, String checkinType,
                         String stepName, String location, String signatureUrl) {
        return workCardService.checkin(cardId, userId, checkinType, stepName, location, signatureUrl);
    }

    @Override
    public List<WorkCardCheckinDTO> listCheckins(Long cardId) {
        return workCardService.listCheckins(cardId);
    }

    @Override
    public PageResult<WorkCardDTO> pageByAircraftNo(String aircraftNo, Integer status, int pageNum, int pageSize) {
        return workCardService.pageByAircraftNo(aircraftNo, status, pageNum, pageSize);
    }
}
```

- [ ] **Step 7: 运行测试**

```bash
mvn test -pl paperless-checkin-service -q
```
期望：BUILD SUCCESS

- [ ] **Step 8: Commit**

```bash
git add mro-backend/paperless-checkin-service/
git commit -m "feat: add paperless-checkin-service complete implementation

Refs: BE-07"
```

---

## Task 5: rag-service — POM + 配置 + Schema

**Files:**
- Create: `mro-backend/rag-service/pom.xml`
- Create: `mro-backend/rag-service/src/main/java/com/mro/rag/RagApplication.java`
- Create: `mro-backend/rag-service/src/main/resources/application.yml`
- Create: `mro-backend/rag-service/src/main/resources/db/migration/V1__init_rag.sql`

- [ ] **Step 1: 创建 POM**

```xml
<!-- mro-backend/rag-service/pom.xml -->
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
  <artifactId>rag-service</artifactId>

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
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-test</artifactId>
      <scope>test</scope>
    </dependency>
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
<module>rag-service</module>
```

- [ ] **Step 3: 创建启动类**

```java
package com.mro.rag;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableDubbo
public class RagApplication {
    public static void main(String[] args) {
        SpringApplication.run(RagApplication.class, args);
    }
}
```

- [ ] **Step 4: 创建 application.yml**

```yaml
server:
  port: 8093

spring:
  application:
    name: rag-service
  threads:
    virtual:
      enabled: true
  datasource:
    url: jdbc:mysql://localhost:3306/mro_rag?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai
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
    name: rag-service
  registry:
    address: nacos://127.0.0.1:8848
    parameters:
      namespace: dev
  protocol:
    name: dubbo
    port: 20891
  provider:
    timeout: 30000
    group: mro
    version: 1.0.0

# RAG外部向量数据库配置 (本地开发占位，生产通过Nacos Config覆盖)
rag:
  vector-db:
    base-url: http://localhost:8200
    api-key: dev-placeholder

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
-- V1__init_rag.sql
CREATE DATABASE IF NOT EXISTS mro_rag DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE mro_rag;

CREATE TABLE IF NOT EXISTS knowledge_base (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    kb_code         VARCHAR(50)     NOT NULL UNIQUE COMMENT '知识库编号',
    kb_name         VARCHAR(200)    NOT NULL COMMENT '知识库名称',
    kb_type         VARCHAR(50)     NOT NULL COMMENT '类型(MANUAL/REGULATION/EXPERIENCE/FAQ)',
    aircraft_type   VARCHAR(50)     COMMENT '适用机型',
    doc_count       INT             NOT NULL DEFAULT 0 COMMENT '文档数量',
    status          TINYINT         NOT NULL DEFAULT 1 COMMENT '状态 1=启用 2=停用',
    description     VARCHAR(500)    COMMENT '描述',
    create_by       BIGINT,
    create_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted      TINYINT         NOT NULL DEFAULT 0,
    INDEX idx_kb_type (kb_type),
    INDEX idx_aircraft_type (aircraft_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库表';

CREATE TABLE IF NOT EXISTS rag_query_log (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT          NOT NULL COMMENT '查询用户ID',
    kb_code         VARCHAR(50)     COMMENT '知识库编号',
    query_text      TEXT            NOT NULL COMMENT '查询文本',
    result_count    INT             COMMENT '返回结果数',
    latency_ms      INT             COMMENT '查询耗时(ms)',
    query_time      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_query_time (query_time),
    INDEX idx_kb_code (kb_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='RAG查询日志表';
```

- [ ] **Step 6: Commit**

```bash
git add mro-backend/rag-service/
git commit -m "feat: add rag-service skeleton with DB schema

Refs: BE-07"
```

---

## Task 6: rag-service — Entity + Mapper + Service + Dubbo Provider

**Files:**
- Create: entity, mapper, service, dubbo impl (见文件结构)
- Test: `src/test/java/com/mro/rag/service/RagServiceTest.java`

- [ ] **Step 1: 写失败测试**

```java
// src/test/java/com/mro/rag/service/RagServiceTest.java
package com.mro.rag.service;

import com.mro.rag.entity.KnowledgeBase;
import com.mro.rag.mapper.KnowledgeBaseMapper;
import com.mro.rag.mapper.RagQueryLogMapper;
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
class RagServiceTest {

    @Mock
    private KnowledgeBaseMapper kbMapper;

    @Mock
    private RagQueryLogMapper queryLogMapper;

    @InjectMocks
    private RagService ragService;

    @Test
    void test_createKnowledgeBase_validInput_returnsId() {
        when(kbMapper.existsByKbCode("KB-001")).thenReturn(false);
        when(kbMapper.insert(any())).thenAnswer(inv -> {
            KnowledgeBase kb = inv.getArgument(0);
            kb.setId(1L);
            return 1;
        });

        Long id = ragService.createKnowledgeBase("KB-001", "A320维修手册库", "MANUAL", "A320", "测试知识库", 1L);
        assertEquals(1L, id);
        verify(kbMapper).insert(any());
    }

    @Test
    void test_createKnowledgeBase_duplicateCode_throwsBusinessException() {
        when(kbMapper.existsByKbCode("KB-001")).thenReturn(true);

        assertThrows(BusinessException.class,
                () -> ragService.createKnowledgeBase("KB-001", "重复库", "MANUAL", null, null, 1L));
        verify(kbMapper, never()).insert(any());
    }

    @Test
    void test_query_emptyQueryText_throwsBusinessException() {
        assertThrows(BusinessException.class,
                () -> ragService.query(1L, "KB-001", "  ", 5));
    }
}
```

- [ ] **Step 2: 运行测试确认失败**

```bash
mvn test -pl rag-service -Dtest=RagServiceTest -q
```
期望：FAIL

- [ ] **Step 3: 创建 Entity 类**

```java
// src/main/java/com/mro/rag/entity/KnowledgeBase.java
package com.mro.rag.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("knowledge_base")
public class KnowledgeBase {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String kbCode;
    private String kbName;
    private String kbType;
    private String aircraftType;
    private Integer docCount;
    private Integer status;
    private String description;
    private Long createBy;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer isDeleted;
}
```

```java
// src/main/java/com/mro/rag/entity/RagQueryLog.java
package com.mro.rag.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("rag_query_log")
public class RagQueryLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String kbCode;
    private String queryText;
    private Integer resultCount;
    private Integer latencyMs;
    private LocalDateTime queryTime;
}
```

- [ ] **Step 4: 创建 Mapper**

```java
// src/main/java/com/mro/rag/mapper/KnowledgeBaseMapper.java
package com.mro.rag.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mro.rag.entity.KnowledgeBase;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface KnowledgeBaseMapper extends BaseMapper<KnowledgeBase> {

    @Select("SELECT COUNT(1) > 0 FROM knowledge_base WHERE kb_code = #{kbCode} AND is_deleted = 0")
    boolean existsByKbCode(@Param("kbCode") String kbCode);

    @Update("UPDATE knowledge_base SET doc_count = doc_count + #{delta} WHERE kb_code = #{kbCode} AND is_deleted = 0")
    int updateDocCount(@Param("kbCode") String kbCode, @Param("delta") int delta);
}
```

```java
// src/main/java/com/mro/rag/mapper/RagQueryLogMapper.java
package com.mro.rag.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mro.rag.entity.RagQueryLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RagQueryLogMapper extends BaseMapper<RagQueryLog> {}
```

- [ ] **Step 5: 创建 RagService**

```java
// src/main/java/com/mro/rag/service/RagService.java
package com.mro.rag.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mro.rag.entity.KnowledgeBase;
import com.mro.rag.entity.RagQueryLog;
import com.mro.rag.mapper.KnowledgeBaseMapper;
import com.mro.rag.mapper.RagQueryLogMapper;
import com.mro.common.core.exception.BusinessException;
import com.mro.common.core.result.ErrorCode;
import com.mro.common.core.result.PageResult;
import com.mro.common.dubbo.dto.rag.KnowledgeBaseDTO;
import com.mro.common.dubbo.dto.rag.RagQueryResultDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RagService {

    private final KnowledgeBaseMapper kbMapper;
    private final RagQueryLogMapper queryLogMapper;

    @Value("${rag.vector-db.base-url:http://localhost:8200}")
    private String vectorDbBaseUrl;

    public Long createKnowledgeBase(String kbCode, String kbName, String kbType,
                                     String aircraftType, String description, Long createBy) {
        if (kbMapper.existsByKbCode(kbCode)) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "知识库编号已存在: " + kbCode);
        }
        KnowledgeBase kb = new KnowledgeBase();
        kb.setKbCode(kbCode);
        kb.setKbName(kbName);
        kb.setKbType(kbType);
        kb.setAircraftType(aircraftType);
        kb.setDescription(description);
        kb.setDocCount(0);
        kb.setStatus(1);
        kb.setCreateBy(createBy);
        kb.setCreateTime(LocalDateTime.now());
        kbMapper.insert(kb);
        return kb.getId();
    }

    public List<RagQueryResultDTO> query(Long userId, String kbCode, String queryText, int topK) {
        if (!StringUtils.hasText(queryText)) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "查询文本不能为空");
        }

        long startMs = System.currentTimeMillis();

        // 调用外部向量数据库进行语义检索（占位实现，实际接入向量DB时替换）
        List<RagQueryResultDTO> results = queryVectorDb(kbCode, queryText.trim(), topK);

        long latencyMs = System.currentTimeMillis() - startMs;

        RagQueryLog log = new RagQueryLog();
        log.setUserId(userId);
        log.setKbCode(kbCode);
        log.setQueryText(queryText.trim());
        log.setResultCount(results.size());
        log.setLatencyMs((int) latencyMs);
        log.setQueryTime(LocalDateTime.now());
        queryLogMapper.insert(log);

        return results;
    }

    public PageResult<KnowledgeBaseDTO> pageKnowledgeBases(String kbType, int pageNum, int pageSize) {
        Page<KnowledgeBase> page = new Page<>(pageNum, pageSize);
        kbMapper.selectPage(page, new LambdaQueryWrapper<KnowledgeBase>()
                .eq(StringUtils.hasText(kbType), KnowledgeBase::getKbType, kbType)
                .eq(KnowledgeBase::getStatus, 1)
                .orderByDesc(KnowledgeBase::getCreateTime));
        List<KnowledgeBaseDTO> list = page.getRecords().stream().map(kb ->
                new KnowledgeBaseDTO(kb.getId(), kb.getKbCode(), kb.getKbName(),
                        kb.getKbType(), kb.getAircraftType(), kb.getDocCount(), kb.getStatus())
        ).toList();
        return PageResult.of(list, page.getTotal(), pageNum, pageSize);
    }

    // 向量数据库调用占位实现 — 真实环境通过 HTTP 调用向量DB的 /query API
    // 集成向量DB时替换此方法体，接口签名保持不变
    private List<RagQueryResultDTO> queryVectorDb(String kbCode, String queryText, int topK) {
        log.info("RAG query: kbCode={}, query={}, topK={}", kbCode, queryText, topK);
        return List.of();
    }
}
```

- [ ] **Step 6: 创建 Dubbo Provider**

```java
// src/main/java/com/mro/rag/dubbo/RagDubboServiceImpl.java
package com.mro.rag.dubbo;

import com.mro.rag.service.RagService;
import com.mro.common.core.result.PageResult;
import com.mro.common.dubbo.dto.rag.KnowledgeBaseDTO;
import com.mro.common.dubbo.dto.rag.RagQueryResultDTO;
import com.mro.common.dubbo.service.RagDubboService;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;
import java.util.List;

@DubboService(version = "1.0.0", group = "mro")
@RequiredArgsConstructor
public class RagDubboServiceImpl implements RagDubboService {

    private final RagService ragService;

    @Override
    public Long createKnowledgeBase(String kbCode, String kbName, String kbType,
                                     String aircraftType, String description, Long createBy) {
        return ragService.createKnowledgeBase(kbCode, kbName, kbType, aircraftType, description, createBy);
    }

    @Override
    public List<RagQueryResultDTO> query(Long userId, String kbCode, String queryText, int topK) {
        return ragService.query(userId, kbCode, queryText, topK);
    }

    @Override
    public PageResult<KnowledgeBaseDTO> pageKnowledgeBases(String kbType, int pageNum, int pageSize) {
        return ragService.pageKnowledgeBases(kbType, pageNum, pageSize);
    }
}
```

- [ ] **Step 7: 运行测试**

```bash
mvn test -pl rag-service -q
```
期望：BUILD SUCCESS

- [ ] **Step 8: 验证所有 BE-07 服务编译通过**

```bash
cd mro-backend
mvn compile -pl vr-ar-training-service,paperless-checkin-service,rag-service -q
```
期望：BUILD SUCCESS

- [ ] **Step 9: Commit**

```bash
git add mro-backend/rag-service/
git commit -m "feat: add rag-service complete implementation

Refs: BE-07"
```

---

## 关键提示

**Dubbo 接口对齐：** 所有 Dubbo Provider 实现类方法签名必须与 `mro-common-dubbo` 中接口完全一致。以 BE-01 中定义的接口为准，若有差异调整实现类，不修改接口。

**DTO 类位置：** `TrainingCourseDTO`、`TrainingRecordDTO`、`WorkCardDTO`、`WorkCardCheckinDTO`、`KnowledgeBaseDTO`、`RagQueryResultDTO` 均为 Java 21 Record，定义在 `mro-common-dubbo` 中，直接 import。

**rag-service 向量数据库集成：** `queryVectorDb()` 方法是占位实现，返回空列表。接入真实向量数据库时（如 Milvus、Qdrant、Weaviate），在此方法中实现 HTTP 调用逻辑，接口签名不变，对调用方透明。

**端口规划：**
- vr-ar-training-service: HTTP 8091 / Dubbo 20889
- paperless-checkin-service: HTTP 8092 / Dubbo 20890
- rag-service: HTTP 8093 / Dubbo 20891

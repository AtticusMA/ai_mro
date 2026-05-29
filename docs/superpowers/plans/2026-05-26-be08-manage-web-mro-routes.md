# BE-08: manage-web MRO HTTP 路由层实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在 manage-web BFF（BE-04 已完成骨架）中新增 MRO 业务的全部 HTTP 路由控制器，将前端 HTTP 请求转发给各 MRO Dubbo 微服务，实现完整的前后端联调入口。

**Architecture:** manage-web 无数据库访问，所有 MRO 数据通过 `@DubboReference` 注入的 Dubbo Consumer 获取。Controller 层负责入参校验、用户上下文提取（从 `UserContextHolder`）、调用 Dubbo 接口，以及将结果包装为 `Result<T>` 统一返回。

**Tech Stack:** Java 21 · Spring Boot 3.3 · Dubbo 3.3（Consumer）· Jakarta Validation · Knife4j（Swagger UI）

**前置条件：** BE-04 完成（manage-web 骨架），BE-05/06/07 完成（全部 MRO Dubbo Provider）

---

## 文件结构（全部新增至 manage-web 模块）

```
mro-backend/manage-web/src/main/java/com/mro/manageweb/
└── controller/
    ├── mro/
    │   ├── AircraftHealthController.java       ← aircraft-health-service HTTP 入口
    │   ├── ArMaintenanceController.java        ← ar-maintenance-service HTTP 入口
    │   ├── FaultDiagnosisController.java       ← fault-diagnosis-service HTTP 入口
    │   ├── MaintenanceManualController.java    ← maintenance-manual-service HTTP 入口
    │   ├── DigitalTwinController.java          ← digital-twin-service HTTP 入口
    │   ├── ToolingMaterialController.java      ← tooling-material-service HTTP 入口
    │   ├── VrArTrainingController.java         ← vr-ar-training-service HTTP 入口
    │   ├── PaperlessCheckinController.java     ← paperless-checkin-service HTTP 入口
    │   └── RagController.java                  ← rag-service HTTP 入口
    └── request/mro/                            ← 各控制器请求体 Record
        ├── SaveHealthRecordRequest.java
        ├── CreateAlertRequest.java
        ├── CloseAlertRequest.java
        ├── CreateArTaskRequest.java
        ├── CreateFaultSessionRequest.java
        ├── AddFaultMessageRequest.java
        ├── CreateManualRequest.java
        ├── RegisterTwinModelRequest.java
        ├── RecordDataPointRequest.java
        ├── CreateToolRequest.java
        ├── CreateMaterialRequest.java
        ├── CreateTrainingCourseRequest.java
        ├── SubmitTrainingResultRequest.java
        ├── CreateWorkCardRequest.java
        ├── WorkCardCheckinRequest.java
        └── CreateKnowledgeBaseRequest.java
```

---

## Task 1: manage-web 新增 MRO Dubbo Consumer 配置

**Files:**
- Modify: `mro-backend/manage-web/pom.xml`（确认依赖已包含所需接口）
- Create: `mro-backend/manage-web/src/main/java/com/mro/manageweb/config/MroDubboConsumerConfig.java`

- [ ] **Step 1: 确认 manage-web POM 已依赖 mro-common-dubbo**

检查 `mro-backend/manage-web/pom.xml` 是否包含：
```xml
<dependency>
  <groupId>com.mro</groupId>
  <artifactId>mro-common-dubbo</artifactId>
</dependency>
```
若缺失，补充进去（BE-04 应已加入，此步骤为确认）。

- [ ] **Step 2: 创建 MRO Dubbo Consumer 配置类**

该类仅用于注册 Dubbo Reference Bean，方便 IDE 感知依赖，实际使用时直接在 Controller 中注入。

```java
// src/main/java/com/mro/manageweb/config/MroDubboConsumerConfig.java
package com.mro.manageweb.config;

import com.mro.common.dubbo.service.*;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.context.annotation.Configuration;

/**
 * MRO Dubbo Consumer 声明（集中管理，避免每个 Controller 重复注解参数）
 * 各 Controller 通过 @DubboReference 直接注入，此类仅起文档作用。
 */
@Configuration
public class MroDubboConsumerConfig {
    // Dubbo Consumer 在 Controller 层通过 @DubboReference 注入
    // version = "1.0.0", group = "mro", check = false, timeout = 5000, retries = 0
}
```

- [ ] **Step 3: 写一个占位 Controller 测试（MockMvc）**

```java
// src/test/java/com/mro/manageweb/controller/mro/AircraftHealthControllerTest.java
package com.mro.manageweb.controller.mro;

import com.mro.common.dubbo.service.AircraftHealthDubboService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AircraftHealthController.class)
class AircraftHealthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AircraftHealthDubboService aircraftHealthDubboService;

    @Test
    void test_listActiveAlerts_withAircraftNo_returns200() throws Exception {
        mockMvc.perform(get("/api/mro/aircraft-health/alerts/active")
                        .param("aircraftNo", "B-1234")
                        .header("X-User-Id", "1"))
                .andExpect(status().isOk());
    }
}
```

- [ ] **Step 4: 运行测试确认失败**

```bash
mvn test -pl manage-web -Dtest=AircraftHealthControllerTest -q
```
期望：FAIL（Controller 不存在）

- [ ] **Step 5: Commit（配置骨架）**

```bash
git add mro-backend/manage-web/src/main/java/com/mro/manageweb/config/MroDubboConsumerConfig.java
git commit -m "feat: add MRO dubbo consumer config skeleton in manage-web

Refs: BE-08"
```

---

## Task 2: AircraftHealthController + ArMaintenanceController

**Files:**
- Create: `mro-backend/manage-web/src/main/java/com/mro/manageweb/controller/mro/AircraftHealthController.java`
- Create: `mro-backend/manage-web/src/main/java/com/mro/manageweb/controller/mro/ArMaintenanceController.java`
- Create: `mro-backend/manage-web/src/main/java/com/mro/manageweb/controller/request/mro/SaveHealthRecordRequest.java`
- Create: `mro-backend/manage-web/src/main/java/com/mro/manageweb/controller/request/mro/CreateAlertRequest.java`
- Create: `mro-backend/manage-web/src/main/java/com/mro/manageweb/controller/request/mro/CloseAlertRequest.java`
- Create: `mro-backend/manage-web/src/main/java/com/mro/manageweb/controller/request/mro/CreateArTaskRequest.java`

- [ ] **Step 1: 创建 Request Records**

```java
// src/main/java/com/mro/manageweb/controller/request/mro/SaveHealthRecordRequest.java
package com.mro.manageweb.controller.request.mro;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record SaveHealthRecordRequest(
        @NotBlank String aircraftNo,
        @NotNull @DecimalMin("0") @DecimalMax("100") BigDecimal healthScore,
        @NotNull Integer status,
        String componentData,
        String remark
) {}
```

```java
// src/main/java/com/mro/manageweb/controller/request/mro/CreateAlertRequest.java
package com.mro.manageweb.controller.request.mro;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateAlertRequest(
        @NotBlank String aircraftNo,
        @NotBlank String alertType,
        @NotNull @Min(1) @Max(4) Integer alertLevel,
        @NotBlank String alertMsg,
        String componentCode
) {}
```

```java
// src/main/java/com/mro/manageweb/controller/request/mro/CloseAlertRequest.java
package com.mro.manageweb.controller.request.mro;

import jakarta.validation.constraints.NotNull;

public record CloseAlertRequest(
        @NotNull Long alertId,
        String handleRemark
) {}
```

```java
// src/main/java/com/mro/manageweb/controller/request/mro/CreateArTaskRequest.java
package com.mro.manageweb.controller.request.mro;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateArTaskRequest(
        @NotBlank String taskNo,
        @NotBlank String aircraftNo,
        @NotBlank String taskTitle,
        @NotBlank String taskType,
        @NotNull @Min(1) @Max(3) Integer priority,
        String arGuideUrl,
        String description
) {}
```

- [ ] **Step 2: 创建 AircraftHealthController**

```java
// src/main/java/com/mro/manageweb/controller/mro/AircraftHealthController.java
package com.mro.manageweb.controller.mro;

import com.mro.common.core.result.PageResult;
import com.mro.common.core.result.Result;
import com.mro.common.core.context.UserContextHolder;
import com.mro.common.dubbo.dto.aircraft.AircraftHealthAlertDTO;
import com.mro.common.dubbo.dto.aircraft.AircraftHealthRecordDTO;
import com.mro.common.dubbo.service.AircraftHealthDubboService;
import com.mro.manageweb.controller.request.mro.CloseAlertRequest;
import com.mro.manageweb.controller.request.mro.CreateAlertRequest;
import com.mro.manageweb.controller.request.mro.SaveHealthRecordRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mro/aircraft-health")
@Validated
public class AircraftHealthController {

    @DubboReference(version = "1.0.0", group = "mro", check = false, timeout = 5000, retries = 0)
    private AircraftHealthDubboService aircraftHealthDubboService;

    @PostMapping("/records")
    public Result<Long> saveHealthRecord(@Valid @RequestBody SaveHealthRecordRequest req) {
        Long id = aircraftHealthDubboService.saveHealthRecord(
                req.aircraftNo(), req.healthScore(), req.status(), req.componentData(), req.remark());
        return Result.ok(id);
    }

    @GetMapping("/records/latest")
    public Result<AircraftHealthRecordDTO> getLatestRecord(@RequestParam String aircraftNo) {
        return Result.ok(aircraftHealthDubboService.getLatestRecord(aircraftNo));
    }

    @GetMapping("/records")
    public Result<PageResult<AircraftHealthRecordDTO>> pageRecords(
            @RequestParam String aircraftNo,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return Result.ok(aircraftHealthDubboService.pageRecords(aircraftNo, pageNum, pageSize));
    }

    @PostMapping("/alerts")
    public Result<Long> createAlert(@Valid @RequestBody CreateAlertRequest req) {
        Long id = aircraftHealthDubboService.createAlert(
                req.aircraftNo(), req.alertType(), req.alertLevel(), req.alertMsg(), req.componentCode());
        return Result.ok(id);
    }

    @GetMapping("/alerts/active")
    public Result<List<AircraftHealthAlertDTO>> listActiveAlerts(@RequestParam String aircraftNo) {
        return Result.ok(aircraftHealthDubboService.listActiveAlerts(aircraftNo));
    }

    @PutMapping("/alerts/close")
    public Result<Void> closeAlert(@Valid @RequestBody CloseAlertRequest req) {
        Long userId = UserContextHolder.get() != null ? UserContextHolder.get().userId() : null;
        aircraftHealthDubboService.closeAlert(req.alertId(), userId, req.handleRemark());
        return Result.ok();
    }
}
```

- [ ] **Step 3: 创建 ArMaintenanceController**

```java
// src/main/java/com/mro/manageweb/controller/mro/ArMaintenanceController.java
package com.mro.manageweb.controller.mro;

import com.mro.common.core.context.UserContextHolder;
import com.mro.common.core.result.PageResult;
import com.mro.common.core.result.Result;
import com.mro.common.dubbo.dto.armaintenance.ArMaintenanceSessionDTO;
import com.mro.common.dubbo.dto.armaintenance.ArMaintenanceTaskDTO;
import com.mro.common.dubbo.service.ArMaintenanceDubboService;
import com.mro.manageweb.controller.request.mro.CreateArTaskRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mro/ar-maintenance")
@Validated
public class ArMaintenanceController {

    @DubboReference(version = "1.0.0", group = "mro", check = false, timeout = 5000, retries = 0)
    private ArMaintenanceDubboService arMaintenanceDubboService;

    @PostMapping("/tasks")
    public Result<Long> createTask(@Valid @RequestBody CreateArTaskRequest req) {
        Long createBy = UserContextHolder.get() != null ? UserContextHolder.get().userId() : null;
        Long id = arMaintenanceDubboService.createTask(
                req.taskNo(), req.aircraftNo(), req.taskTitle(), req.taskType(),
                req.priority(), req.arGuideUrl(), req.description(), createBy);
        return Result.ok(id);
    }

    @PutMapping("/tasks/{taskId}/assign")
    public Result<Void> assignTask(@PathVariable Long taskId,
                                    @RequestParam @NotNull Long assignedUser) {
        arMaintenanceDubboService.assignTask(taskId, assignedUser);
        return Result.ok();
    }

    @PutMapping("/tasks/{taskId}/complete")
    public Result<Void> completeTask(@PathVariable Long taskId) {
        arMaintenanceDubboService.completeTask(taskId);
        return Result.ok();
    }

    @GetMapping("/tasks")
    public Result<PageResult<ArMaintenanceTaskDTO>> pageTasks(
            @RequestParam(required = false) String aircraftNo,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return Result.ok(arMaintenanceDubboService.pageByAircraftNo(aircraftNo, status, pageNum, pageSize));
    }

    @PostMapping("/sessions/start")
    public Result<String> startSession(@RequestParam @NotNull Long taskId,
                                        @RequestParam(required = false) String deviceType) {
        Long userId = UserContextHolder.get() != null ? UserContextHolder.get().userId() : null;
        String token = arMaintenanceDubboService.startSession(taskId, userId, deviceType);
        return Result.ok(token);
    }

    @PutMapping("/sessions/end")
    public Result<Void> endSession(@RequestParam String sessionToken) {
        arMaintenanceDubboService.endSession(sessionToken);
        return Result.ok();
    }

    @GetMapping("/sessions")
    public Result<List<ArMaintenanceSessionDTO>> listSessions(@RequestParam @NotNull Long taskId) {
        return Result.ok(arMaintenanceDubboService.listSessions(taskId));
    }
}
```

- [ ] **Step 4: 运行 AircraftHealthControllerTest（应通过）**

```bash
mvn test -pl manage-web -Dtest=AircraftHealthControllerTest -q
```
期望：PASS（MockMvc GET 返回 200）

- [ ] **Step 5: Commit**

```bash
git add mro-backend/manage-web/src/main/java/com/mro/manageweb/controller/mro/AircraftHealthController.java
git add mro-backend/manage-web/src/main/java/com/mro/manageweb/controller/mro/ArMaintenanceController.java
git add mro-backend/manage-web/src/main/java/com/mro/manageweb/controller/request/mro/
git commit -m "feat: add aircraft-health and ar-maintenance HTTP routes in manage-web

Refs: BE-08"
```

---

## Task 3: FaultDiagnosisController + MaintenanceManualController

**Files:**
- Create: `mro-backend/manage-web/src/main/java/com/mro/manageweb/controller/mro/FaultDiagnosisController.java`
- Create: `mro-backend/manage-web/src/main/java/com/mro/manageweb/controller/mro/MaintenanceManualController.java`
- Create: request records: `CreateFaultSessionRequest.java`, `AddFaultMessageRequest.java`, `CreateManualRequest.java`

- [ ] **Step 1: 创建 Request Records**

```java
// src/main/java/com/mro/manageweb/controller/request/mro/CreateFaultSessionRequest.java
package com.mro.manageweb.controller.request.mro;

import jakarta.validation.constraints.NotBlank;

public record CreateFaultSessionRequest(
        String aircraftNo,
        String faultCategory,
        @NotBlank String title
) {}
```

```java
// src/main/java/com/mro/manageweb/controller/request/mro/AddFaultMessageRequest.java
package com.mro.manageweb.controller.request.mro;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record AddFaultMessageRequest(
        @NotNull Long sessionId,
        @NotBlank @Pattern(regexp = "user|assistant") String role,
        @NotBlank String content,
        Integer tokenCount
) {}
```

```java
// src/main/java/com/mro/manageweb/controller/request/mro/CreateManualRequest.java
package com.mro.manageweb.controller.request.mro;

import jakarta.validation.constraints.NotBlank;

public record CreateManualRequest(
        @NotBlank String manualCode,
        @NotBlank String manualName,
        String aircraftType,
        @NotBlank String version,
        String category,
        String fileUrl
) {}
```

- [ ] **Step 2: 创建 FaultDiagnosisController**

```java
// src/main/java/com/mro/manageweb/controller/mro/FaultDiagnosisController.java
package com.mro.manageweb.controller.mro;

import com.mro.common.core.context.UserContextHolder;
import com.mro.common.core.result.PageResult;
import com.mro.common.core.result.Result;
import com.mro.common.dubbo.dto.faultdiagnosis.FaultDiagnosisMessageDTO;
import com.mro.common.dubbo.dto.faultdiagnosis.FaultDiagnosisSessionDTO;
import com.mro.common.dubbo.service.FaultDiagnosisDubboService;
import com.mro.manageweb.controller.request.mro.AddFaultMessageRequest;
import com.mro.manageweb.controller.request.mro.CreateFaultSessionRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mro/fault-diagnosis")
@Validated
public class FaultDiagnosisController {

    @DubboReference(version = "1.0.0", group = "mro", check = false, timeout = 30000, retries = 0)
    private FaultDiagnosisDubboService faultDiagnosisDubboService;

    @PostMapping("/sessions")
    public Result<String> createSession(@Valid @RequestBody CreateFaultSessionRequest req) {
        Long userId = UserContextHolder.get() != null ? UserContextHolder.get().userId() : null;
        String sessionNo = faultDiagnosisDubboService.createSession(
                userId, req.aircraftNo(), req.faultCategory(), req.title());
        return Result.ok(sessionNo);
    }

    @PutMapping("/sessions/{sessionId}/end")
    public Result<Void> endSession(@PathVariable Long sessionId,
                                    @RequestParam(required = false) String conclusion) {
        faultDiagnosisDubboService.endSession(sessionId, conclusion);
        return Result.ok();
    }

    @PostMapping("/messages")
    public Result<Long> addMessage(@Valid @RequestBody AddFaultMessageRequest req) {
        Long id = faultDiagnosisDubboService.addMessage(
                req.sessionId(), req.role(), req.content(), req.tokenCount());
        return Result.ok(id);
    }

    @GetMapping("/sessions/{sessionId}/messages")
    public Result<List<FaultDiagnosisMessageDTO>> listMessages(@PathVariable Long sessionId) {
        return Result.ok(faultDiagnosisDubboService.listMessages(sessionId));
    }

    @GetMapping("/sessions")
    public Result<PageResult<FaultDiagnosisSessionDTO>> pageSessions(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        Long userId = UserContextHolder.get() != null ? UserContextHolder.get().userId() : null;
        return Result.ok(faultDiagnosisDubboService.pageByUserId(userId, pageNum, pageSize));
    }
}
```

- [ ] **Step 3: 创建 MaintenanceManualController**

```java
// src/main/java/com/mro/manageweb/controller/mro/MaintenanceManualController.java
package com.mro.manageweb.controller.mro;

import com.mro.common.core.context.UserContextHolder;
import com.mro.common.core.result.PageResult;
import com.mro.common.core.result.Result;
import com.mro.common.dubbo.dto.manual.ManualDTO;
import com.mro.common.dubbo.dto.manual.ManualSectionDTO;
import com.mro.common.dubbo.service.ManualDubboService;
import com.mro.manageweb.controller.request.mro.CreateManualRequest;
import jakarta.validation.Valid;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mro/manuals")
@Validated
public class MaintenanceManualController {

    @DubboReference(version = "1.0.0", group = "mro", check = false, timeout = 5000, retries = 0)
    private ManualDubboService manualDubboService;

    @PostMapping
    public Result<Long> createManual(@Valid @RequestBody CreateManualRequest req) {
        Long createBy = UserContextHolder.get() != null ? UserContextHolder.get().userId() : null;
        Long id = manualDubboService.createManual(req.manualCode(), req.manualName(),
                req.aircraftType(), req.version(), req.category(), req.fileUrl(), createBy);
        return Result.ok(id);
    }

    @GetMapping
    public Result<PageResult<ManualDTO>> pageManuals(
            @RequestParam(required = false) String aircraftType,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return Result.ok(manualDubboService.pageManuals(aircraftType, category, pageNum, pageSize));
    }

    @GetMapping("/{manualId}/sections")
    public Result<List<ManualSectionDTO>> listSections(@PathVariable Long manualId) {
        return Result.ok(manualDubboService.listSections(manualId));
    }

    @GetMapping("/sections/search")
    public Result<PageResult<ManualSectionDTO>> searchSections(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return Result.ok(manualDubboService.searchSections(keyword, pageNum, pageSize));
    }
}
```

- [ ] **Step 4: 编译验证**

```bash
mvn compile -pl manage-web -q
```
期望：BUILD SUCCESS

- [ ] **Step 5: Commit**

```bash
git add mro-backend/manage-web/src/main/java/com/mro/manageweb/controller/mro/FaultDiagnosisController.java
git add mro-backend/manage-web/src/main/java/com/mro/manageweb/controller/mro/MaintenanceManualController.java
git add mro-backend/manage-web/src/main/java/com/mro/manageweb/controller/request/mro/
git commit -m "feat: add fault-diagnosis and maintenance-manual HTTP routes in manage-web

Refs: BE-08"
```

---

## Task 4: DigitalTwinController + ToolingMaterialController

**Files:**
- Create: `mro-backend/manage-web/src/main/java/com/mro/manageweb/controller/mro/DigitalTwinController.java`
- Create: `mro-backend/manage-web/src/main/java/com/mro/manageweb/controller/mro/ToolingMaterialController.java`
- Create: `RegisterTwinModelRequest.java`, `RecordDataPointRequest.java`, `CreateToolRequest.java`, `CreateMaterialRequest.java`

- [ ] **Step 1: 创建 Request Records**

```java
// src/main/java/com/mro/manageweb/controller/request/mro/RegisterTwinModelRequest.java
package com.mro.manageweb.controller.request.mro;

import jakarta.validation.constraints.NotBlank;

public record RegisterTwinModelRequest(
        @NotBlank String modelCode,
        @NotBlank String modelName,
        @NotBlank String aircraftNo,
        String aircraftType,
        String modelFileUrl
) {}
```

```java
// src/main/java/com/mro/manageweb/controller/request/mro/RecordDataPointRequest.java
package com.mro.manageweb.controller.request.mro;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record RecordDataPointRequest(
        @NotNull Long modelId,
        @NotBlank String aircraftNo,
        @NotBlank String dataType,
        String componentCode,
        @NotNull BigDecimal value,
        String unit
) {}
```

```java
// src/main/java/com/mro/manageweb/controller/request/mro/CreateToolRequest.java
package com.mro.manageweb.controller.request.mro;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateToolRequest(
        @NotBlank String toolCode,
        @NotBlank String toolName,
        String toolType,
        String specification,
        @NotNull @Min(0) Integer totalQty,
        String unit,
        String location
) {}
```

```java
// src/main/java/com/mro/manageweb/controller/request/mro/CreateMaterialRequest.java
package com.mro.manageweb.controller.request.mro;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CreateMaterialRequest(
        @NotBlank String partNo,
        @NotBlank String partName,
        String aircraftType,
        String category,
        @NotNull @Min(0) Integer stockQty,
        @NotNull @Min(0) Integer minStock,
        String unit,
        BigDecimal unitPrice,
        String supplier
) {}
```

- [ ] **Step 2: 创建 DigitalTwinController**

```java
// src/main/java/com/mro/manageweb/controller/mro/DigitalTwinController.java
package com.mro.manageweb.controller.mro;

import com.mro.common.core.result.Result;
import com.mro.common.dubbo.dto.digitaltwin.DigitalTwinDataPointDTO;
import com.mro.common.dubbo.dto.digitaltwin.DigitalTwinModelDTO;
import com.mro.common.dubbo.service.DigitalTwinDubboService;
import com.mro.manageweb.controller.request.mro.RecordDataPointRequest;
import com.mro.manageweb.controller.request.mro.RegisterTwinModelRequest;
import jakarta.validation.Valid;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mro/digital-twin")
@Validated
public class DigitalTwinController {

    @DubboReference(version = "1.0.0", group = "mro", check = false, timeout = 5000, retries = 0)
    private DigitalTwinDubboService digitalTwinDubboService;

    @PostMapping("/models")
    public Result<Long> registerModel(@Valid @RequestBody RegisterTwinModelRequest req) {
        Long id = digitalTwinDubboService.registerModel(req.modelCode(), req.modelName(),
                req.aircraftNo(), req.aircraftType(), req.modelFileUrl());
        return Result.ok(id);
    }

    @GetMapping("/models")
    public Result<DigitalTwinModelDTO> getModelByAircraftNo(@RequestParam String aircraftNo) {
        return Result.ok(digitalTwinDubboService.getModelByAircraftNo(aircraftNo));
    }

    @PostMapping("/data-points")
    public Result<Long> recordDataPoint(@Valid @RequestBody RecordDataPointRequest req) {
        Long id = digitalTwinDubboService.recordDataPoint(req.modelId(), req.aircraftNo(),
                req.dataType(), req.componentCode(), req.value(), req.unit());
        return Result.ok(id);
    }

    @GetMapping("/data-points")
    public Result<List<DigitalTwinDataPointDTO>> getLatestDataPoints(
            @RequestParam String aircraftNo,
            @RequestParam String dataType,
            @RequestParam(defaultValue = "50") int limit) {
        return Result.ok(digitalTwinDubboService.getLatestDataPoints(aircraftNo, dataType, limit));
    }

    @GetMapping("/anomalies")
    public Result<List<DigitalTwinDataPointDTO>> getAnomalies(@RequestParam String aircraftNo) {
        return Result.ok(digitalTwinDubboService.getAnomalies(aircraftNo));
    }
}
```

- [ ] **Step 3: 创建 ToolingMaterialController**

```java
// src/main/java/com/mro/manageweb/controller/mro/ToolingMaterialController.java
package com.mro.manageweb.controller.mro;

import com.mro.common.core.result.PageResult;
import com.mro.common.core.result.Result;
import com.mro.common.dubbo.dto.tooling.MaterialItemDTO;
import com.mro.common.dubbo.dto.tooling.ToolingItemDTO;
import com.mro.common.dubbo.service.ToolingMaterialDubboService;
import com.mro.manageweb.controller.request.mro.CreateMaterialRequest;
import com.mro.manageweb.controller.request.mro.CreateToolRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mro/tooling-material")
@Validated
public class ToolingMaterialController {

    @DubboReference(version = "1.0.0", group = "mro", check = false, timeout = 5000, retries = 0)
    private ToolingMaterialDubboService toolingMaterialDubboService;

    @PostMapping("/tools")
    public Result<Long> createTool(@Valid @RequestBody CreateToolRequest req) {
        Long id = toolingMaterialDubboService.createTool(req.toolCode(), req.toolName(),
                req.toolType(), req.specification(), req.totalQty(), req.unit(), req.location());
        return Result.ok(id);
    }

    @GetMapping("/tools")
    public Result<PageResult<ToolingItemDTO>> pageTools(
            @RequestParam(required = false) String toolType,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return Result.ok(toolingMaterialDubboService.pageTools(toolType, pageNum, pageSize));
    }

    @PutMapping("/tools/{toolId}/borrow")
    public Result<Void> borrowTool(@PathVariable Long toolId,
                                    @RequestParam @NotNull @Min(1) Integer qty) {
        toolingMaterialDubboService.borrowTool(toolId, qty);
        return Result.ok();
    }

    @PutMapping("/tools/{toolId}/return")
    public Result<Void> returnTool(@PathVariable Long toolId,
                                    @RequestParam @NotNull @Min(1) Integer qty) {
        toolingMaterialDubboService.returnTool(toolId, qty);
        return Result.ok();
    }

    @PostMapping("/materials")
    public Result<Long> createMaterial(@Valid @RequestBody CreateMaterialRequest req) {
        Long id = toolingMaterialDubboService.createMaterial(req.partNo(), req.partName(),
                req.aircraftType(), req.category(), req.stockQty(), req.minStock(),
                req.unit(), req.unitPrice(), req.supplier());
        return Result.ok(id);
    }

    @GetMapping("/materials")
    public Result<PageResult<MaterialItemDTO>> pageMaterials(
            @RequestParam(required = false) String aircraftType,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return Result.ok(toolingMaterialDubboService.pageMaterials(aircraftType, category, pageNum, pageSize));
    }

    @GetMapping("/materials/low-stock")
    public Result<List<MaterialItemDTO>> getLowStockItems() {
        return Result.ok(toolingMaterialDubboService.getLowStockItems());
    }

    @PutMapping("/materials/{materialId}/consume")
    public Result<Void> consumeMaterial(@PathVariable Long materialId,
                                         @RequestParam @NotNull @Min(1) Integer qty) {
        toolingMaterialDubboService.consumeMaterial(materialId, qty);
        return Result.ok();
    }

    @PutMapping("/materials/{materialId}/replenish")
    public Result<Void> replenishMaterial(@PathVariable Long materialId,
                                           @RequestParam @NotNull @Min(1) Integer qty) {
        toolingMaterialDubboService.replenishMaterial(materialId, qty);
        return Result.ok();
    }
}
```

- [ ] **Step 4: 编译验证**

```bash
mvn compile -pl manage-web -q
```
期望：BUILD SUCCESS

- [ ] **Step 5: Commit**

```bash
git add mro-backend/manage-web/src/main/java/com/mro/manageweb/controller/mro/DigitalTwinController.java
git add mro-backend/manage-web/src/main/java/com/mro/manageweb/controller/mro/ToolingMaterialController.java
git add mro-backend/manage-web/src/main/java/com/mro/manageweb/controller/request/mro/
git commit -m "feat: add digital-twin and tooling-material HTTP routes in manage-web

Refs: BE-08"
```

---

## Task 5: VrArTrainingController + PaperlessCheckinController + RagController

**Files:**
- Create: `mro-backend/manage-web/src/main/java/com/mro/manageweb/controller/mro/VrArTrainingController.java`
- Create: `mro-backend/manage-web/src/main/java/com/mro/manageweb/controller/mro/PaperlessCheckinController.java`
- Create: `mro-backend/manage-web/src/main/java/com/mro/manageweb/controller/mro/RagController.java`
- Create: `CreateTrainingCourseRequest.java`, `SubmitTrainingResultRequest.java`, `CreateWorkCardRequest.java`, `WorkCardCheckinRequest.java`, `CreateKnowledgeBaseRequest.java`

- [ ] **Step 1: 创建 Request Records**

```java
// src/main/java/com/mro/manageweb/controller/request/mro/CreateTrainingCourseRequest.java
package com.mro.manageweb.controller.request.mro;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CreateTrainingCourseRequest(
        @NotBlank String courseCode,
        @NotBlank String courseName,
        @NotBlank String courseType,
        String aircraftType,
        String category,
        Integer durationMin,
        @NotNull @DecimalMin("0") BigDecimal passScore,
        String resourceUrl
) {}
```

```java
// src/main/java/com/mro/manageweb/controller/request/mro/SubmitTrainingResultRequest.java
package com.mro.manageweb.controller.request.mro;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record SubmitTrainingResultRequest(
        @NotNull Long recordId,
        @NotNull @DecimalMin("0") @DecimalMax("100") BigDecimal score,
        Integer durationMin
) {}
```

```java
// src/main/java/com/mro/manageweb/controller/request/mro/CreateWorkCardRequest.java
package com.mro.manageweb.controller.request.mro;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public record CreateWorkCardRequest(
        @NotBlank String cardNo,
        @NotBlank String aircraftNo,
        @NotBlank String taskTitle,
        String taskType,
        String workArea,
        String assignedTeam,
        LocalDate planDate,
        String description
) {}
```

```java
// src/main/java/com/mro/manageweb/controller/request/mro/WorkCardCheckinRequest.java
package com.mro.manageweb.controller.request.mro;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record WorkCardCheckinRequest(
        @NotNull Long cardId,
        @NotBlank @Pattern(regexp = "START|STEP|COMPLETE") String checkinType,
        String stepName,
        String location,
        String signatureUrl
) {}
```

```java
// src/main/java/com/mro/manageweb/controller/request/mro/CreateKnowledgeBaseRequest.java
package com.mro.manageweb.controller.request.mro;

import jakarta.validation.constraints.NotBlank;

public record CreateKnowledgeBaseRequest(
        @NotBlank String kbCode,
        @NotBlank String kbName,
        @NotBlank String kbType,
        String aircraftType,
        String description
) {}
```

- [ ] **Step 2: 创建 VrArTrainingController**

```java
// src/main/java/com/mro/manageweb/controller/mro/VrArTrainingController.java
package com.mro.manageweb.controller.mro;

import com.mro.common.core.context.UserContextHolder;
import com.mro.common.core.result.PageResult;
import com.mro.common.core.result.Result;
import com.mro.common.dubbo.dto.training.TrainingCourseDTO;
import com.mro.common.dubbo.dto.training.TrainingRecordDTO;
import com.mro.common.dubbo.service.VrArTrainingDubboService;
import com.mro.manageweb.controller.request.mro.CreateTrainingCourseRequest;
import com.mro.manageweb.controller.request.mro.SubmitTrainingResultRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mro/vr-ar-training")
@Validated
public class VrArTrainingController {

    @DubboReference(version = "1.0.0", group = "mro", check = false, timeout = 5000, retries = 0)
    private VrArTrainingDubboService vrArTrainingDubboService;

    @PostMapping("/courses")
    public Result<Long> createCourse(@Valid @RequestBody CreateTrainingCourseRequest req) {
        Long createBy = UserContextHolder.get() != null ? UserContextHolder.get().userId() : null;
        Long id = vrArTrainingDubboService.createCourse(
                req.courseCode(), req.courseName(), req.courseType(), req.aircraftType(),
                req.category(), req.durationMin(), req.passScore(), req.resourceUrl(), createBy);
        return Result.ok(id);
    }

    @GetMapping("/courses")
    public Result<PageResult<TrainingCourseDTO>> pageCourses(
            @RequestParam(required = false) String courseType,
            @RequestParam(required = false) String aircraftType,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return Result.ok(vrArTrainingDubboService.pageCourses(courseType, aircraftType, pageNum, pageSize));
    }

    @PostMapping("/records/start")
    public Result<Long> startTraining(@RequestParam @NotNull Long courseId) {
        Long userId = UserContextHolder.get() != null ? UserContextHolder.get().userId() : null;
        return Result.ok(vrArTrainingDubboService.startTraining(courseId, userId));
    }

    @PutMapping("/records/submit")
    public Result<Void> submitResult(@Valid @RequestBody SubmitTrainingResultRequest req) {
        vrArTrainingDubboService.submitResult(req.recordId(), req.score(), req.durationMin());
        return Result.ok();
    }

    @GetMapping("/records/my")
    public Result<List<TrainingRecordDTO>> myRecords() {
        Long userId = UserContextHolder.get() != null ? UserContextHolder.get().userId() : null;
        return Result.ok(vrArTrainingDubboService.listUserRecords(userId));
    }
}
```

- [ ] **Step 3: 创建 PaperlessCheckinController**

```java
// src/main/java/com/mro/manageweb/controller/mro/PaperlessCheckinController.java
package com.mro.manageweb.controller.mro;

import com.mro.common.core.context.UserContextHolder;
import com.mro.common.core.result.PageResult;
import com.mro.common.core.result.Result;
import com.mro.common.dubbo.dto.checkin.WorkCardCheckinDTO;
import com.mro.common.dubbo.dto.checkin.WorkCardDTO;
import com.mro.common.dubbo.service.PaperlessCheckinDubboService;
import com.mro.manageweb.controller.request.mro.CreateWorkCardRequest;
import com.mro.manageweb.controller.request.mro.WorkCardCheckinRequest;
import jakarta.validation.Valid;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mro/paperless-checkin")
@Validated
public class PaperlessCheckinController {

    @DubboReference(version = "1.0.0", group = "mro", check = false, timeout = 5000, retries = 0)
    private PaperlessCheckinDubboService paperlessCheckinDubboService;

    @PostMapping("/work-cards")
    public Result<Long> createWorkCard(@Valid @RequestBody CreateWorkCardRequest req) {
        Long createBy = UserContextHolder.get() != null ? UserContextHolder.get().userId() : null;
        Long id = paperlessCheckinDubboService.createWorkCard(
                req.cardNo(), req.aircraftNo(), req.taskTitle(), req.taskType(),
                req.workArea(), req.assignedTeam(), req.planDate(), req.description(), createBy);
        return Result.ok(id);
    }

    @GetMapping("/work-cards")
    public Result<PageResult<WorkCardDTO>> pageWorkCards(
            @RequestParam(required = false) String aircraftNo,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return Result.ok(paperlessCheckinDubboService.pageByAircraftNo(aircraftNo, status, pageNum, pageSize));
    }

    @PostMapping("/checkin")
    public Result<Long> checkin(@Valid @RequestBody WorkCardCheckinRequest req) {
        Long userId = UserContextHolder.get() != null ? UserContextHolder.get().userId() : null;
        Long id = paperlessCheckinDubboService.checkin(
                req.cardId(), userId, req.checkinType(), req.stepName(), req.location(), req.signatureUrl());
        return Result.ok(id);
    }

    @GetMapping("/work-cards/{cardId}/checkins")
    public Result<List<WorkCardCheckinDTO>> listCheckins(@PathVariable Long cardId) {
        return Result.ok(paperlessCheckinDubboService.listCheckins(cardId));
    }
}
```

- [ ] **Step 4: 创建 RagController**

```java
// src/main/java/com/mro/manageweb/controller/mro/RagController.java
package com.mro.manageweb.controller.mro;

import com.mro.common.core.context.UserContextHolder;
import com.mro.common.core.result.PageResult;
import com.mro.common.core.result.Result;
import com.mro.common.dubbo.dto.rag.KnowledgeBaseDTO;
import com.mro.common.dubbo.dto.rag.RagQueryResultDTO;
import com.mro.common.dubbo.service.RagDubboService;
import com.mro.manageweb.controller.request.mro.CreateKnowledgeBaseRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mro/rag")
@Validated
public class RagController {

    @DubboReference(version = "1.0.0", group = "mro", check = false, timeout = 30000, retries = 0)
    private RagDubboService ragDubboService;

    @PostMapping("/knowledge-bases")
    public Result<Long> createKnowledgeBase(@Valid @RequestBody CreateKnowledgeBaseRequest req) {
        Long createBy = UserContextHolder.get() != null ? UserContextHolder.get().userId() : null;
        Long id = ragDubboService.createKnowledgeBase(req.kbCode(), req.kbName(),
                req.kbType(), req.aircraftType(), req.description(), createBy);
        return Result.ok(id);
    }

    @GetMapping("/knowledge-bases")
    public Result<PageResult<KnowledgeBaseDTO>> pageKnowledgeBases(
            @RequestParam(required = false) String kbType,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return Result.ok(ragDubboService.pageKnowledgeBases(kbType, pageNum, pageSize));
    }

    @GetMapping("/query")
    public Result<List<RagQueryResultDTO>> query(
            @RequestParam(required = false) String kbCode,
            @RequestParam @NotBlank String q,
            @RequestParam(defaultValue = "5") int topK) {
        Long userId = UserContextHolder.get() != null ? UserContextHolder.get().userId() : null;
        return Result.ok(ragDubboService.query(userId, kbCode, q, topK));
    }
}
```

- [ ] **Step 5: 编译 manage-web 完整验证**

```bash
mvn compile -pl manage-web -q
```
期望：BUILD SUCCESS

- [ ] **Step 6: 运行所有 manage-web 测试**

```bash
mvn test -pl manage-web -q
```
期望：BUILD SUCCESS（所有已有测试通过）

- [ ] **Step 7: Commit**

```bash
git add mro-backend/manage-web/src/main/java/com/mro/manageweb/controller/mro/
git commit -m "feat: add vr-ar-training, paperless-checkin, rag HTTP routes in manage-web

Refs: BE-08"
```

---

## Task 6: 全量编译验证 + API 路由汇总

**Files:**
- No new files

- [ ] **Step 1: 全量编译 mro-backend 所有模块**

```bash
cd mro-backend
mvn compile -q
```
期望：BUILD SUCCESS（所有 13+ 模块编译通过）

- [ ] **Step 2: 全量测试**

```bash
cd mro-backend
mvn test -q
```
期望：BUILD SUCCESS（所有单元测试通过，测试覆盖 Service 层核心逻辑）

- [ ] **Step 3: 最终 Commit**

```bash
git add mro-backend/
git commit -m "feat: complete all backend service implementations (BE-01 to BE-08)

All 13 microservices implemented with TDD:
- Gateway + Auth + System + manage-web BFF
- aircraft-health, ar-maintenance, fault-diagnosis
- maintenance-manual, digital-twin, tooling-material
- vr-ar-training, paperless-checkin, rag-service

Refs: BE-01 BE-02 BE-03 BE-04 BE-05 BE-06 BE-07 BE-08"
```

---

## API 路由汇总（manage-web HTTP 接口，统一经 Gateway 代理）

| 模块 | 方法 | 路径 | 说明 |
|------|------|------|------|
| **飞机健康** | POST | /api/mro/aircraft-health/records | 上报健康记录 |
| | GET | /api/mro/aircraft-health/records/latest | 获取最新健康记录 |
| | GET | /api/mro/aircraft-health/records | 分页查询健康记录 |
| | POST | /api/mro/aircraft-health/alerts | 创建告警 |
| | GET | /api/mro/aircraft-health/alerts/active | 活跃告警列表 |
| | PUT | /api/mro/aircraft-health/alerts/close | 关闭告警 |
| **AR 维修** | POST | /api/mro/ar-maintenance/tasks | 创建任务 |
| | PUT | /api/mro/ar-maintenance/tasks/{id}/assign | 分配工程师 |
| | PUT | /api/mro/ar-maintenance/tasks/{id}/complete | 完成任务 |
| | GET | /api/mro/ar-maintenance/tasks | 分页查询任务 |
| | POST | /api/mro/ar-maintenance/sessions/start | 开启AR会话 |
| | PUT | /api/mro/ar-maintenance/sessions/end | 结束AR会话 |
| | GET | /api/mro/ar-maintenance/sessions | 查询会话列表 |
| **故障诊断** | POST | /api/mro/fault-diagnosis/sessions | 创建诊断会话 |
| | PUT | /api/mro/fault-diagnosis/sessions/{id}/end | 结束会话 |
| | POST | /api/mro/fault-diagnosis/messages | 发送消息 |
| | GET | /api/mro/fault-diagnosis/sessions/{id}/messages | 查询消息历史 |
| | GET | /api/mro/fault-diagnosis/sessions | 分页查询会话 |
| **维修手册** | POST | /api/mro/manuals | 创建手册 |
| | GET | /api/mro/manuals | 分页查询手册 |
| | GET | /api/mro/manuals/{id}/sections | 查询章节列表 |
| | GET | /api/mro/manuals/sections/search | 全文搜索章节 |
| **数字孪生** | POST | /api/mro/digital-twin/models | 注册孪生模型 |
| | GET | /api/mro/digital-twin/models | 按飞机号查询模型 |
| | POST | /api/mro/digital-twin/data-points | 上报数据点 |
| | GET | /api/mro/digital-twin/data-points | 查询最新数据点 |
| | GET | /api/mro/digital-twin/anomalies | 查询异常数据 |
| **工具物料** | POST | /api/mro/tooling-material/tools | 新增工具 |
| | GET | /api/mro/tooling-material/tools | 分页查询工具 |
| | PUT | /api/mro/tooling-material/tools/{id}/borrow | 借出工具 |
| | PUT | /api/mro/tooling-material/tools/{id}/return | 归还工具 |
| | POST | /api/mro/tooling-material/materials | 新增物料 |
| | GET | /api/mro/tooling-material/materials | 分页查询物料 |
| | GET | /api/mro/tooling-material/materials/low-stock | 低库存预警 |
| | PUT | /api/mro/tooling-material/materials/{id}/consume | 消耗物料 |
| | PUT | /api/mro/tooling-material/materials/{id}/replenish | 补充物料 |
| **VR/AR培训** | POST | /api/mro/vr-ar-training/courses | 创建课程 |
| | GET | /api/mro/vr-ar-training/courses | 分页查询课程 |
| | POST | /api/mro/vr-ar-training/records/start | 开始培训 |
| | PUT | /api/mro/vr-ar-training/records/submit | 提交成绩 |
| | GET | /api/mro/vr-ar-training/records/my | 我的培训记录 |
| **无纸化工卡** | POST | /api/mro/paperless-checkin/work-cards | 创建工卡 |
| | GET | /api/mro/paperless-checkin/work-cards | 分页查询工卡 |
| | POST | /api/mro/paperless-checkin/checkin | 工卡签到 |
| | GET | /api/mro/paperless-checkin/work-cards/{id}/checkins | 查询签到记录 |
| **RAG 检索** | POST | /api/mro/rag/knowledge-bases | 创建知识库 |
| | GET | /api/mro/rag/knowledge-bases | 分页查询知识库 |
| | GET | /api/mro/rag/query | 语义检索 |

---

## 关键提示

**UserContextHolder 使用：** 需要当前用户 ID 时，通过 `UserContextHolder.get().userId()` 获取。若 `UserContextHolder.get()` 返回 null（未登录或白名单路径），对需要 userId 的接口应返回 UNAUTHORIZED（由 gateway-service 的 AuthGlobalFilter 拦截，manage-web 正常情况下 UserContext 一定存在）。

**DubboReference 超时设置：** AI 相关接口（fault-diagnosis、rag）`timeout = 30000`；其他接口 `timeout = 5000`，`retries = 0`（幂等性由业务层保证，不做自动重试）。

**DTO Record 对齐：** 所有 Controller 方法入参和返回的 DTO 类均来自 `mro-common-dubbo`，直接 import，不要在 manage-web 层重新定义 DTO。Request Record 定义在 `manage-web` 内部的 `controller/request/mro/` 包中。

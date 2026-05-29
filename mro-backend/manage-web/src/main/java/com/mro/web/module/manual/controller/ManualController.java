package com.mro.web.module.manual.controller;

import com.mro.common.core.response.PageResult;
import com.mro.common.core.response.R;
import com.mro.common.dubbo.common.request.*;
import com.mro.common.dubbo.manual.request.*;
import com.mro.common.dubbo.manual.response.*;
import com.mro.web.module.manual.app.ManualAppService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/manuals")
@Validated
@RequiredArgsConstructor
public class ManualController {

    private final ManualAppService manualAppService;

    /** GET /api/manuals — 手册列表（分页/筛选） */
    @GetMapping
    public R<PageResult<ManualDocDTO>> listManuals(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String aircraftType,
            @RequestParam(required = false) String parsedStatus,
            @RequestParam(required = false) String manualNo) {
        return R.ok(manualAppService.listManuals(
                new ManualQueryParam(manualNo, aircraftType, parsedStatus, pageNum, pageSize)));
    }

    /** POST /api/manuals — 上传手册文档 (multipart/form-data) */
    @PostMapping(consumes = "multipart/form-data")
    public R<Map<String, Long>> uploadManual(
            @RequestPart("file") MultipartFile file,
            @RequestPart("title") @NotBlank String title,
            @RequestPart("manualNo") @NotBlank String manualNo,
            @RequestPart("aircraftType") @NotBlank String aircraftType,
            @RequestPart("format") @NotBlank String format) {
        Long id = manualAppService.uploadManual(file, title, manualNo, aircraftType, format);
        return R.ok(Map.of("id", id));
    }

    /** GET /api/manuals/{id} — 手册详情 */
    @GetMapping("/{id}")
    public R<ManualDocDTO> getManual(@PathVariable Long id) {
        return R.ok(manualAppService.getManual(id));
    }

    /** POST /api/manuals/{id}/parse — 触发深度解析 */
    @PostMapping("/{id}/parse")
    public R<Void> triggerParse(@PathVariable Long id) {
        manualAppService.triggerParse(id);
        return R.ok();
    }

    /** POST /api/manuals/{id}/publish — 发布手册 */
    @PostMapping("/{id}/publish")
    public R<Void> publishManual(@PathVariable Long id) {
        manualAppService.publishManual(id);
        return R.ok();
    }

    /** DELETE /api/manuals/{id} — 删除手册 */
    @DeleteMapping("/{id}")
    public R<Void> deleteManual(@PathVariable Long id) {
        manualAppService.deleteManual(id);
        return R.ok();
    }

    /** GET /api/manuals/{id}/versions — 版本历史 */
    @GetMapping("/{id}/versions")
    public R<PageResult<ManualVersionDTO>> listVersions(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return R.ok(manualAppService.listVersions(id, pageNum, pageSize));
    }

    /** POST /api/manuals/{id}/versions — 创建新版本（客户化修订） */
    @PostMapping("/{id}/versions")
    public R<Map<String, Long>> createVersion(
            @PathVariable Long id,
            @Valid @RequestBody CreateVersionCommand cmd) {
        Long versionId = manualAppService.createVersion(id, cmd);
        return R.ok(Map.of("versionId", versionId));
    }

    /** POST /api/manuals/{id}/translate — 提交翻译任务 */
    @PostMapping("/{id}/translate")
    public R<Map<String, Long>> submitTranslation(
            @PathVariable Long id,
            @RequestBody TranslateRequest req) {
        Long taskId = manualAppService.submitTranslation(
                id,
                req.sourceLang() != null ? req.sourceLang() : "en",
                req.targetLang() != null ? req.targetLang() : "zh");
        return R.ok(Map.of("taskId", taskId));
    }

    /** GET /api/manuals/translations/{taskId} — 获取翻译结果 */
    @GetMapping("/translations/{taskId}")
    public R<TranslationTaskDTO> getTranslationResult(@PathVariable Long taskId) {
        return R.ok(manualAppService.getTranslationResult(taskId));
    }

    /** GET /api/manuals/search — 全文搜索 */
    @GetMapping("/search")
    public R<PageResult<ManualSearchResultDTO>> searchManuals(
            @RequestParam("q") String query,
            @RequestParam(required = false) String aircraftType,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return R.ok(manualAppService.searchManuals(
                new ManualSearchParam(query, aircraftType, pageNum, pageSize)));
    }

    record TranslateRequest(String sourceLang, String targetLang) {}
}

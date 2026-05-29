package com.mro.web.module.tshoot.controller;

import com.mro.common.core.response.PageResult;
import com.mro.common.core.response.R;
import com.mro.web.module.tshoot.app.TshootAppService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.mro.common.dubbo.common.request.*;
import com.mro.common.dubbo.tshoot.request.*;
import com.mro.common.dubbo.tshoot.response.*;

@RestController
@RequestMapping("/api/tshoot")
@RequiredArgsConstructor
public class TshootController {

    private final TshootAppService tshootAppService;

    // ---- Knowledge Base ----

    @GetMapping("/knowledge-bases")
    public R<PageResult<KnowledgeBaseDTO>> listKnowledgeBases(PageQueryParam param) {
        return R.ok(tshootAppService.listKnowledgeBases(param));
    }

    @PostMapping("/knowledge-bases")
    public R<Long> createKnowledgeBase(@RequestBody CreateKbCommand cmd) {
        return R.ok(tshootAppService.createKnowledgeBase(cmd));
    }

    @PostMapping("/knowledge-bases/{kbId}/documents")
    public R<Long> uploadDocument(@PathVariable Long kbId,
                                       @RequestBody UploadDocCommand cmd) {
        return R.ok(tshootAppService.uploadDocument(kbId, cmd));
    }

    @DeleteMapping("/knowledge-bases/{kbId}/documents/{docId}")
    public R<Void> deleteDocument(@PathVariable Long kbId,
                                       @PathVariable Long docId) {
        tshootAppService.deleteDocument(kbId, docId);
        return R.ok();
    }

    // ---- Fault Queries ----

    @PostMapping("/queries")
    public R<Long> submitQuery(@RequestBody FaultQueryCommand cmd) {
        return R.ok(tshootAppService.submitQuery(cmd));
    }

    @GetMapping("/queries/{queryId}/result")
    public R<TshootResultDTO> getQueryResult(@PathVariable Long queryId) {
        return R.ok(tshootAppService.getQueryResult(queryId));
    }

    // ---- Reports ----

    @GetMapping("/reports")
    public R<PageResult<TshootReportDTO>> listMyReports(@RequestParam Long userId,
                                                              PageQueryParam param) {
        return R.ok(tshootAppService.listMyReports(userId, param));
    }

    @GetMapping("/reports/{reportId}")
    public R<TshootReportDTO> getReport(@PathVariable Long reportId) {
        return R.ok(tshootAppService.getReport(reportId));
    }

    // ---- Repair History ----

    @GetMapping("/history")
    public R<PageResult<RepairHistoryDTO>> listHistory(HistoryQueryParam param) {
        return R.ok(tshootAppService.listHistory(param));
    }

    @GetMapping("/statistics")
    public R<FaultStatisticsDTO> getStatistics(StatQueryParam param) {
        return R.ok(tshootAppService.getStatistics(param));
    }
}

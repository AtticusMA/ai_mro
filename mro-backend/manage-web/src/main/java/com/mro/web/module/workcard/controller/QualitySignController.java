package com.mro.web.module.workcard.controller;

import com.mro.common.core.response.PageResult;
import com.mro.common.core.response.R;
import com.mro.common.dubbo.workcard.request.QualitySignCommand;
import com.mro.common.dubbo.workcard.response.QualitySignRecordDTO;
import com.mro.common.dubbo.workcard.response.WorkcardDTO;
import com.mro.common.dubbo.workcard.response.WorkcardDetailDTO;
import com.mro.web.module.workcard.app.QualitySignAppService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 质检签署接口
 * Refs: MRO-008
 */
@RestController
@RequestMapping("/api/workcards")
@RequiredArgsConstructor
public class QualitySignController {

    private final QualitySignAppService qualitySignAppService;

    @GetMapping("/pending-sign")
    public R<PageResult<WorkcardDTO>> listPendingSign(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return R.ok(qualitySignAppService.listPendingSign(pageNum, pageSize));
    }

    @GetMapping("/{id}/quality-sign")
    public R<WorkcardDetailDTO> getQualitySignDetail(@PathVariable Long id) {
        return R.ok(qualitySignAppService.getQualitySignDetail(id));
    }

    @PostMapping("/{id}/quality-sign")
    public R<Long> submitQualitySign(@PathVariable Long id, @RequestBody QualitySignCommand cmd) {
        return R.ok(qualitySignAppService.submitQualitySign(id, cmd));
    }

    @GetMapping("/{workcardId}/sign-records")
    public R<List<QualitySignRecordDTO>> listSignRecords(@PathVariable Long workcardId) {
        return R.ok(qualitySignAppService.listSignRecords(workcardId));
    }

    @GetMapping("/sign-records/{id}")
    public R<QualitySignRecordDTO> getSignRecord(@PathVariable Long id) {
        return R.ok(qualitySignAppService.getSignRecord(id));
    }
}

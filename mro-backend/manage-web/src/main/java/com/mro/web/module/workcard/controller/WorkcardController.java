package com.mro.web.module.workcard.controller;

import com.mro.common.core.response.PageResult;
import com.mro.common.core.response.R;
import com.mro.common.dubbo.workcard.request.*;
import com.mro.common.dubbo.workcard.response.*;
import com.mro.web.module.workcard.app.WorkcardAppService;
import com.mro.web.module.workcard.app.WorkcardAppService.SignRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workcards")
@Validated
@RequiredArgsConstructor
public class WorkcardController {

    private final WorkcardAppService workcardAppService;

    @GetMapping
    public R<PageResult<WorkcardDTO>> listWorkcards(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String cardType,
            @RequestParam(required = false) String aircraftId,
            @RequestParam(required = false) String priority) {
        return R.ok(workcardAppService.listWorkcards(
                new WorkcardQueryParam(status, cardType, aircraftId, priority, pageNum, pageSize)));
    }

    @PostMapping
    public R<Long> createWorkcard(@Valid @RequestBody CreateWorkcardCommand cmd) {
        return R.ok(workcardAppService.createWorkcard(cmd));
    }

    @GetMapping("/{id}")
    public R<WorkcardDetailDTO> getWorkcard(@PathVariable Long id) {
        return R.ok(workcardAppService.getWorkcard(id));
    }

    @PutMapping("/{id}")
    public R<Void> updateWorkcard(@PathVariable Long id,
                                  @Valid @RequestBody UpdateWorkcardCommand cmd) {
        workcardAppService.updateWorkcard(id, cmd);
        return R.ok();
    }

    @PostMapping("/{id}/submit")
    public R<Void> submitForApproval(@PathVariable Long id) {
        workcardAppService.submitForApproval(id);
        return R.ok();
    }

    @PostMapping("/{id}/approve")
    public R<Void> approveWorkcard(@PathVariable Long id,
                                   @RequestParam String action,
                                   @RequestParam(required = false) String comment) {
        workcardAppService.approveWorkcard(id, action, comment);
        return R.ok();
    }

    @PostMapping("/{id}/issue")
    public R<Void> issueWorkcard(@PathVariable Long id) {
        workcardAppService.issueWorkcard(id);
        return R.ok();
    }

    @PutMapping("/{id}/steps/{stepId}/complete")
    public R<Void> completeStep(@PathVariable Long id, @PathVariable Long stepId) {
        workcardAppService.completeStep(id, stepId);
        return R.ok();
    }

    @PostMapping("/{id}/sign")
    public R<SignatureResultDTO> signWorkcard(@PathVariable Long id,
                                              @Valid @RequestBody SignRequest req) {
        return R.ok(workcardAppService.signWorkcard(id, req));
    }

    @GetMapping("/{id}/signatures")
    public R<List<SignatureDTO>> getSignatures(@PathVariable Long id) {
        return R.ok(workcardAppService.getSignatures(id));
    }

    @GetMapping("/{id}/blockchain-verify")
    public R<BlockchainVerifyDTO> verifyBlockchain(@PathVariable Long id) {
        return R.ok(workcardAppService.verifyBlockchain(id));
    }

    @GetMapping("/progress")
    public R<WorkcardProgressDTO> getProgress() {
        return R.ok(workcardAppService.getProgress());
    }

    @GetMapping("/alerts")
    public R<PageResult<WorkcardAlertDTO>> getAlerts(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return R.ok(workcardAppService.getAlerts(pageNum, pageSize));
    }
}

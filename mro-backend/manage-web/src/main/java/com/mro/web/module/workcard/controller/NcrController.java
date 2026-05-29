package com.mro.web.module.workcard.controller;

import com.mro.common.core.response.PageResult;
import com.mro.common.core.response.R;
import com.mro.common.dubbo.workcard.request.CreateNcrCommand;
import com.mro.common.dubbo.workcard.response.NcrDTO;
import com.mro.web.module.workcard.app.NcrAppService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * NCR (Non-Conformance Report) endpoints per MRO-008 spec.
 * Refs: MRO-008
 */
@RestController
@RequestMapping("/api/ncr")
@RequiredArgsConstructor
public class NcrController {

    private final NcrAppService ncrAppService;

    @GetMapping
    public R<PageResult<NcrDTO>> listNcrs(
            @RequestParam(required = false) Long workcardId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return R.ok(ncrAppService.listNcrs(workcardId, status, pageNum, pageSize));
    }

    @PostMapping
    public R<Long> createNcr(@RequestBody CreateNcrCommand cmd) {
        return R.ok(ncrAppService.createNcr(cmd));
    }

    @GetMapping("/{id}")
    public R<NcrDTO> getNcr(@PathVariable Long id) {
        return R.ok(ncrAppService.getNcr(id));
    }

    @PostMapping("/{id}/close")
    public R<Void> closeNcr(@PathVariable Long id, @RequestBody Map<String, String> body) {
        ncrAppService.closeNcr(id, body.get("closeSignature"));
        return R.ok();
    }
}

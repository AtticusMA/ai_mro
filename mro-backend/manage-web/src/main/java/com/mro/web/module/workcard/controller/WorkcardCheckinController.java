package com.mro.web.module.workcard.controller;

import com.mro.common.core.response.R;
import com.mro.common.dubbo.workcard.request.CheckinCommand;
import com.mro.common.dubbo.workcard.response.WorkcardCheckinDTO;
import com.mro.web.module.workcard.app.WorkcardCheckinAppService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 工卡签到接口
 * Refs: MRO-008
 */
@RestController
@RequestMapping("/api/workcards")
@RequiredArgsConstructor
public class WorkcardCheckinController {

    private final WorkcardCheckinAppService workcardCheckinAppService;

    @PostMapping("/{id}/checkin")
    public R<Long> checkIn(@PathVariable Long id, @RequestBody CheckinCommand cmd) {
        return R.ok(workcardCheckinAppService.checkIn(id, cmd));
    }

    @PostMapping("/{id}/checkout")
    public R<Void> checkOut(@PathVariable Long id) {
        workcardCheckinAppService.checkOut(id);
        return R.ok();
    }

    @GetMapping("/{workcardId}/checkins")
    public R<List<WorkcardCheckinDTO>> listCheckins(@PathVariable Long workcardId) {
        return R.ok(workcardCheckinAppService.listCheckins(workcardId));
    }
}

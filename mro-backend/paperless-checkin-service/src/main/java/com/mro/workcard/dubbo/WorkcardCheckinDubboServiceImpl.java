package com.mro.workcard.dubbo;

import com.mro.common.dubbo.workcard.request.CheckinCommand;
import com.mro.common.dubbo.workcard.response.WorkcardCheckinDTO;
import com.mro.common.dubbo.workcard.service.WorkcardCheckinDubboService;
import com.mro.workcard.service.WorkcardCheckinService;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;

import java.util.List;

/**
 * 工卡签到 Dubbo 实现
 * Refs: MRO-008
 */
@DubboService(version = "1.0.0")
@RequiredArgsConstructor
public class WorkcardCheckinDubboServiceImpl implements WorkcardCheckinDubboService {

    private final WorkcardCheckinService workcardCheckinService;

    @Override
    public Long checkIn(CheckinCommand cmd, Long userId) {
        return workcardCheckinService.checkIn(cmd, userId);
    }

    @Override
    public void checkOut(Long workcardId, Long userId) {
        workcardCheckinService.checkOut(workcardId, userId);
    }

    @Override
    public List<WorkcardCheckinDTO> listCheckins(Long workcardId) {
        return workcardCheckinService.listCheckins(workcardId);
    }
}

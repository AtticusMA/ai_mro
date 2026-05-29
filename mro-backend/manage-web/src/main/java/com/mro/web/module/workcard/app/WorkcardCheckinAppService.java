package com.mro.web.module.workcard.app;

import com.mro.common.dubbo.workcard.request.CheckinCommand;
import com.mro.common.dubbo.workcard.response.WorkcardCheckinDTO;
import com.mro.common.dubbo.workcard.service.WorkcardCheckinDubboService;
import com.mro.web.context.UserContext;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 工卡签到应用服务
 * Refs: MRO-008
 */
@Service
@RequiredArgsConstructor
public class WorkcardCheckinAppService {

    @DubboReference(version = "1.0.0", timeout = 5000, retries = 0)
    private WorkcardCheckinDubboService workcardCheckinDubboService;

    public Long checkIn(Long workcardId, CheckinCommand cmd) {
        CheckinCommand withId = new CheckinCommand(workcardId, cmd.location(), cmd.deviceId());
        return workcardCheckinDubboService.checkIn(withId, UserContext.getUserId());
    }

    public void checkOut(Long workcardId) {
        workcardCheckinDubboService.checkOut(workcardId, UserContext.getUserId());
    }

    public List<WorkcardCheckinDTO> listCheckins(Long workcardId) {
        return workcardCheckinDubboService.listCheckins(workcardId);
    }
}

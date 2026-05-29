package com.mro.common.dubbo.workcard.service;

import com.mro.common.dubbo.workcard.request.CheckinCommand;
import com.mro.common.dubbo.workcard.response.WorkcardCheckinDTO;

import java.util.List;

/**
 * 工卡签到 Dubbo 接口
 * Refs: MRO-008
 */
public interface WorkcardCheckinDubboService {

    Long checkIn(CheckinCommand cmd, Long userId);

    void checkOut(Long workcardId, Long userId);

    List<WorkcardCheckinDTO> listCheckins(Long workcardId);
}

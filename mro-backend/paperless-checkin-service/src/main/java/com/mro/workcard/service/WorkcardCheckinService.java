package com.mro.workcard.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.mro.common.core.exception.BizException;
import com.mro.common.dubbo.workcard.request.CheckinCommand;
import com.mro.common.dubbo.workcard.response.WorkcardCheckinDTO;
import com.mro.workcard.domain.entity.WorkcardCheckin;
import com.mro.workcard.mapper.WorkcardCheckinMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkcardCheckinService {

    private final WorkcardCheckinMapper workcardCheckinMapper;

    public Long checkIn(CheckinCommand cmd, Long userId) {
        WorkcardCheckin checkin = new WorkcardCheckin();
        checkin.setWorkcardId(cmd.workcardId());
        checkin.setUserId(userId);
        checkin.setCheckInTime(LocalDateTime.now());
        checkin.setLocation(cmd.location());
        checkin.setDeviceId(cmd.deviceId());
        workcardCheckinMapper.insert(checkin);
        return checkin.getId();
    }

    @Transactional
    public void checkOut(Long workcardId, Long userId) {
        WorkcardCheckin active = workcardCheckinMapper.selectOne(
                new LambdaQueryWrapper<WorkcardCheckin>()
                        .eq(WorkcardCheckin::getWorkcardId, workcardId)
                        .eq(WorkcardCheckin::getUserId, userId)
                        .isNull(WorkcardCheckin::getCheckOutTime)
                        .orderByDesc(WorkcardCheckin::getCreateTime)
                        .last("LIMIT 1"));
        if (active == null) {
            throw new BizException(4916, "未找到签到记录，请先签到");
        }
        workcardCheckinMapper.update(null, new LambdaUpdateWrapper<WorkcardCheckin>()
                .set(WorkcardCheckin::getCheckOutTime, LocalDateTime.now())
                .eq(WorkcardCheckin::getId, active.getId()));
    }

    public List<WorkcardCheckinDTO> listCheckins(Long workcardId) {
        List<WorkcardCheckin> list = workcardCheckinMapper.selectList(
                new LambdaQueryWrapper<WorkcardCheckin>()
                        .eq(WorkcardCheckin::getWorkcardId, workcardId)
                        .orderByDesc(WorkcardCheckin::getCheckInTime));
        return list.stream().map(this::toDTO).collect(Collectors.toList());
    }

    private WorkcardCheckinDTO toDTO(WorkcardCheckin c) {
        return new WorkcardCheckinDTO(c.getId(), c.getWorkcardId(), c.getUserId(),
                null, c.getCheckInTime(), c.getCheckOutTime(), c.getLocation(), c.getDeviceId());
    }
}

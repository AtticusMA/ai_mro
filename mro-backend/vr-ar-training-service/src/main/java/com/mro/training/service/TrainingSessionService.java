package com.mro.training.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mro.common.core.exception.BizException;
import com.mro.common.core.response.PageResult;
import com.mro.common.dubbo.training.request.*;
import com.mro.common.dubbo.training.response.*;
import com.mro.training.domain.entity.TrainingScenario;
import com.mro.training.domain.entity.TrainingSession;
import com.mro.training.mapper.TrainingScenarioMapper;
import com.mro.training.mapper.TrainingSessionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TrainingSessionService {

    private static final int ERR_SESSION_NOT_FOUND      = 4803;
    private static final int ERR_SESSION_IN_PROGRESS    = 4804;
    private static final int ERR_MAX_COLLABORATIVE      = 4805;
    private static final int MAX_COLLABORATIVE_TRAINEES = 10;

    private final TrainingSessionMapper sessionMapper;
    private final TrainingScenarioMapper scenarioMapper;

    @Transactional
    public List<Long> createSessions(CreateTrainingSessionCommand cmd) {
        if ("collaborative".equals(cmd.mode()) && cmd.traineeIds().size() > MAX_COLLABORATIVE_TRAINEES) {
            throw new BizException(ERR_MAX_COLLABORATIVE, "协同培训人数超出上限（最大10人）");
        }
        List<Long> sessionIds = new ArrayList<>();
        for (Long traineeId : cmd.traineeIds()) {
            TrainingSession session = new TrainingSession();
            session.setScenarioId(cmd.scenarioId());
            session.setTraineeId(traineeId);
            session.setMode(cmd.mode());
            session.setStartedAt(cmd.scheduledAt() != null ? cmd.scheduledAt() : Instant.now());
            session.setStatus("in_progress");
            session.setAssignedBy(cmd.assignedBy());
            session.setCreatedAt(LocalDateTime.now());
            session.setUpdatedAt(LocalDateTime.now());
            sessionMapper.insert(session);
            sessionIds.add(session.getId());
        }
        return sessionIds;
    }

    public PageResult<TrainingSessionDTO> listSessions(SessionQueryParam param) {
        LambdaQueryWrapper<TrainingSession> wrapper = new LambdaQueryWrapper<>();
        if (param.scenarioId() != null) wrapper.eq(TrainingSession::getScenarioId, param.scenarioId());
        if (param.traineeId() != null) wrapper.eq(TrainingSession::getTraineeId, param.traineeId());
        if (param.status() != null) wrapper.eq(TrainingSession::getStatus, param.status());
        if (param.mode() != null) wrapper.eq(TrainingSession::getMode, param.mode());
        wrapper.orderByDesc(TrainingSession::getStartedAt);
        Page<TrainingSession> page = sessionMapper.selectPage(
                new Page<>(param.pageNum(), param.pageSize()), wrapper);
        List<TrainingSessionDTO> dtos = page.getRecords().stream().map(this::toDTO).toList();
        return PageResult.of(dtos, page.getTotal(), param.pageNum(), param.pageSize());
    }

    public TrainingSessionDTO getSession(Long sessionId) {
        TrainingSession session = sessionMapper.selectById(sessionId);
        if (session == null) throw new BizException(ERR_SESSION_NOT_FOUND, "培训会话不存在");
        return toDTO(session);
    }

    public TrainingSession getEntityById(Long sessionId) {
        TrainingSession session = sessionMapper.selectById(sessionId);
        if (session == null) throw new BizException(ERR_SESSION_NOT_FOUND, "培训会话不存在");
        return session;
    }

    private TrainingSessionDTO toDTO(TrainingSession s) {
        TrainingScenario scenario = scenarioMapper.selectById(s.getScenarioId());
        String scenarioName = scenario != null ? scenario.getName() : null;
        return new TrainingSessionDTO(s.getId(), s.getScenarioId(), scenarioName,
                s.getTraineeId(), null, s.getMode(),
                s.getStartedAt(), s.getEndedAt(), s.getStatus());
    }
}

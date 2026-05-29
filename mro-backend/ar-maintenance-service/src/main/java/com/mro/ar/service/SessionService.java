package com.mro.ar.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mro.ar.domain.entity.ArSession;
import com.mro.ar.domain.entity.VideoArchive;
import com.mro.ar.mapper.ArSessionMapper;
import com.mro.ar.mapper.VideoArchiveMapper;
import com.mro.common.core.exception.BizException;
import com.mro.common.core.response.PageResult;
import com.mro.common.dubbo.common.request.*;
import com.mro.common.dubbo.ar.request.*;
import com.mro.common.dubbo.ar.response.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneOffset;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionService {

    private final ArSessionMapper arSessionMapper;
    private final VideoArchiveMapper videoArchiveMapper;

    @Transactional
    public ArSessionDTO createSession(CreateSessionCommand cmd) {
        ArSession session = new ArSession();
        session.setCallerId(cmd.callerId());
        session.setTaskId(cmd.taskId());
        session.setExpertId(cmd.expertId());
        session.setStatus("waiting");
        session.setSignalingToken(UUID.randomUUID().toString().replace("-", ""));
        arSessionMapper.insert(session);

        return toDTO(session);
    }

    @Transactional
    public void joinSession(Long sessionId, Long expertId) {
        ArSession session = getSessionOrThrow(sessionId);
        if ("ended".equals(session.getStatus())) {
            throw new BizException(4303, "会话已结束，不可操作");
        }
        session.setExpertId(expertId);
        session.setStatus("active");
        arSessionMapper.updateById(session);
    }

    @Transactional
    public void endSession(Long sessionId, Long operatorId) {
        ArSession session = getSessionOrThrow(sessionId);
        if ("ended".equals(session.getStatus())) {
            throw new BizException(4303, "会话已结束，不可操作");
        }
        session.setStatus("ended");
        arSessionMapper.updateById(session);
        log.info("AR session {} ended by operator {}", sessionId, operatorId);
    }

    public ArSession getSessionOrThrow(Long sessionId) {
        ArSession session = arSessionMapper.selectById(sessionId);
        if (session == null) {
            throw new BizException(4302, "远程协作会话不存在");
        }
        return session;
    }

    public PageResult<VideoArchiveDTO> listArchives(ArchiveQueryParam param, UserContextDTO ctx) {
        LambdaQueryWrapper<VideoArchive> wrapper = new LambdaQueryWrapper<VideoArchive>()
                .eq(param.taskId() != null, VideoArchive::getTaskId, param.taskId())
                .eq(param.sessionId() != null, VideoArchive::getSessionId, param.sessionId())
                .orderByDesc(VideoArchive::getCreatedAt);

        Page<VideoArchive> page = videoArchiveMapper.selectPage(
                new Page<>(param.pageNum(), param.pageSize()), wrapper);

        return PageResult.of(page.getRecords().stream()
                .map(this::toArchiveDTO).toList(), page.getTotal(), (int) page.getCurrent(), (int) page.getSize());
    }

    @Transactional
    public Long saveArchive(Long taskId, Long sessionId, String fileUrl, int durationSeconds) {
        VideoArchive archive = new VideoArchive();
        archive.setTaskId(taskId);
        archive.setSessionId(sessionId);
        archive.setFileUrl(fileUrl);
        archive.setDurationSeconds(durationSeconds);
        videoArchiveMapper.insert(archive);
        return archive.getId();
    }

    private ArSessionDTO toDTO(ArSession s) {
        return new ArSessionDTO(
                s.getId(),
                s.getCallerId(),
                s.getExpertId(),
                s.getStatus(),
                s.getSignalingToken(),
                s.getCreatedAt() != null ? s.getCreatedAt().toInstant(ZoneOffset.UTC) : null
        );
    }

    private VideoArchiveDTO toArchiveDTO(VideoArchive v) {
        return new VideoArchiveDTO(
                v.getId(),
                v.getTaskId(),
                v.getSessionId(),
                v.getFileUrl(),
                v.getDurationSeconds() != null ? v.getDurationSeconds() : 0,
                v.getCreatedAt() != null ? v.getCreatedAt().toInstant(ZoneOffset.UTC) : null
        );
    }
}

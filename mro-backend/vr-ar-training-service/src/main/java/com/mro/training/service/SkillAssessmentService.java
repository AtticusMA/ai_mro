package com.mro.training.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mro.common.core.exception.BizException;
import com.mro.common.dubbo.training.request.*;
import com.mro.common.dubbo.training.response.*;
import com.mro.training.domain.entity.SkillAssessment;
import com.mro.training.domain.entity.TrainingSession;
import com.mro.training.mapper.SkillAssessmentMapper;
import com.mro.training.mapper.TrainingSessionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SkillAssessmentService {

    private static final int ERR_SESSION_NOT_FOUND   = 4803;
    private static final int ERR_SESSION_IN_PROGRESS = 4804;

    private final SkillAssessmentMapper assessmentMapper;
    private final TrainingSessionMapper sessionMapper;

    public List<SkillAssessmentDTO> getAssessments(Long sessionId) {
        TrainingSession session = sessionMapper.selectById(sessionId);
        if (session == null) throw new BizException(ERR_SESSION_NOT_FOUND, "培训会话不存在");
        if ("in_progress".equals(session.getStatus()))
            throw new BizException(ERR_SESSION_IN_PROGRESS, "培训会话仍在进行中，无法查看最终评估");

        List<SkillAssessment> list = assessmentMapper.selectList(
                new LambdaQueryWrapper<SkillAssessment>()
                        .eq(SkillAssessment::getSessionId, sessionId)
                        .orderByAsc(SkillAssessment::getAssessedAt));
        return list.stream().map(a -> new SkillAssessmentDTO(
                a.getId(), a.getSessionId(), a.getMetricName(), a.getScore(),
                Map.of(), a.getAssessedAt()
        )).toList();
    }

    public IndividualReportDTO generateIndividualReport(Long traineeId, LocalDate start, LocalDate end) {
        String startStr = start.atStartOfDay().toString();
        String endStr = end.atTime(23, 59, 59).toString();

        List<TrainingSession> sessions = sessionMapper.selectList(
                new LambdaQueryWrapper<TrainingSession>()
                        .eq(TrainingSession::getTraineeId, traineeId)
                        .eq(TrainingSession::getStatus, "completed"));

        int totalSessions = sessions.size();
        List<Map<String, Object>> skillTrend =
                assessmentMapper.selectMonthlyTrend(traineeId, startStr, endStr);

        BigDecimal totalHours = BigDecimal.ZERO;
        for (TrainingSession s : sessions) {
            if (s.getStartedAt() != null && s.getEndedAt() != null) {
                long seconds = s.getEndedAt().getEpochSecond() - s.getStartedAt().getEpochSecond();
                totalHours = totalHours.add(BigDecimal.valueOf(seconds)
                        .divide(BigDecimal.valueOf(3600), 2, RoundingMode.HALF_UP));
            }
        }

        List<Map<String, Object>> avgByMetric = assessmentMapper.selectAvgScoreByTrainee(traineeId);
        List<String> weakPoints = new ArrayList<>();
        for (Map<String, Object> row : avgByMetric) {
            Object val = row.get("avg_score");
            double score = val instanceof Number n ? n.doubleValue() : 100.0;
            if (score < 70.0) weakPoints.add((String) row.get("metric_name"));
        }

        String recommendations = weakPoints.isEmpty()
                ? "各项技能表现良好，建议继续保持"
                : "建议加强以下方面的训练：" + String.join("、", weakPoints);

        return new IndividualReportDTO(traineeId, null, start, end,
                totalSessions, totalHours, skillTrend, weakPoints, recommendations);
    }

    public OverviewReportDTO generateOverviewReport(LocalDate start, LocalDate end) {
        long totalTrainees = sessionMapper.selectCount(
                new LambdaQueryWrapper<TrainingSession>().groupBy(TrainingSession::getTraineeId));
        long totalSessions = sessionMapper.selectCount(
                new LambdaQueryWrapper<TrainingSession>().eq(TrainingSession::getStatus, "completed"));

        return new OverviewReportDTO(
                (int) totalTrainees, (int) totalSessions, BigDecimal.ZERO,
                List.of(), List.of());
    }
}

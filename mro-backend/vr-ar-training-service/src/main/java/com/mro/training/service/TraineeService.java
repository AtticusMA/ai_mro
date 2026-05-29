package com.mro.training.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mro.common.core.exception.BizException;
import com.mro.common.core.response.PageResult;
import com.mro.common.dubbo.training.request.*;
import com.mro.common.dubbo.training.response.*;
import com.mro.training.domain.entity.TraineeProfile;
import com.mro.training.mapper.SkillAssessmentMapper;
import com.mro.training.mapper.TraineeProfileMapper;
import com.mro.training.mapper.TrainingSessionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TraineeService {

    private static final int ERR_TRAINEE_NOT_FOUND = 4802;

    private final TraineeProfileMapper traineeProfileMapper;
    private final TrainingSessionMapper sessionMapper;
    private final SkillAssessmentMapper assessmentMapper;

    public PageResult<TraineeDTO> listTrainees(int pageNum, int pageSize) {
        Page<TraineeProfile> page = traineeProfileMapper.selectPage(
                new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<TraineeProfile>().orderByAsc(TraineeProfile::getId));
        List<TraineeDTO> dtos = page.getRecords().stream()
                .map(p -> new TraineeDTO(p.getId(), p.getUserId(), null,
                        p.getSkillLevel(), p.getTotalTrainingHours()))
                .toList();
        return PageResult.of(dtos, page.getTotal(), pageNum, pageSize);
    }

    public TraineeProfileDTO getTraineeProfile(Long traineeId) {
        TraineeProfile profile = traineeProfileMapper.selectById(traineeId);
        if (profile == null) throw new BizException(ERR_TRAINEE_NOT_FOUND, "学员档案不存在");

        List<Map<String, Object>> avgScores = assessmentMapper.selectAvgScoreByTrainee(traineeId);
        Map<String, Double> skillRadar = new java.util.HashMap<>();
        for (Map<String, Object> row : avgScores) {
            String metric = (String) row.get("metric_name");
            Object val = row.get("avg_score");
            skillRadar.put(metric, val instanceof Number n ? n.doubleValue() : 0.0);
        }

        return new TraineeProfileDTO(
                profile.getId(), profile.getUserId(), null,
                profile.getSkillLevel(), profile.getTotalTrainingHours(),
                profile.getLastAssessmentDate(), skillRadar, List.of());
    }

    public TraineeProfile getById(Long id) {
        return traineeProfileMapper.selectById(id);
    }

    public void updateTrainingHours(Long traineeId, BigDecimal additionalHours) {
        TraineeProfile profile = traineeProfileMapper.selectById(traineeId);
        if (profile == null) return;
        BigDecimal current = profile.getTotalTrainingHours() != null
                ? profile.getTotalTrainingHours() : BigDecimal.ZERO;
        profile.setTotalTrainingHours(current.add(additionalHours));
        traineeProfileMapper.updateById(profile);
    }
}

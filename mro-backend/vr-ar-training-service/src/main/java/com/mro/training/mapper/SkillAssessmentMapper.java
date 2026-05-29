package com.mro.training.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mro.training.domain.entity.SkillAssessment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Mapper
public interface SkillAssessmentMapper extends BaseMapper<SkillAssessment> {

    @Select("SELECT metric_name, AVG(score) as avg_score FROM skill_assessment " +
            "WHERE session_id IN (SELECT id FROM training_session WHERE trainee_id = #{traineeId} AND deleted = 0) " +
            "AND deleted = 0 GROUP BY metric_name")
    List<Map<String, Object>> selectAvgScoreByTrainee(Long traineeId);

    @Select("SELECT DATE_FORMAT(sa.assessed_at, '%Y-%m') as month, AVG(sa.score) as avg_score " +
            "FROM skill_assessment sa " +
            "JOIN training_session ts ON ts.id = sa.session_id " +
            "WHERE ts.trainee_id = #{traineeId} " +
            "AND sa.assessed_at BETWEEN #{start} AND #{end} " +
            "AND sa.deleted = 0 AND ts.deleted = 0 " +
            "GROUP BY month ORDER BY month")
    List<Map<String, Object>> selectMonthlyTrend(Long traineeId, String start, String end);
}

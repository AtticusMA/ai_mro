package com.mro.training.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mro.training.domain.entity.TrainingSession;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TrainingSessionMapper extends BaseMapper<TrainingSession> {
}

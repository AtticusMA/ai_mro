package com.mro.aircraft.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mro.aircraft.domain.entity.HealthAlert;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface HealthAlertMapper extends BaseMapper<HealthAlert> {
}

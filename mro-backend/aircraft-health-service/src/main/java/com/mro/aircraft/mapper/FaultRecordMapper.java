package com.mro.aircraft.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mro.aircraft.domain.entity.FaultRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FaultRecordMapper extends BaseMapper<FaultRecord> {
}

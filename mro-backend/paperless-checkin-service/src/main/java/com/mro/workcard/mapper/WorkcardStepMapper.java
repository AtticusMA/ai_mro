package com.mro.workcard.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mro.workcard.domain.entity.WorkcardStep;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface WorkcardStepMapper extends BaseMapper<WorkcardStep> {

    @Select("SELECT COUNT(*) FROM workcard_step WHERE workcard_id = #{workcardId} AND deleted = 0")
    int countByWorkcard(Long workcardId);

    @Select("SELECT COUNT(*) FROM workcard_step WHERE workcard_id = #{workcardId} AND status = 'completed' AND deleted = 0")
    int countCompletedByWorkcard(Long workcardId);
}

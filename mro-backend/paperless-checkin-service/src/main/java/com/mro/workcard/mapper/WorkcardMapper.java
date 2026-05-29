package com.mro.workcard.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mro.workcard.domain.entity.Workcard;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface WorkcardMapper extends BaseMapper<Workcard> {

    @Select("SELECT DISTINCT ws.completed_by FROM workcard_step ws " +
            "WHERE ws.workcard_id = #{workcardId} AND ws.completed_by IS NOT NULL AND ws.deleted = 0")
    List<Long> selectAssigneeIdsByWorkcard(Long workcardId);

    // Returns assignee names — resolved via user IDs in service layer; this query returns IDs cast to String as placeholder
    default java.util.List<String> selectAssigneeNamesByWorkcard(Long workcardId) {
        return selectAssigneeIdsByWorkcard(workcardId).stream()
                .map(Object::toString).toList();
    }
}

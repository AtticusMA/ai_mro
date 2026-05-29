package com.mro.tool.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mro.tool.domain.entity.Tool;
import com.mro.tool.domain.entity.ToolCabinet;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ToolCabinetMapper extends BaseMapper<ToolCabinet> {

    @Select("SELECT t.slot_no, t.id, t.name, t.tool_code, t.rfid_tag, t.status " +
            "FROM tool t WHERE t.cabinet_id = #{cabinetId} AND t.deleted = 0 ORDER BY t.slot_no")
    List<Tool> selectSlotsByCabinet(Long cabinetId);
}

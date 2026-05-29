package com.mro.tool.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mro.tool.domain.entity.Tool;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ToolMapper extends BaseMapper<Tool> {
}

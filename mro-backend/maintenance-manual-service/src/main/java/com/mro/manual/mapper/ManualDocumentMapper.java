package com.mro.manual.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mro.manual.domain.entity.ManualDocument;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ManualDocumentMapper extends BaseMapper<ManualDocument> {
}

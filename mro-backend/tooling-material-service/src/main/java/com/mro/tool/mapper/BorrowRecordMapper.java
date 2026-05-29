package com.mro.tool.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mro.tool.domain.entity.BorrowRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BorrowRecordMapper extends BaseMapper<BorrowRecord> {
}

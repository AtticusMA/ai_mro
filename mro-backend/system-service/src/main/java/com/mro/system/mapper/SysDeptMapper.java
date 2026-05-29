package com.mro.system.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mro.system.entity.SysDept;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface SysDeptMapper extends BaseMapper<SysDept> {
    @Select("SELECT id FROM sys_dept WHERE FIND_IN_SET(#{deptId}, ancestors) AND is_deleted = 0")
    List<Long> selectDescendantIds(Long deptId);
}

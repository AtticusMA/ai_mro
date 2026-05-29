package com.mro.system.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mro.system.entity.SysDict;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface SysDictMapper extends BaseMapper<SysDict> {
    @Select("SELECT dict_code, dict_label FROM sys_dict WHERE dict_group = #{dictGroup} AND status = 1 ORDER BY id")
    List<com.mro.common.dubbo.system.response.DictItemDTO> selectItemsByGroup(String dictGroup);
}

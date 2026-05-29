package com.mro.workcard.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mro.workcard.domain.entity.PersonnelQualification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PersonnelQualificationMapper extends BaseMapper<PersonnelQualification> {

    @Select("SELECT * FROM personnel_qualification WHERE aircraft_type = #{aircraftType} " +
            "AND deleted = 0 AND valid_to >= CURDATE() " +
            "LIMIT #{offset}, #{pageSize}")
    List<PersonnelQualification> selectQualifiedPersonnel(String aircraftType, int offset, int pageSize);

    @Select("SELECT COUNT(*) FROM personnel_qualification WHERE aircraft_type = #{aircraftType} " +
            "AND deleted = 0 AND valid_to >= CURDATE()")
    long countQualifiedPersonnel(String aircraftType);
}

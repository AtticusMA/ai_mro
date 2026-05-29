package com.mro.dtwin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mro.common.dubbo.dtwin.response.WorkstationLoadDTO;
import com.mro.dtwin.domain.entity.Workstation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface WorkstationMapper extends BaseMapper<Workstation> {

    @Select("""
            SELECT w.id AS workstationId, w.name AS workstationName,
                   COUNT(o.id) AS orderCount,
                   COALESCE(AVG(o.progress) / 100.0, 0) AS utilizationRate
            FROM workstation w
            LEFT JOIN maintenance_order o ON o.workstation_id = w.id
                AND o.deleted = 0
                AND (#{startDate} IS NULL OR DATE(o.create_time) >= #{startDate})
                AND (#{endDate}   IS NULL OR DATE(o.create_time) <= #{endDate})
            WHERE w.deleted = 0
              AND (#{hangarId} IS NULL OR w.hangar_id = #{hangarId})
            GROUP BY w.id, w.name
            """)
    List<WorkstationLoadDTO> selectWorkstationLoad(Long hangarId, LocalDate startDate, LocalDate endDate);
}

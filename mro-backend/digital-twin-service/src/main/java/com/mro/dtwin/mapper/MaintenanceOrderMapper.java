package com.mro.dtwin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mro.dtwin.domain.entity.MaintenanceOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;

@Mapper
public interface MaintenanceOrderMapper extends BaseMapper<MaintenanceOrder> {

    @Select("""
            SELECT COALESCE(AVG(DATEDIFF(
                CASE WHEN o.status = 'completed' THEN o.update_time ELSE NOW() END,
                o.create_time)), 0)
            FROM maintenance_order o
            INNER JOIN production_plan p ON p.id = o.plan_id AND p.deleted = 0
            WHERE o.deleted = 0
              AND (#{hangarId}   IS NULL OR p.hangar_id = #{hangarId})
              AND (#{startDate}  IS NULL OR DATE(o.create_time) >= #{startDate})
              AND (#{endDate}    IS NULL OR DATE(o.create_time) <= #{endDate})
            """)
    double selectAvgCompletionDays(Long hangarId, LocalDate startDate, LocalDate endDate);

    @Select("""
            SELECT COUNT(o.id)
            FROM maintenance_order o
            INNER JOIN production_plan p ON p.id = o.plan_id AND p.deleted = 0
            WHERE o.deleted = 0 AND o.status = 'completed'
              AND (#{hangarId}   IS NULL OR p.hangar_id = #{hangarId})
              AND (#{startDate}  IS NULL OR DATE(o.create_time) >= #{startDate})
              AND (#{endDate}    IS NULL OR DATE(o.create_time) <= #{endDate})
            """)
    long countCompleted(Long hangarId, LocalDate startDate, LocalDate endDate);

    @Select("""
            SELECT COUNT(o.id)
            FROM maintenance_order o
            INNER JOIN production_plan p ON p.id = o.plan_id AND p.deleted = 0
            WHERE o.deleted = 0
              AND (#{hangarId}   IS NULL OR p.hangar_id = #{hangarId})
              AND (#{startDate}  IS NULL OR DATE(o.create_time) >= #{startDate})
              AND (#{endDate}    IS NULL OR DATE(o.create_time) <= #{endDate})
            """)
    long countTotal(Long hangarId, LocalDate startDate, LocalDate endDate);
}

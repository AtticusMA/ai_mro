package com.mro.training.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mro.training.domain.entity.TraineeProfile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TraineeProfileMapper extends BaseMapper<TraineeProfile> {

    @Select("SELECT tp.*, u.username FROM trainee_profile tp " +
            "LEFT JOIN sys_user u ON u.id = tp.user_id " +
            "WHERE tp.deleted = 0 LIMIT #{offset}, #{pageSize}")
    List<TraineeProfile> selectPageWithUser(int offset, int pageSize);
}

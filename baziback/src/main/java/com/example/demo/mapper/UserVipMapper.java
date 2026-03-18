package com.example.demo.mapper;

import com.example.demo.entity.UserVip;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;

@Mapper
public interface UserVipMapper {

    @Insert("INSERT INTO tb_user_vip (user_id, vip_type, start_time, end_time, source, source_id, is_active) " +
            "VALUES (#{userId}, #{vipType}, #{startTime}, #{endTime}, #{source}, #{sourceId}, #{isActive})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(UserVip userVip);
}

package com.example.demo.mapper;

import com.example.demo.entity.UserFeatureUnlock;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;

@Mapper
public interface UserFeatureUnlockMapper {

    @Insert("INSERT INTO tb_user_feature_unlock (user_id, feature_code, feature_name, unlock_type, start_time, end_time, source, source_id, is_active) " +
            "VALUES (#{userId}, #{featureCode}, #{featureName}, #{unlockType}, #{startTime}, #{endTime}, #{source}, #{sourceId}, #{isActive})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(UserFeatureUnlock unlock);
}

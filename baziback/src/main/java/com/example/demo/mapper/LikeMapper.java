package com.example.demo.mapper;

import org.apache.ibatis.annotations.*;

@Mapper
public interface LikeMapper {
    
    @Insert("INSERT INTO tb_like (user_id, target_type, target_id) VALUES (#{userId}, #{targetType}, #{targetId})")
    int insert(@Param("userId") Long userId, @Param("targetType") String targetType, @Param("targetId") Long targetId);
    
    @Delete("DELETE FROM tb_like WHERE user_id = #{userId} AND target_type = #{targetType} AND target_id = #{targetId}")
    int delete(@Param("userId") Long userId, @Param("targetType") String targetType, @Param("targetId") Long targetId);
    
    @Select("SELECT COUNT(*) FROM tb_like WHERE user_id = #{userId} AND target_type = #{targetType} AND target_id = #{targetId}")
    int exists(@Param("userId") Long userId, @Param("targetType") String targetType, @Param("targetId") Long targetId);
    
    @Select("SELECT target_id FROM tb_like WHERE user_id = #{userId} AND target_type = #{targetType}")
    java.util.List<Long> selectLikedIds(@Param("userId") Long userId, @Param("targetType") String targetType);
}

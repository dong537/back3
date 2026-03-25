package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.entity.Topic;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface TopicMapper extends BaseMapper<Topic> {
    
    @Select("SELECT * FROM tb_topic WHERE status = 1 ORDER BY sort_order ASC")
    List<Topic> selectAllTopics();
    
    @Select("SELECT * FROM tb_topic WHERE is_hot = 1 AND status = 1 ORDER BY posts_count DESC LIMIT #{limit}")
    List<Topic> selectHotTopics(@Param("limit") int limit);
    
    @Update("UPDATE tb_topic SET posts_count = posts_count + 1 WHERE id = #{topicId}")
    int incrementPostsCount(@Param("topicId") Long topicId);
    
    @Select("SELECT * FROM tb_topic WHERE name = #{name}")
    Topic selectByName(@Param("name") String name);
}

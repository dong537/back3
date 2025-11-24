package com.example.demo.mapper;

import com.example.demo.entity.UserFeedback;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 用户反馈Mapper
 */
@Mapper
public interface UserFeedbackMapper {
    
    /**
     * 插入反馈
     */
    @Insert("INSERT INTO tb_user_feedback (user_id, feedback_type, related_id, rating, content, tags, status) " +
            "VALUES (#{userId}, #{feedbackType}, #{relatedId}, #{rating}, #{content}, #{tags}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(UserFeedback feedback);
    
    /**
     * 更新反馈状态和回复
     */
    @Update("UPDATE tb_user_feedback SET status=#{status}, admin_reply=#{adminReply}, reply_time=NOW() WHERE id=#{id}")
    int updateReply(@Param("id") Long id, @Param("status") Integer status, @Param("adminReply") String adminReply);
    
    /**
     * 根据ID查询
     */
    @Select("SELECT * FROM tb_user_feedback WHERE id=#{id}")
    UserFeedback findById(Long id);
    
    /**
     * 根据用户ID查询反馈列表
     */
    @Select("SELECT * FROM tb_user_feedback WHERE user_id=#{userId} ORDER BY create_time DESC")
    List<UserFeedback> findByUserId(Long userId);
    
    /**
     * 根据反馈类型查询
     */
    @Select("SELECT * FROM tb_user_feedback WHERE feedback_type=#{feedbackType} ORDER BY create_time DESC")
    List<UserFeedback> findByType(String feedbackType);
    
    /**
     * 查询待处理的反馈
     */
    @Select("SELECT * FROM tb_user_feedback WHERE status=0 ORDER BY create_time ASC")
    List<UserFeedback> findPending();
    
    /**
     * 统计用户的反馈数量
     */
    @Select("SELECT COUNT(*) FROM tb_user_feedback WHERE user_id=#{userId}")
    int countByUserId(Long userId);
    
    /**
     * 删除反馈
     */
    @Delete("DELETE FROM tb_user_feedback WHERE id=#{id}")
    int deleteById(Long id);
}

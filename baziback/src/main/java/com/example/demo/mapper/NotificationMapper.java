package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.demo.entity.Notification;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {
    
    @Select("SELECT n.*, u.username, u.nickname, u.avatar " +
            "FROM tb_notification n LEFT JOIN tb_user u ON n.from_user_id = u.id " +
            "WHERE n.user_id = #{userId} " +
            "ORDER BY n.created_at DESC")
    List<Notification> selectByUserId(IPage<Notification> page, @Param("userId") Long userId);
    
    @Select("SELECT n.*, u.username, u.nickname, u.avatar " +
            "FROM tb_notification n LEFT JOIN tb_user u ON n.from_user_id = u.id " +
            "WHERE n.user_id = #{userId} AND n.type = #{type} " +
            "ORDER BY n.created_at DESC")
    List<Notification> selectByUserIdAndType(IPage<Notification> page, @Param("userId") Long userId, @Param("type") String type);
    
    @Select("SELECT COUNT(*) FROM tb_notification WHERE user_id = #{userId}")
    long countByUserId(@Param("userId") Long userId);
    
    @Select("SELECT COUNT(*) FROM tb_notification WHERE user_id = #{userId} AND type = #{type}")
    long countByUserIdAndType(@Param("userId") Long userId, @Param("type") String type);
    
    @Select("SELECT COUNT(*) FROM tb_notification WHERE user_id = #{userId} AND is_read = 0")
    int countUnread(@Param("userId") Long userId);
    
    @Update("UPDATE tb_notification SET is_read = 1 WHERE user_id = #{userId}")
    int markAllAsRead(@Param("userId") Long userId);
    
    @Update("UPDATE tb_notification SET is_read = 1 WHERE id = #{id}")
    int markAsRead(@Param("id") Long id);
}

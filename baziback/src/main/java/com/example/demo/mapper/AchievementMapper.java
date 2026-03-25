package com.example.demo.mapper;

import com.example.demo.entity.Achievement;
import com.example.demo.entity.UserAchievement;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 成就数据访问层
 */
@Mapper
public interface AchievementMapper {
    
    /**
     * 查询所有启用的成就
     */
    @Select("SELECT * FROM tb_achievement WHERE is_active = 1 ORDER BY sort_order ASC")
    List<Achievement> findAllActive();
    
    /**
     * 根据成就代码查询成就
     */
    @Select("SELECT * FROM tb_achievement WHERE achievement_code = #{achievementCode}")
    Achievement findByCode(String achievementCode);
    
    /**
     * 查询用户已解锁的成就
     */
    @Select("SELECT ua.*, a.achievement_name, a.achievement_description, a.achievement_type, a.icon_url " +
            "FROM tb_user_achievement ua " +
            "LEFT JOIN tb_achievement a ON ua.achievement_id = a.id " +
            "WHERE ua.user_id = #{userId} ORDER BY ua.unlocked_time DESC")
    List<UserAchievementWithInfo> findUserAchievements(Long userId);
    
    /**
     * 检查用户是否已解锁某个成就
     */
    @Select("SELECT COUNT(*) FROM tb_user_achievement " +
            "WHERE user_id = #{userId} AND achievement_code = #{achievementCode}")
    int checkUserAchievement(@Param("userId") Long userId, @Param("achievementCode") String achievementCode);
    
    /**
     * 插入用户成就记录
     */
    @Insert("INSERT INTO tb_user_achievement (user_id, achievement_id, achievement_code, unlocked_time, points_earned) " +
            "VALUES (#{userId}, #{achievementId}, #{achievementCode}, #{unlockedTime}, #{pointsEarned})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertUserAchievement(UserAchievement userAchievement);
    
    /**
     * 用户成就信息（包含成就详情）
     */
    class UserAchievementWithInfo extends UserAchievement {
        public String achievementName;
        public String achievementDescription;
        public String achievementType;
        public String iconUrl;
    }
}

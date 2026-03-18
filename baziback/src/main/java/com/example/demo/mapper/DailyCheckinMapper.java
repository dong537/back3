package com.example.demo.mapper;

import com.example.demo.entity.DailyCheckin;
import org.apache.ibatis.annotations.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 每日签到数据访问层
 */
@Mapper
public interface DailyCheckinMapper {
    
    /**
     * 根据用户ID和日期查询签到记录
     */
    @Select("SELECT * FROM tb_daily_checkin WHERE user_id = #{userId} AND checkin_date = #{checkinDate} LIMIT 1")
    DailyCheckin findByUserIdAndDate(@Param("userId") Long userId, @Param("checkinDate") LocalDate checkinDate);
    
    /**
     * 插入签到记录
     */
    @Insert("INSERT INTO tb_daily_checkin (user_id, checkin_date, checkin_time, streak_days, points_earned) " +
            "VALUES (#{userId}, #{checkinDate}, #{checkinTime}, #{streakDays}, #{pointsEarned})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(DailyCheckin checkin);
    
    /**
     * 查询用户最近的签到记录（用于计算连续天数）
     */
    @Select("SELECT * FROM tb_daily_checkin WHERE user_id = #{userId} ORDER BY checkin_date DESC LIMIT #{limit}")
    List<DailyCheckin> findRecentByUserId(@Param("userId") Long userId, @Param("limit") Integer limit);
    
    /**
     * 查询用户今天是否已签到
     */
    @Select("SELECT COUNT(*) FROM tb_daily_checkin WHERE user_id = #{userId} AND checkin_date = #{checkinDate}")
    int countByUserIdAndDate(@Param("userId") Long userId, @Param("checkinDate") LocalDate checkinDate);
    
    /**
     * 查询用户最近一次签到日期
     */
    @Select("SELECT checkin_date FROM tb_daily_checkin WHERE user_id = #{userId} ORDER BY checkin_date DESC LIMIT 1")
    LocalDate findLastCheckinDate(@Param("userId") Long userId);
    
    /**
     * 查询用户连续签到天数（从最近一次签到往前计算）
     */
    @Select("SELECT streak_days FROM tb_daily_checkin WHERE user_id = #{userId} ORDER BY checkin_date DESC LIMIT 1")
    Integer findCurrentStreak(@Param("userId") Long userId);
    
    /**
     * 查询用户本周签到记录
     */
    @Select("SELECT * FROM tb_daily_checkin WHERE user_id = #{userId} AND checkin_date >= #{startDate} AND checkin_date <= #{endDate} ORDER BY checkin_date")
    List<DailyCheckin> findWeeklyCheckins(@Param("userId") Long userId, 
                                          @Param("startDate") LocalDate startDate, 
                                          @Param("endDate") LocalDate endDate);
}

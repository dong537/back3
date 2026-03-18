package com.example.demo.mapper;

import com.example.demo.entity.DailyLucky;
import org.apache.ibatis.annotations.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 每日幸运数据访问层
 */
@Mapper
public interface DailyLuckyMapper {
    
    /**
     * 根据日期查询每日幸运
     */
    @Select("SELECT * FROM tb_daily_lucky WHERE lucky_date = #{date} LIMIT 1")
    DailyLucky findByDate(@Param("date") LocalDate date);
    
    /**
     * 根据日期范围查询每日幸运
     */
    @Select("SELECT * FROM tb_daily_lucky WHERE lucky_date >= #{startDate} AND lucky_date <= #{endDate} ORDER BY lucky_date ASC")
    List<DailyLucky> findByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    /**
     * 插入每日幸运记录
     */
    @Insert("INSERT INTO tb_daily_lucky (lucky_date, lucky_number, lucky_color, lucky_constellation, lucky_food, suitable_actions, unsuitable_actions, description) " +
            "VALUES (#{luckyDate}, #{luckyNumber}, #{luckyColor}, #{luckyConstellation}, #{luckyFood}, #{suitableActions}, #{unsuitableActions}, #{description})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(DailyLucky dailyLucky);
    
    /**
     * 更新每日幸运记录
     */
    @Update("UPDATE tb_daily_lucky SET " +
            "lucky_number = #{luckyNumber}, " +
            "lucky_color = #{luckyColor}, " +
            "lucky_constellation = #{luckyConstellation}, " +
            "lucky_food = #{luckyFood}, " +
            "suitable_actions = #{suitableActions}, " +
            "unsuitable_actions = #{unsuitableActions}, " +
            "description = #{description} " +
            "WHERE id = #{id}")
    int update(DailyLucky dailyLucky);
    
    /**
     * 根据ID查询
     */
    @Select("SELECT * FROM tb_daily_lucky WHERE id = #{id}")
    DailyLucky findById(@Param("id") Long id);
    
    /**
     * 查询最近的每日幸运记录
     */
    @Select("SELECT * FROM tb_daily_lucky ORDER BY lucky_date DESC LIMIT #{limit}")
    List<DailyLucky> findRecent(@Param("limit") Integer limit);
}

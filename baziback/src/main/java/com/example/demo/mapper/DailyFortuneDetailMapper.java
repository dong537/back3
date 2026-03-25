package com.example.demo.mapper;

import com.example.demo.entity.DailyFortuneDetail;
import org.apache.ibatis.annotations.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 每日运势详情数据访问层
 */
@Mapper
public interface DailyFortuneDetailMapper {
    
    /**
     * 根据日期查询每日运势详情
     */
    @Select("SELECT * FROM tb_daily_fortune_detail WHERE fortune_date = #{date} LIMIT 1")
    DailyFortuneDetail findByDate(@Param("date") LocalDate date);
    
    /**
     * 根据日期范围查询每日运势详情
     */
    @Select("SELECT * FROM tb_daily_fortune_detail WHERE fortune_date >= #{startDate} AND fortune_date <= #{endDate} ORDER BY fortune_date ASC")
    List<DailyFortuneDetail> findByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    /**
     * 插入每日运势详情记录
     */
    @Insert("INSERT INTO tb_daily_fortune_detail (" +
            "fortune_date, love_score, love_analysis, career_score, career_analysis, " +
            "wealth_score, wealth_analysis, health_score, health_analysis, " +
            "study_score, study_analysis, relationship_score, relationship_analysis, " +
            "lucky_color, lucky_number, lucky_direction, lucky_time, " +
            "suitable_actions, unsuitable_actions, overall_advice, keywords, " +
            "create_time, update_time" +
            ") VALUES (" +
            "#{fortuneDate}, #{loveScore}, #{loveAnalysis}, #{careerScore}, #{careerAnalysis}, " +
            "#{wealthScore}, #{wealthAnalysis}, #{healthScore}, #{healthAnalysis}, " +
            "#{studyScore}, #{studyAnalysis}, #{relationshipScore}, #{relationshipAnalysis}, " +
            "#{luckyColor}, #{luckyNumber}, #{luckyDirection}, #{luckyTime}, " +
            "#{suitableActions}, #{unsuitableActions}, #{overallAdvice}, #{keywords}, " +
            "NOW(), NOW()" +
            ")")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(DailyFortuneDetail dailyFortuneDetail);
    
    /**
     * 更新每日运势详情记录
     */
    @Update("UPDATE tb_daily_fortune_detail SET " +
            "love_score = #{loveScore}, love_analysis = #{loveAnalysis}, " +
            "career_score = #{careerScore}, career_analysis = #{careerAnalysis}, " +
            "wealth_score = #{wealthScore}, wealth_analysis = #{wealthAnalysis}, " +
            "health_score = #{healthScore}, health_analysis = #{healthAnalysis}, " +
            "study_score = #{studyScore}, study_analysis = #{studyAnalysis}, " +
            "relationship_score = #{relationshipScore}, relationship_analysis = #{relationshipAnalysis}, " +
            "lucky_color = #{luckyColor}, lucky_number = #{luckyNumber}, " +
            "lucky_direction = #{luckyDirection}, lucky_time = #{luckyTime}, " +
            "suitable_actions = #{suitableActions}, unsuitable_actions = #{unsuitableActions}, " +
            "overall_advice = #{overallAdvice}, keywords = #{keywords}, " +
            "update_time = NOW() " +
            "WHERE id = #{id}")
    int update(DailyFortuneDetail dailyFortuneDetail);
    
    /**
     * 根据ID查询
     */
    @Select("SELECT * FROM tb_daily_fortune_detail WHERE id = #{id}")
    DailyFortuneDetail findById(@Param("id") Long id);
    
    /**
     * 查询最近的每日运势详情记录
     */
    @Select("SELECT * FROM tb_daily_fortune_detail ORDER BY fortune_date DESC LIMIT #{limit}")
    List<DailyFortuneDetail> findRecent(@Param("limit") Integer limit);
    
    /**
     * 获取总记录数
     */
    @Select("SELECT COUNT(*) FROM tb_daily_fortune_detail")
    long countTotal();

    /**
     * 根据偏移量获取一条记录
     */
    @Select("SELECT * FROM tb_daily_fortune_detail LIMIT 1 OFFSET #{offset}")
    DailyFortuneDetail findByOffset(@Param("offset") long offset);
    
    /**
     * 批量插入
     */
    @Insert("<script>" +
            "INSERT INTO tb_daily_fortune_detail (" +
            "fortune_date, love_score, love_analysis, career_score, career_analysis, " +
            "wealth_score, wealth_analysis, health_score, health_analysis, " +
            "study_score, study_analysis, relationship_score, relationship_analysis, " +
            "lucky_color, lucky_number, lucky_direction, lucky_time, " +
            "suitable_actions, unsuitable_actions, overall_advice, keywords, " +
            "create_time, update_time" +
            ") VALUES " +
            "<foreach collection='list' item='item' separator=','>" +
            "(" +
            "#{item.fortuneDate}, #{item.loveScore}, #{item.loveAnalysis}, #{item.careerScore}, #{item.careerAnalysis}, " +
            "#{item.wealthScore}, #{item.wealthAnalysis}, #{item.healthScore}, #{item.healthAnalysis}, " +
            "#{item.studyScore}, #{item.studyAnalysis}, #{item.relationshipScore}, #{item.relationshipAnalysis}, " +
            "#{item.luckyColor}, #{item.luckyNumber}, #{item.luckyDirection}, #{item.luckyTime}, " +
            "#{item.suitableActions}, #{item.unsuitableActions}, #{item.overallAdvice}, #{item.keywords}, " +
            "NOW(), NOW()" +
            ")" +
            "</foreach>" +
            "</script>")
    int batchInsert(@Param("list") List<DailyFortuneDetail> list);
}

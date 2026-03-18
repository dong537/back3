package com.example.demo.mapper;

import com.example.demo.entity.TbTarotDailyDraw;
import org.apache.ibatis.annotations.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 每日塔罗牌抽牌记录数据访问层
 */
@Mapper
public interface TarotDailyDrawMapper {
    
    /**
     * 根据用户ID和日期查询抽牌记录
     */
    @Select("SELECT * FROM tb_tarot_daily_draw WHERE user_id = #{userId} AND draw_date = #{drawDate} LIMIT 1")
    TbTarotDailyDraw findByUserIdAndDate(@Param("userId") Long userId, @Param("drawDate") LocalDate drawDate);
    
    /**
     * 插入抽牌记录
     */
    @Insert("INSERT INTO tb_tarot_daily_draw (user_id, draw_date, card_id, is_reversed) " +
            "VALUES (#{userId}, #{drawDate}, #{cardId}, #{isReversed})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(TbTarotDailyDraw dailyDraw);
    
    /**
     * 查询用户的历史抽牌记录
     */
    @Select("SELECT * FROM tb_tarot_daily_draw WHERE user_id = #{userId} ORDER BY draw_date DESC LIMIT #{limit}")
    List<TbTarotDailyDraw> findHistoryByUserId(@Param("userId") Long userId, @Param("limit") Integer limit);
    
    /**
     * 检查用户今天是否已抽牌
     */
    @Select("SELECT COUNT(*) FROM tb_tarot_daily_draw WHERE user_id = #{userId} AND draw_date = #{drawDate}")
    int countByUserIdAndDate(@Param("userId") Long userId, @Param("drawDate") LocalDate drawDate);
}

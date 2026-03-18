package com.example.demo.mapper;

import com.example.demo.entity.TbTarotCard;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 塔罗牌数据访问层
 */
@Mapper
public interface TarotCardMapper {
    
    /**
     * 根据card_id查询塔罗牌详细信息
     */
    @Select("SELECT * FROM tb_tarot_card WHERE card_id = #{cardId} LIMIT 1")
    TbTarotCard findByCardId(Integer cardId);
    
    /**
     * 根据ID查询塔罗牌详细信息
     */
    @Select("SELECT * FROM tb_tarot_card WHERE id = #{id}")
    TbTarotCard findById(Integer id);
    
    /**
     * 根据牌名查询塔罗牌（支持中文和英文名称，支持模糊匹配）
     */
    @Select("SELECT * FROM tb_tarot_card WHERE card_name_cn = #{name} OR card_name_en = #{name} OR card_name_cn LIKE CONCAT('%', #{name}, '%') OR card_name_en LIKE CONCAT('%', #{name}, '%') LIMIT 1")
    TbTarotCard findByName(@Param("name") String name);
    
    /**
     * 查询所有塔罗牌
     */
    @Select("SELECT * FROM tb_tarot_card ORDER BY sort_order, card_id")
    List<TbTarotCard> findAll();
    
    /**
     * 根据类型查询塔罗牌
     */
    @Select("SELECT * FROM tb_tarot_card WHERE card_type = #{cardType} ORDER BY sort_order, card_id")
    List<TbTarotCard> findByCardType(@Param("cardType") String cardType);
    
    /**
     * 根据花色查询塔罗牌
     */
    @Select("SELECT * FROM tb_tarot_card WHERE suit = #{suit} ORDER BY sort_order, number")
    List<TbTarotCard> findBySuit(@Param("suit") String suit);
}

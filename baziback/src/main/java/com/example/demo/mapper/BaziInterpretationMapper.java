package com.example.demo.mapper;

import com.example.demo.entity.BaziInterpretation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 八字解释数据访问层
 */
@Mapper
public interface BaziInterpretationMapper {

    /**
     * 根据十神类型和干支位置查询解释
     */
    @Select("SELECT * FROM tb_bazi_interpretation WHERE god_type = #{godType} AND ganzhi_position = #{ganzhiPosition} LIMIT 1")
    BaziInterpretation findByGodTypeAndPosition(@Param("godType") String godType, @Param("ganzhiPosition") String ganzhiPosition);

    /**
     * 根据十神类型查询所有解释
     */
    @Select("SELECT * FROM tb_bazi_interpretation WHERE god_type = #{godType} ORDER BY id")
    List<BaziInterpretation> findByGodType(@Param("godType") String godType);

    /**
     * 根据干支位置查询所有解释
     */
    @Select("SELECT * FROM tb_bazi_interpretation WHERE ganzhi_position = #{ganzhiPosition} ORDER BY id")
    List<BaziInterpretation> findByPosition(@Param("ganzhiPosition") String ganzhiPosition);

    /**
     * 查询所有解释
     */
    @Select("SELECT * FROM tb_bazi_interpretation ORDER BY id")
    List<BaziInterpretation> findAll();
}

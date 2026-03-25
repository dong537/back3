package com.example.demo.mapper;

import com.example.demo.entity.TbHexagram;
import com.example.demo.entity.TbHexagramYao;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

/**
 * 卦象数据访问层
 */
@Mapper
public interface HexagramMapper {
    
    /**
     * 根据ID查询卦象
     */
    @Select("SELECT * FROM tb_hexagram WHERE id = #{id}")
    TbHexagram findById(Integer id);
    
    /**
     * 根据上下卦查询卦象
     */
    @Select("SELECT * FROM tb_hexagram WHERE upper_gua = #{upperGua} AND lower_gua = #{lowerGua} LIMIT 1")
    TbHexagram findByUpperAndLower(@Param("upperGua") String upperGua, @Param("lowerGua") String lowerGua);
    
    /**
     * 根据卦名查询卦象
     */
    @Select("SELECT * FROM tb_hexagram WHERE name = #{name} OR name_short = #{name} LIMIT 1")
    TbHexagram findByName(String name);
    
    /**
     * 查询所有卦象
     */
    @Select("SELECT * FROM tb_hexagram ORDER BY id")
    List<TbHexagram> findAll();
    
    /**
     * 根据卦ID查询所有爻
     */
    @Select("SELECT * FROM tb_hexagram_yao WHERE hexagram_id = #{hexagramId} ORDER BY yao_position")
    List<TbHexagramYao> findYaosByHexagramId(Integer hexagramId);
    
    /**
     * 根据卦ID和爻位查询爻
     */
    @Select("SELECT * FROM tb_hexagram_yao WHERE hexagram_id = #{hexagramId} AND yao_position = #{yaoPosition}")
    TbHexagramYao findByHexagramIdAndPosition(@Param("hexagramId") Integer hexagramId, @Param("yaoPosition") Integer yaoPosition);
    
    /**
     * 根据卦ID和文本类型查询卦爻辞
     */
    @Select("SELECT content FROM base_hexagram_text WHERE hexagram_id = #{hexagramId} AND text_type = #{textType} AND yao_position IS NULL LIMIT 1")
    String findTextByHexagramIdAndType(@Param("hexagramId") Integer hexagramId, @Param("textType") String textType);

    /**
     * 根据卦ID和文本类型查询卦爻辞（包含白话解释）
     */
    @Select("SELECT content, explanation FROM base_hexagram_text WHERE hexagram_id = #{hexagramId} AND text_type = #{textType} AND yao_position IS NULL LIMIT 1")
    Map<String, Object> findTextDetailByHexagramIdAndType(@Param("hexagramId") Integer hexagramId, @Param("textType") String textType);
    
    /**
     * 根据卦ID、文本类型和爻位查询爻辞
     */
    @Select("SELECT content FROM base_hexagram_text WHERE hexagram_id = #{hexagramId} AND text_type = #{textType} AND yao_position = #{yaoPosition} LIMIT 1")
    String findTextByHexagramIdAndYaoPosition(@Param("hexagramId") Integer hexagramId, @Param("textType") String textType, @Param("yaoPosition") Integer yaoPosition);

    /**
     * 根据卦ID、文本类型和爻位查询爻辞（包含白话解释）
     */
    @Select("SELECT content, explanation FROM base_hexagram_text WHERE hexagram_id = #{hexagramId} AND text_type = #{textType} AND yao_position = #{yaoPosition} LIMIT 1")
    Map<String, Object> findTextDetailByHexagramIdAndYaoPosition(@Param("hexagramId") Integer hexagramId, @Param("textType") String textType, @Param("yaoPosition") Integer yaoPosition);
}

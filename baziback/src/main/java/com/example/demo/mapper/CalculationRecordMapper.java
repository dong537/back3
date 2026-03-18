package com.example.demo.mapper;

import com.example.demo.entity.TbCalculationRecord;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 测算记录 Mapper
 */
@Mapper
public interface CalculationRecordMapper {

    /**
     * 插入新记录
     */
    @Insert("INSERT INTO tb_calculation_record (user_id, record_type, record_title, question, summary, data, create_time, update_time) " +
            "VALUES (#{userId}, #{recordType}, #{recordTitle}, #{question}, #{summary}, #{data}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(TbCalculationRecord record);

    /**
     * 根据ID查询
     */
    @Select("SELECT * FROM tb_calculation_record WHERE id = #{id}")
    TbCalculationRecord selectById(Long id);

    /**
     * 根据用户ID查询所有记录
     */
    @Select("SELECT * FROM tb_calculation_record WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<TbCalculationRecord> selectByUserId(Long userId);

    /**
     * 根据用户ID和类型查询记录
     */
    @Select("SELECT * FROM tb_calculation_record WHERE user_id = #{userId} AND record_type = #{recordType} ORDER BY create_time DESC")
    List<TbCalculationRecord> selectByUserIdAndType(Long userId, String recordType);

    /**
     * 分页查询用户记录
     */
    @Select("SELECT * FROM tb_calculation_record WHERE user_id = #{userId} ORDER BY create_time DESC LIMIT #{offset}, #{limit}")
    List<TbCalculationRecord> selectByUserIdPaged(Long userId, int offset, int limit);

    /**
     * 更新记录
     */
    @Update("UPDATE tb_calculation_record SET record_title = #{recordTitle}, question = #{question}, summary = #{summary}, data = #{data}, update_time = NOW() WHERE id = #{id}")
    int update(TbCalculationRecord record);

    /**
     * 删除记录
     */
    @Delete("DELETE FROM tb_calculation_record WHERE id = #{id}")
    int delete(Long id);

    /**
     * 删除用户的记录
     */
    @Delete("DELETE FROM tb_calculation_record WHERE user_id = #{userId}")
    int deleteByUserId(Long userId);

    /**
     * 统计用户记录数
     */
    @Select("SELECT COUNT(*) FROM tb_calculation_record WHERE user_id = #{userId}")
    int countByUserId(Long userId);

    /**
     * 统计用户某类型的记录数
     */
    @Select("SELECT COUNT(*) FROM tb_calculation_record WHERE user_id = #{userId} AND record_type = #{recordType}")
    int countByUserIdAndType(Long userId, String recordType);
}

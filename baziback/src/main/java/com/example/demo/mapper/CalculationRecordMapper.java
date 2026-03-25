package com.example.demo.mapper;

import com.example.demo.entity.TbCalculationRecord;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface CalculationRecordMapper {

    String BASE_COLUMNS =
            "id, " +
            "user_id AS userId, " +
            "record_type AS recordType, " +
            "record_title AS recordTitle, " +
            "question, " +
            "summary, " +
            "input_data AS inputData, " +
            "result_data AS data, " +
            "created_at AS createTime, " +
            "updated_at AS updateTime";

    @Insert("INSERT INTO tb_calculation_record " +
            "(user_id, record_type, record_title, question, input_data, result_data, summary, created_at, updated_at) " +
            "VALUES (#{userId}, #{recordType}, #{recordTitle}, #{question}, #{inputData}, #{data}, #{summary}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(TbCalculationRecord record);

    @Select("SELECT " + BASE_COLUMNS + " FROM tb_calculation_record WHERE id = #{id}")
    TbCalculationRecord selectById(Long id);

    @Select("SELECT " + BASE_COLUMNS + " FROM tb_calculation_record WHERE id = #{id} AND user_id = #{userId}")
    TbCalculationRecord selectByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    @Select("SELECT " + BASE_COLUMNS + " FROM tb_calculation_record WHERE user_id = #{userId} ORDER BY created_at DESC")
    List<TbCalculationRecord> selectByUserId(Long userId);

    @Select("SELECT " + BASE_COLUMNS + " FROM tb_calculation_record WHERE user_id = #{userId} AND record_type = #{recordType} ORDER BY created_at DESC")
    List<TbCalculationRecord> selectByUserIdAndType(@Param("userId") Long userId, @Param("recordType") String recordType);

    @Select("SELECT " + BASE_COLUMNS + " FROM tb_calculation_record " +
            "WHERE user_id = #{userId} " +
            "ORDER BY created_at DESC LIMIT #{offset}, #{limit}")
    List<TbCalculationRecord> selectByUserIdPaged(@Param("userId") Long userId,
                                                  @Param("offset") int offset,
                                                  @Param("limit") int limit);

    @Select("SELECT " + BASE_COLUMNS + " FROM tb_calculation_record " +
            "WHERE user_id = #{userId} AND record_type = #{recordType} " +
            "ORDER BY created_at DESC LIMIT #{offset}, #{limit}")
    List<TbCalculationRecord> selectByUserIdAndTypePaged(@Param("userId") Long userId,
                                                         @Param("recordType") String recordType,
                                                         @Param("offset") int offset,
                                                         @Param("limit") int limit);

    @Update("UPDATE tb_calculation_record " +
            "SET record_title = #{recordTitle}, question = #{question}, summary = #{summary}, input_data = #{inputData}, result_data = #{data}, updated_at = NOW() " +
            "WHERE id = #{id} AND user_id = #{userId}")
    int updateByUserId(TbCalculationRecord record);

    @Delete("DELETE FROM tb_calculation_record WHERE id = #{id} AND user_id = #{userId}")
    int deleteByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    @Delete("DELETE FROM tb_calculation_record WHERE user_id = #{userId}")
    int deleteByUserId(Long userId);

    @Select("SELECT COUNT(*) FROM tb_calculation_record WHERE user_id = #{userId}")
    int countByUserId(Long userId);

    @Select("SELECT COUNT(*) FROM tb_calculation_record WHERE user_id = #{userId} AND record_type = #{recordType}")
    int countByUserIdAndType(@Param("userId") Long userId, @Param("recordType") String recordType);
}

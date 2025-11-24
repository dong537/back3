package com.example.demo.mapper;

import com.example.demo.entity.AnalysisHistory;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 分析历史记录Mapper
 */
@Mapper
public interface AnalysisHistoryMapper {
    
    /**
     * 插入分析历史
     */
    @Insert("INSERT INTO tb_analysis_history (user_id, bazi_info_id, analysis_type, request_data, " +
            "response_data, report_id, analysis_duration, model_version, is_favorite) " +
            "VALUES (#{userId}, #{baziInfoId}, #{analysisType}, #{requestData}, #{responseData}, " +
            "#{reportId}, #{analysisDuration}, #{modelVersion}, #{isFavorite})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(AnalysisHistory history);
    
    /**
     * 根据ID查询
     */
    @Select("SELECT * FROM tb_analysis_history WHERE id=#{id}")
    AnalysisHistory findById(Long id);
    
    /**
     * 根据用户ID查询历史记录
     */
    @Select("SELECT * FROM tb_analysis_history WHERE user_id=#{userId} ORDER BY create_time DESC LIMIT #{limit} OFFSET #{offset}")
    List<AnalysisHistory> findByUserId(@Param("userId") Long userId, @Param("offset") int offset, @Param("limit") int limit);
    
    /**
     * 根据用户ID和分析类型查询
     */
    @Select("SELECT * FROM tb_analysis_history WHERE user_id=#{userId} AND analysis_type=#{analysisType} " +
            "ORDER BY create_time DESC LIMIT #{limit} OFFSET #{offset}")
    List<AnalysisHistory> findByUserIdAndType(@Param("userId") Long userId, @Param("analysisType") String analysisType, 
                                               @Param("offset") int offset, @Param("limit") int limit);
    
    /**
     * 查询用户的收藏记录
     */
    @Select("SELECT * FROM tb_analysis_history WHERE user_id=#{userId} AND is_favorite=1 ORDER BY create_time DESC")
    List<AnalysisHistory> findFavoritesByUserId(Long userId);
    
    /**
     * 更新收藏状态
     */
    @Update("UPDATE tb_analysis_history SET is_favorite=#{isFavorite} WHERE id=#{id}")
    int updateFavorite(@Param("id") Long id, @Param("isFavorite") Integer isFavorite);
    
    /**
     * 统计用户的分析次数
     */
    @Select("SELECT COUNT(*) FROM tb_analysis_history WHERE user_id=#{userId}")
    int countByUserId(Long userId);
    
    /**
     * 统计用户某类型的分析次数
     */
    @Select("SELECT COUNT(*) FROM tb_analysis_history WHERE user_id=#{userId} AND analysis_type=#{analysisType}")
    int countByUserIdAndType(@Param("userId") Long userId, @Param("analysisType") String analysisType);
    
    /**
     * 删除历史记录
     */
    @Delete("DELETE FROM tb_analysis_history WHERE id=#{id}")
    int deleteById(Long id);
}

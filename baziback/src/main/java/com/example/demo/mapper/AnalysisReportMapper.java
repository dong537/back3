package com.example.demo.mapper;

import com.example.demo.entity.AnalysisReport;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 深度分析报告Mapper
 */
@Mapper
public interface AnalysisReportMapper {
    
    /**
     * 插入报告
     */
    @Insert("INSERT INTO tb_analysis_report (user_id, bazi_info_id, report_type, report_title, " +
            "report_content, report_data, version, status, view_count, export_count) " +
            "VALUES (#{userId}, #{baziInfoId}, #{reportType}, #{reportTitle}, #{reportContent}, " +
            "#{reportData}, #{version}, #{status}, #{viewCount}, #{exportCount})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(AnalysisReport report);
    
    /**
     * 更新报告
     */
    @Update("UPDATE tb_analysis_report SET report_title=#{reportTitle}, report_content=#{reportContent}, " +
            "report_data=#{reportData}, version=#{version}, status=#{status} WHERE id=#{id}")
    int update(AnalysisReport report);
    
    /**
     * 根据ID查询
     */
    @Select("SELECT * FROM tb_analysis_report WHERE id=#{id}")
    AnalysisReport findById(Long id);
    
    /**
     * 根据用户ID查询报告列表
     */
    @Select("SELECT * FROM tb_analysis_report WHERE user_id=#{userId} AND status=1 " +
            "ORDER BY create_time DESC LIMIT #{limit} OFFSET #{offset}")
    List<AnalysisReport> findByUserId(@Param("userId") Long userId, @Param("offset") int offset, @Param("limit") int limit);
    
    /**
     * 根据用户ID和报告类型查询
     */
    @Select("SELECT * FROM tb_analysis_report WHERE user_id=#{userId} AND report_type=#{reportType} AND status=1 " +
            "ORDER BY create_time DESC LIMIT #{limit} OFFSET #{offset}")
    List<AnalysisReport> findByUserIdAndType(@Param("userId") Long userId, @Param("reportType") String reportType,
                                              @Param("offset") int offset, @Param("limit") int limit);
    
    /**
     * 增加查看次数
     */
    @Update("UPDATE tb_analysis_report SET view_count=view_count+1, last_view_time=NOW() WHERE id=#{id}")
    int incrementViewCount(Long id);
    
    /**
     * 增加导出次数
     */
    @Update("UPDATE tb_analysis_report SET export_count=export_count+1 WHERE id=#{id}")
    int incrementExportCount(Long id);
    
    /**
     * 统计用户的报告数量
     */
    @Select("SELECT COUNT(*) FROM tb_analysis_report WHERE user_id=#{userId} AND status=1")
    int countByUserId(Long userId);
    
    /**
     * 删除报告
     */
    @Delete("DELETE FROM tb_analysis_report WHERE id=#{id}")
    int deleteById(Long id);
}

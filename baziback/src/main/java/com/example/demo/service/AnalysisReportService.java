package com.example.demo.service;

import com.example.demo.entity.AnalysisReport;
import com.example.demo.enums.ErrorCode;
import com.example.demo.exception.BusinessException;
import com.example.demo.mapper.AnalysisReportMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 深度分析报告服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AnalysisReportService {
    
    private final AnalysisReportMapper reportMapper;
    
    /**
     * 创建报告
     */
    @Transactional
    public AnalysisReport createReport(AnalysisReport report) {
        if (report.getVersion() == null) {
            report.setVersion(1);
        }
        if (report.getStatus() == null) {
            report.setStatus(1); // 默认已发布
        }
        if (report.getViewCount() == null) {
            report.setViewCount(0);
        }
        if (report.getExportCount() == null) {
            report.setExportCount(0);
        }
        
        int result = reportMapper.insert(report);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.REPORT_GENERATION_FAILED);
        }
        
        log.info("创建报告成功，用户ID：{}，报告ID：{}", report.getUserId(), report.getId());
        return report;
    }
    
    /**
     * 更新报告
     */
    @Transactional
    public AnalysisReport updateReport(AnalysisReport report) {
        AnalysisReport existing = reportMapper.findById(report.getId());
        if (existing == null) {
            throw new BusinessException(ErrorCode.REPORT_NOT_FOUND);
        }
        
        // 更新版本号
        report.setVersion(existing.getVersion() + 1);
        
        int result = reportMapper.update(report);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.DATABASE_ERROR, "更新报告失败");
        }
        
        log.info("更新报告成功，报告ID：{}", report.getId());
        return reportMapper.findById(report.getId());
    }
    
    /**
     * 获取报告详情
     */
    @Transactional
    public AnalysisReport getReport(Long id) {
        AnalysisReport report = reportMapper.findById(id);
        if (report == null) {
            throw new BusinessException(ErrorCode.REPORT_NOT_FOUND);
        }
        
        // 增加查看次数
        reportMapper.incrementViewCount(id);
        
        return report;
    }
    
    /**
     * 获取用户的报告列表（分页）
     */
    public List<AnalysisReport> getUserReportList(Long userId, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        return reportMapper.findByUserId(userId, offset, pageSize);
    }
    
    /**
     * 根据报告类型获取列表（分页）
     */
    public List<AnalysisReport> getUserReportByType(Long userId, String reportType, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        return reportMapper.findByUserIdAndType(userId, reportType, offset, pageSize);
    }
    
    /**
     * 导出报告
     */
    @Transactional
    public void exportReport(Long userId, Long reportId) {
        AnalysisReport report = reportMapper.findById(reportId);
        if (report == null) {
            throw new BusinessException(ErrorCode.REPORT_NOT_FOUND);
        }
        
        if (!report.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED);
        }
        
        // 增加导出次数
        reportMapper.incrementExportCount(reportId);
        
        log.info("导出报告成功，用户ID：{}，报告ID：{}", userId, reportId);
    }
    
    /**
     * 统计用户的报告数量
     */
    public int countUserReports(Long userId) {
        return reportMapper.countByUserId(userId);
    }
    
    /**
     * 删除报告
     */
    @Transactional
    public void deleteReport(Long userId, Long reportId) {
        AnalysisReport report = reportMapper.findById(reportId);
        if (report == null) {
            throw new BusinessException(ErrorCode.REPORT_NOT_FOUND);
        }
        
        if (!report.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED);
        }
        
        int result = reportMapper.deleteById(reportId);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.DATABASE_ERROR, "删除报告失败");
        }
        
        log.info("删除报告成功，用户ID：{}，报告ID：{}", userId, reportId);
    }
}

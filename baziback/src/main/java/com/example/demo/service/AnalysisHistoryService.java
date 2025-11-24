package com.example.demo.service;

import com.example.demo.entity.AnalysisHistory;
import com.example.demo.enums.ErrorCode;
import com.example.demo.exception.BusinessException;
import com.example.demo.mapper.AnalysisHistoryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 分析历史记录服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AnalysisHistoryService {
    
    private final AnalysisHistoryMapper historyMapper;
    
    /**
     * 保存分析历史
     */
    @Transactional
    public AnalysisHistory saveHistory(AnalysisHistory history) {
        int result = historyMapper.insert(history);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.DATABASE_ERROR, "保存分析历史失败");
        }
        
        log.info("保存分析历史成功，用户ID：{}，分析类型：{}", history.getUserId(), history.getAnalysisType());
        return history;
    }
    
    /**
     * 获取分析历史
     */
    public AnalysisHistory getHistory(Long id) {
        AnalysisHistory history = historyMapper.findById(id);
        if (history == null) {
            throw new BusinessException(ErrorCode.ANALYSIS_HISTORY_NOT_FOUND);
        }
        return history;
    }
    
    /**
     * 获取用户的分析历史列表（分页）
     */
    public List<AnalysisHistory> getUserHistoryList(Long userId, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        return historyMapper.findByUserId(userId, offset, pageSize);
    }
    
    /**
     * 根据分析类型获取历史列表（分页）
     */
    public List<AnalysisHistory> getUserHistoryByType(Long userId, String analysisType, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        return historyMapper.findByUserIdAndType(userId, analysisType, offset, pageSize);
    }
    
    /**
     * 获取用户的收藏列表
     */
    public List<AnalysisHistory> getUserFavorites(Long userId) {
        return historyMapper.findFavoritesByUserId(userId);
    }
    
    /**
     * 收藏/取消收藏
     */
    @Transactional
    public void toggleFavorite(Long userId, Long historyId) {
        AnalysisHistory history = historyMapper.findById(historyId);
        if (history == null) {
            throw new BusinessException(ErrorCode.ANALYSIS_HISTORY_NOT_FOUND);
        }
        
        if (!history.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED);
        }
        
        int newStatus = (history.getIsFavorite() == null || history.getIsFavorite() == 0) ? 1 : 0;
        historyMapper.updateFavorite(historyId, newStatus);
        
        log.info("更新收藏状态成功，历史ID：{}，新状态：{}", historyId, newStatus);
    }
    
    /**
     * 统计用户的分析次数
     */
    public int countUserAnalysis(Long userId) {
        return historyMapper.countByUserId(userId);
    }
    
    /**
     * 统计用户某类型的分析次数
     */
    public int countUserAnalysisByType(Long userId, String analysisType) {
        return historyMapper.countByUserIdAndType(userId, analysisType);
    }
    
    /**
     * 删除分析历史
     */
    @Transactional
    public void deleteHistory(Long userId, Long historyId) {
        AnalysisHistory history = historyMapper.findById(historyId);
        if (history == null) {
            throw new BusinessException(ErrorCode.ANALYSIS_HISTORY_NOT_FOUND);
        }
        
        if (!history.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED);
        }
        
        int result = historyMapper.deleteById(historyId);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.DATABASE_ERROR, "删除分析历史失败");
        }
        
        log.info("删除分析历史成功，用户ID：{}，历史ID：{}", userId, historyId);
    }
}

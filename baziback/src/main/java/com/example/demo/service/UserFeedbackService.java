package com.example.demo.service;

import com.example.demo.entity.UserFeedback;
import com.example.demo.enums.ErrorCode;
import com.example.demo.exception.BusinessException;
import com.example.demo.mapper.UserFeedbackMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用户反馈服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserFeedbackService {
    
    private final UserFeedbackMapper feedbackMapper;
    
    /**
     * 提交反馈
     */
    @Transactional
    public UserFeedback submitFeedback(UserFeedback feedback) {
        // 验证评分范围
        if (feedback.getRating() != null && (feedback.getRating() < 1 || feedback.getRating() > 5)) {
            throw new BusinessException(ErrorCode.RATING_OUT_OF_RANGE);
        }
        
        // 验证反馈内容
        if (feedback.getContent() == null || feedback.getContent().trim().isEmpty()) {
            throw new BusinessException(ErrorCode.FEEDBACK_CONTENT_EMPTY);
        }
        
        if (feedback.getStatus() == null) {
            feedback.setStatus(0); // 默认待处理
        }
        
        int result = feedbackMapper.insert(feedback);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.DATABASE_ERROR, "提交反馈失败");
        }
        
        log.info("提交反馈成功，用户ID：{}，反馈ID：{}", feedback.getUserId(), feedback.getId());
        return feedback;
    }
    
    /**
     * 获取反馈详情
     */
    public UserFeedback getFeedback(Long id) {
        UserFeedback feedback = feedbackMapper.findById(id);
        if (feedback == null) {
            throw new BusinessException(ErrorCode.FEEDBACK_NOT_FOUND);
        }
        return feedback;
    }
    
    /**
     * 获取用户的反馈列表
     */
    public List<UserFeedback> getUserFeedbackList(Long userId) {
        return feedbackMapper.findByUserId(userId);
    }
    
    /**
     * 根据类型获取反馈列表
     */
    public List<UserFeedback> getFeedbackByType(String feedbackType) {
        return feedbackMapper.findByType(feedbackType);
    }
    
    /**
     * 获取待处理的反馈列表
     */
    public List<UserFeedback> getPendingFeedback() {
        return feedbackMapper.findPending();
    }
    
    /**
     * 管理员回复反馈
     */
    @Transactional
    public void replyFeedback(Long feedbackId, String adminReply) {
        UserFeedback feedback = feedbackMapper.findById(feedbackId);
        if (feedback == null) {
            throw new BusinessException(ErrorCode.FEEDBACK_NOT_FOUND);
        }
        
        if (feedback.getStatus() == 2) {
            throw new BusinessException(ErrorCode.FEEDBACK_ALREADY_PROCESSED, "反馈已关闭");
        }
        
        int result = feedbackMapper.updateReply(feedbackId, 1, adminReply);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.DATABASE_ERROR, "回复反馈失败");
        }
        
        log.info("回复反馈成功，反馈ID：{}", feedbackId);
    }
    
    /**
     * 统计用户的反馈数量
     */
    public int countUserFeedback(Long userId) {
        return feedbackMapper.countByUserId(userId);
    }
    
    /**
     * 删除反馈
     */
    @Transactional
    public void deleteFeedback(Long userId, Long feedbackId) {
        UserFeedback feedback = feedbackMapper.findById(feedbackId);
        if (feedback == null) {
            throw new BusinessException(ErrorCode.FEEDBACK_NOT_FOUND);
        }
        
        if (!feedback.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED);
        }
        
        int result = feedbackMapper.deleteById(feedbackId);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.DATABASE_ERROR, "删除反馈失败");
        }
        
        log.info("删除反馈成功，用户ID：{}，反馈ID：{}", userId, feedbackId);
    }
}

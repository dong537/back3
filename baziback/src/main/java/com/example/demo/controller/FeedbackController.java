package com.example.demo.controller;

import com.example.demo.annotation.RequireAuth;
import com.example.demo.dto.request.feedback.SubmitFeedbackRequest;
import com.example.demo.dto.response.Result;
import com.example.demo.entity.UserFeedback;
import com.example.demo.service.UserFeedbackService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户反馈控制器
 */
@Slf4j
@Tag(name = "用户反馈管理", description = "用户反馈的提交和查询")
@RestController
@RequestMapping("/api/feedback")
@RequiredArgsConstructor
public class FeedbackController {
    
    private final UserFeedbackService feedbackService;
    private final ObjectMapper objectMapper;
    
    /**
     * 提交反馈
     */
    @Operation(summary = "提交反馈", description = "用户提交反馈意见")
    @RequireAuth
    @PostMapping
    public Result<UserFeedback> submitFeedback(
            @Validated @RequestBody SubmitFeedbackRequest request,
            HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            log.info("提交反馈，用户ID：{}，反馈类型：{}", userId, request.getFeedbackType());
            
            UserFeedback feedback = new UserFeedback();
            feedback.setUserId(userId);
            feedback.setFeedbackType(request.getFeedbackType());
            feedback.setRelatedId(request.getRelatedId());
            feedback.setRating(request.getRating());
            feedback.setContent(request.getContent());
            
            // 转换标签为JSON
            if (request.getTags() != null && !request.getTags().isEmpty()) {
                feedback.setTags(objectMapper.writeValueAsString(request.getTags()));
            }
            
            UserFeedback created = feedbackService.submitFeedback(feedback);
            return Result.success(created);
        } catch (Exception e) {
            log.error("提交反馈失败", e);
            return Result.error("提交反馈失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取用户反馈列表
     */
    @Operation(summary = "获取反馈列表", description = "获取用户的反馈历史")
    @RequireAuth
    @GetMapping("/list")
    public Result<List<UserFeedback>> getFeedbackList(HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            log.info("获取反馈列表，用户ID：{}", userId);
            
            List<UserFeedback> list = feedbackService.getUserFeedbackList(userId);
            return Result.success(list);
        } catch (Exception e) {
            log.error("获取反馈列表失败", e);
            return Result.error("获取反馈列表失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取反馈详情
     */
    @Operation(summary = "获取反馈详情", description = "获取指定反馈的详细信息")
    @RequireAuth
    @GetMapping("/{id}")
    public Result<UserFeedback> getFeedback(@PathVariable Long id) {
        try {
            log.info("获取反馈详情，反馈ID：{}", id);
            
            UserFeedback feedback = feedbackService.getFeedback(id);
            return Result.success(feedback);
        } catch (Exception e) {
            log.error("获取反馈详情失败", e);
            return Result.error("获取反馈详情失败：" + e.getMessage());
        }
    }
}

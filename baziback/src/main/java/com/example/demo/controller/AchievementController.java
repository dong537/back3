package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.entity.Achievement;
import com.example.demo.mapper.AchievementMapper;
import com.example.demo.service.AchievementService;
import com.example.demo.service.SseEmitterService;
import com.example.demo.util.AuthUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

/**
 * 成就控制器
 */
@RestController
@RequestMapping("/api/achievement")
@RequiredArgsConstructor
@Slf4j
public class AchievementController {

    private final AchievementService achievementService;
    private final SseEmitterService sseEmitterService;
    private final AuthUtil authUtil;

    /**
     * 获取所有成就列表
     */
    @GetMapping("/list")
    public Result<List<Achievement>> getAllAchievements() {
        log.info("查询所有成就列表");
        List<Achievement> achievements = achievementService.getAllAchievements();
        log.info("查询到 {} 个成就", achievements.size());
        return Result.success(achievements);
    }

    /**
     * 获取用户已解锁的成就
     */
    @GetMapping("/user")
    public Result<List<AchievementMapper.UserAchievementWithInfo>> getUserAchievements(
            @RequestHeader(value = "Authorization", required = false) String token) {
        Long userId = authUtil.requireUserId(token);
        log.info("查询用户成就: userId={}", userId);
        List<AchievementMapper.UserAchievementWithInfo> achievements = achievementService.getUserAchievements(userId);
        log.info("用户 {} 已解锁 {} 个成就", userId, achievements.size());
        return Result.success(achievements);
    }

    /**
     * 获取用户成就统计
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> getUserAchievementStats(
            @RequestHeader(value = "Authorization", required = false) String token) {
        Long userId = authUtil.requireUserId(token);
        log.info("查询用户成就统计: userId={}", userId);
        Map<String, Object> stats = achievementService.getUserAchievementStats(userId);
        return Result.success(stats);
    }

    /**
     * 成就相关的 SSE 订阅（WebFlux）
     * 前端可以使用 EventSource 连接：/api/achievement/sse
     * 支持从URL参数或Header中获取token
     */
    @GetMapping(value = "/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<Map<String, Object>>> subscribeAchievementSse(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestParam(value = "token", required = false) String tokenParam) {
        // 优先使用Header中的token，如果没有则使用URL参数中的token
        String authToken = token != null ? token : (tokenParam != null ? "Bearer " + tokenParam : null);
        Long userId = authUtil.requireUserId(authToken);
        log.info("用户 {} 订阅成就 SSE 流", userId);
        return sseEmitterService.subscribe(userId);
    }
}

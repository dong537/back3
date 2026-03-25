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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/achievement")
@RequiredArgsConstructor
@Slf4j
public class AchievementController {

    private final AchievementService achievementService;
    private final SseEmitterService sseEmitterService;
    private final AuthUtil authUtil;

    @GetMapping("/list")
    public Result<List<Achievement>> getAllAchievements() {
        log.info("查询所有成就列表");
        List<Achievement> achievements = achievementService.getAllAchievements();
        log.info("查询到 {} 个成就", achievements.size());
        return Result.success(achievements);
    }

    @GetMapping("/user")
    public Result<List<AchievementMapper.UserAchievementWithInfo>> getUserAchievements(
            @RequestHeader(value = "Authorization", required = false) String token) {
        Long userId = authUtil.requireUserId(token);
        log.info("查询用户成就: userId={}", userId);
        achievementService.reconcileAchievements(userId);
        List<AchievementMapper.UserAchievementWithInfo> achievements =
                achievementService.getUserAchievements(userId);
        log.info("用户 {} 已解锁 {} 个成就", userId, achievements.size());
        return Result.success(achievements);
    }

    @GetMapping("/stats")
    public Result<Map<String, Object>> getUserAchievementStats(
            @RequestHeader(value = "Authorization", required = false) String token) {
        Long userId = authUtil.requireUserId(token);
        log.info("查询用户成就统计: userId={}", userId);
        achievementService.reconcileAchievements(userId);
        Map<String, Object> stats = achievementService.getUserAchievementStats(userId);
        return Result.success(stats);
    }

    @GetMapping(value = "/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<Map<String, Object>>> subscribeAchievementSse(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestParam(value = "token", required = false) String tokenParam) {
        String authToken = token != null ? token : (tokenParam != null ? "Bearer " + tokenParam : null);
        Long userId = authUtil.requireUserId(authToken);
        log.info("用户 {} 订阅成就 SSE 流", userId);
        return sseEmitterService.subscribe(userId, "achievement");
    }
}

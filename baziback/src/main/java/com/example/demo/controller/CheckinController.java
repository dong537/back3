package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.service.CheckinService;
import com.example.demo.util.AuthUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 打卡签到控制器
 */
@RestController
@RequestMapping("/api/checkin")
@RequiredArgsConstructor
@Slf4j
public class CheckinController {

    private final CheckinService checkinService;
    private final AuthUtil authUtil;

    /**
     * 执行打卡
     * POST /api/checkin
     */
    @PostMapping
    public Result<Map<String, Object>> doCheckin(
            @RequestHeader(value = "Authorization", required = false) String token) {
        Long userId = authUtil.requireUserId(token);
        log.info("用户打卡: userId={}", userId);
        Map<String, Object> result = checkinService.doCheckin(userId);
        log.info("打卡成功: userId={}, result={}", userId, result);
        return Result.success(result);
    }

    /**
     * 获取今日打卡状态
     * GET /api/checkin/today
     */
    @GetMapping("/today")
    public Result<Map<String, Object>> getTodayStatus(
            @RequestHeader(value = "Authorization", required = false) String token) {
        Long userId = authUtil.requireUserId(token);
        Map<String, Object> result = checkinService.getTodayStatus(userId);
        return Result.success(result);
    }

    /**
     * 获取本周打卡进度
     * GET /api/checkin/weekly
     */
    @GetMapping("/weekly")
    public Result<Map<String, Object>> getWeeklyProgress(
            @RequestHeader(value = "Authorization", required = false) String token) {
        Long userId = authUtil.requireUserId(token);
        Map<String, Object> result = checkinService.getWeeklyProgress(userId);
        return Result.success(result);
    }

    /**
     * 获取连续打卡信息
     * GET /api/checkin/streak
     */
    @GetMapping("/streak")
    public Result<Map<String, Object>> getStreakInfo(
            @RequestHeader(value = "Authorization", required = false) String token) {
        Long userId = authUtil.requireUserId(token);
        Map<String, Object> result = checkinService.getStreakInfo(userId);
        return Result.success(result);
    }
}

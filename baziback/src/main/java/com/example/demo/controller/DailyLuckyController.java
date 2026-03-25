package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.entity.DailyLucky;
import com.example.demo.exception.BusinessException;
import com.example.demo.service.DailyLuckyService;
import com.example.demo.util.AuthUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 每日幸运控制器
 */
@RestController
@RequestMapping("/api/daily-lucky")
@RequiredArgsConstructor
@Slf4j
public class DailyLuckyController {

    private final DailyLuckyService dailyLuckyService;
    private final AuthUtil authUtil;

    /**
     * 获取今天的每日幸运
     * GET /api/daily-lucky/today
     */
    @GetMapping("/today")
    public Result<Map<String, Object>> getTodayLucky() {
        DailyLucky todayLucky = dailyLuckyService.getTodayLucky();
        Map<String, Object> formatted = dailyLuckyService.formatDailyLucky(todayLucky);
        return Result.success(formatted);
    }

    /**
     * 获取指定日期的每日幸运
     * GET /api/daily-lucky/date?date=2026-01-12
     */
    @GetMapping("/date")
    public Result<Map<String, Object>> getLuckyByDate(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        if (date == null) {
            date = LocalDate.now();
        }
        DailyLucky dailyLucky = dailyLuckyService.getDailyLucky(date);
        Map<String, Object> formatted = dailyLuckyService.formatDailyLucky(dailyLucky);
        return Result.success(formatted);
    }

    /**
     * 获取未来几天的每日幸运
     * GET /api/daily-lucky/future?days=7
     */
    @GetMapping("/future")
    public Result<List<DailyLucky>> getFutureLucky(@RequestParam(defaultValue = "7") int days) {
        if (days < 1 || days > 30) {
            throw new BusinessException("天数必须在1-30之间");
        }
        List<DailyLucky> futureLucky = dailyLuckyService.getFutureLucky(days);
        return Result.success(futureLucky);
    }

    /**
     * 获取日期范围的每日幸运
     * GET /api/daily-lucky/range?startDate=2026-01-01&endDate=2026-01-31
     */
    @GetMapping("/range")
    public Result<List<DailyLucky>> getLuckyByRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new BusinessException("开始日期不能晚于结束日期");
        }
        List<DailyLucky> luckyList = dailyLuckyService.getLuckyByDateRange(startDate, endDate);
        return Result.success(luckyList);
    }

    /**
     * 创建或更新每日幸运（管理员接口）
     * POST /api/daily-lucky
     */
    @PostMapping
    public Result<DailyLucky> saveDailyLucky(
            @RequestBody DailyLucky dailyLucky,
            @RequestHeader(value = "Authorization", required = false) String token) {
        // 需要登录认证（后续可扩展管理员权限检查）
        authUtil.requireUserId(token);
        if (dailyLucky == null || dailyLucky.getLuckyDate() == null) {
            throw new BusinessException("每日幸运数据不能为空");
        }
        DailyLucky saved = dailyLuckyService.saveDailyLucky(dailyLucky);
        return Result.success(saved);
    }
}

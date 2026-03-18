package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.entity.DailyFortuneDetail;
import com.example.demo.exception.BusinessException;
import com.example.demo.service.DailyFortuneDetailService;
import com.example.demo.util.AuthUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 每日运势详情控制器
 */
@RestController
@RequestMapping("/api/daily-fortune-detail")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin
public class DailyFortuneDetailController {

    private final DailyFortuneDetailService dailyFortuneDetailService;
    private final AuthUtil authUtil;

    /**
     * 获取今天的每日运势详情（为每个用户每日随机抽取一条）
     * GET /api/daily-fortune-detail/today
     */
    @GetMapping("/today")
    public Result<Map<String, Object>> getTodayFortuneDetail(
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            Long userId = authUtil.tryGetUserId(token);
            DailyFortuneDetail todayDetail = dailyFortuneDetailService.getTodayFortuneDetail(userId);
            
            if (todayDetail == null) {
                throw new BusinessException("无法获取运势详情");
            }

            Map<String, Object> formatted = dailyFortuneDetailService.formatDailyFortuneDetail(todayDetail);
            // 标记数据来源
            formatted.put("isRandom", true);
            formatted.put("fortuneDate", LocalDate.now().toString()); // 强制设置为今天
            return Result.success(formatted);
        } catch (Exception e) {
            log.error("获取今日运势详情失败", e);
            throw new BusinessException("获取今日运势详情失败: " + e.getMessage());
        }
    }
    
    /**
     * 随机生成指定日期的运势详情（不保存到数据库）
     * GET /api/daily-fortune-detail/random?date=2026-01-12
     */
    @GetMapping("/random")
    public Result<Map<String, Object>> getRandomFortuneDetail(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            if (date == null) {
                date = LocalDate.now();
            }
            // 不允许查询未来日期
            if (date.isAfter(LocalDate.now())) {
                throw new BusinessException("不能生成未来日期的运势详情");
            }
            // 强制生成随机数据（不查询数据库）
            DailyFortuneDetail randomDetail = dailyFortuneDetailService.generateRandomFortuneDetail(date);
            Map<String, Object> formatted = dailyFortuneDetailService.formatDailyFortuneDetail(randomDetail);
            formatted.put("isRandom", true);
            return Result.success(formatted);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("生成随机运势详情失败: date={}", date, e);
            throw new BusinessException("生成随机运势详情失败: " + e.getMessage());
        }
    }

    /**
     * 获取指定日期的每日运势详情
     * GET /api/daily-fortune-detail/date?date=2026-01-12
     */
    @GetMapping("/date")
    public Result<Map<String, Object>> getFortuneDetailByDate(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            if (date == null) {
                date = LocalDate.now();
            }
            // 不允许查询未来日期
            if (date.isAfter(LocalDate.now())) {
                throw new BusinessException("不能查询未来日期的运势详情");
            }
            DailyFortuneDetail detail = dailyFortuneDetailService.getDailyFortuneDetail(date);
            Map<String, Object> formatted = dailyFortuneDetailService.formatDailyFortuneDetail(detail);
            return Result.success(formatted);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取指定日期运势详情失败: date={}", date, e);
            throw new BusinessException("获取运势详情失败: " + e.getMessage());
        }
    }

    /**
     * 获取未来几天的每日运势详情
     * GET /api/daily-fortune-detail/future?days=7
     */
    @GetMapping("/future")
    public Result<List<DailyFortuneDetail>> getFutureFortuneDetails(@RequestParam(defaultValue = "7") int days) {
        try {
            if (days < 1 || days > 30) {
                throw new BusinessException("天数必须在1-30之间");
            }
            List<DailyFortuneDetail> futureDetails = dailyFortuneDetailService.getFutureFortuneDetails(days);
            return Result.success(futureDetails);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取未来运势详情失败: days={}", days, e);
            throw new BusinessException("获取未来运势详情失败: " + e.getMessage());
        }
    }

    /**
     * 获取日期范围的每日运势详情
     * GET /api/daily-fortune-detail/range?startDate=2026-01-01&endDate=2026-01-31
     */
    @GetMapping("/range")
    public Result<List<DailyFortuneDetail>> getFortuneDetailsByRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            if (startDate.isAfter(endDate)) {
                throw new BusinessException("开始日期不能晚于结束日期");
            }
            // 限制查询范围不超过90天
            if (startDate.until(endDate).getDays() > 90) {
                throw new BusinessException("查询日期范围不能超过90天");
            }
            List<DailyFortuneDetail> details = dailyFortuneDetailService.getFortuneDetailsByDateRange(startDate, endDate);
            return Result.success(details);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取日期范围运势详情失败: startDate={}, endDate={}", startDate, endDate, e);
            throw new BusinessException("获取运势详情失败: " + e.getMessage());
        }
    }

    /**
     * 创建或更新每日运势详情（管理员接口）
     * POST /api/daily-fortune-detail
     */
    @PostMapping
    public Result<DailyFortuneDetail> saveDailyFortuneDetail(
            @RequestBody DailyFortuneDetail dailyFortuneDetail,
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            // 需要登录认证（后续可扩展管理员权限检查）
            authUtil.requireUserId(token);
            if (dailyFortuneDetail == null || dailyFortuneDetail.getFortuneDate() == null) {
                throw new BusinessException("每日运势详情数据不能为空");
            }
            DailyFortuneDetail saved = dailyFortuneDetailService.saveDailyFortuneDetail(dailyFortuneDetail);
            Long userId = authUtil.tryGetUserId(token);
            log.info("保存运势详情成功: date={}, userId={}", 
                    dailyFortuneDetail.getFortuneDate(), userId);
            return Result.success(saved);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("保存运势详情失败", e);
            throw new BusinessException("保存运势详情失败: " + e.getMessage());
        }
    }

    /**
     * 批量创建每日运势详情（管理员接口）
     * POST /api/daily-fortune-detail/batch
     */
    @PostMapping("/batch")
    public Result<Integer> batchSaveDailyFortuneDetails(
            @RequestBody List<DailyFortuneDetail> dailyFortuneDetails,
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            authUtil.requireUserId(token);
            if (dailyFortuneDetails == null || dailyFortuneDetails.isEmpty()) {
                throw new BusinessException("每日运势详情数据不能为空");
            }
            // 限制批量插入数量
            if (dailyFortuneDetails.size() > 100) {
                throw new BusinessException("批量插入数量不能超过100条");
            }
            int count = dailyFortuneDetailService.batchSave(dailyFortuneDetails);
            Long userId = authUtil.tryGetUserId(token);
            log.info("批量保存运势详情成功: count={}, userId={}", count, userId);
            return Result.success(count);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("批量保存运势详情失败", e);
            throw new BusinessException("批量保存运势详情失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取运势趋势分析（对比今天和昨天的运势变化）
     * GET /api/daily-fortune-detail/trend?date=2026-01-12
     */
    @GetMapping("/trend")
    public Result<Map<String, Object>> getFortuneTrend(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            if (date == null) {
                date = LocalDate.now();
            }
            if (date.isAfter(LocalDate.now())) {
                throw new BusinessException("不能查询未来日期的运势趋势");
            }
            Map<String, Object> trend = dailyFortuneDetailService.getFortuneTrend(date);
            return Result.success(trend);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取运势趋势失败: date={}", date, e);
            throw new BusinessException("获取运势趋势失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取运势统计信息
     * GET /api/daily-fortune-detail/stats?date=2026-01-12
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> getFortuneStats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            if (date == null) {
                date = LocalDate.now();
            }
            if (date.isAfter(LocalDate.now())) {
                throw new BusinessException("不能查询未来日期的运势统计");
            }
            
            DailyFortuneDetail detail = dailyFortuneDetailService.getDailyFortuneDetail(date);
            Map<String, Object> stats = new HashMap<>();
            stats.put("date", date.toString());
            stats.put("averageScore", dailyFortuneDetailService.getAverageScore(detail));
            stats.put("bestAspect", dailyFortuneDetailService.getBestAspect(detail));
            stats.put("weakestAspect", dailyFortuneDetailService.getWeakestAspect(detail));
            stats.put("detail", dailyFortuneDetailService.formatDailyFortuneDetail(detail));
            
            return Result.success(stats);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取运势统计失败: date={}", date, e);
            throw new BusinessException("获取运势统计失败: " + e.getMessage());
        }
    }
}

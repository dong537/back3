package com.example.demo.controller;

import com.example.demo.entity.TbCalculationRecord;
import com.example.demo.service.CalculationRecordService;
import com.example.demo.common.Result;
import com.example.demo.util.AuthUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 测算记录 Controller
 */
@RestController
@RequestMapping("/api/calculation-records")
@RequiredArgsConstructor
@Slf4j
public class CalculationRecordController {

    private final CalculationRecordService calculationRecordService;
    private final AuthUtil authUtil;

    /**
     * 保存测算记录
     */
    @PostMapping
    public Result<TbCalculationRecord> saveRecord(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestBody TbCalculationRecord record) {
        // 从token中获取用户ID并设置到record中
        Long userId = authUtil.tryGetUserId(token);
        if (userId != null) {
            record.setUserId(userId);
        } else {
            // 如果未登录，记录警告日志
            log.warn("保存记录时用户未登录，recordType={}, recordTitle={}", 
                    record.getRecordType(), record.getRecordTitle());
        }
        TbCalculationRecord saved = calculationRecordService.saveRecord(record);
        return Result.success(saved);
    }

    /**
     * 获取记录详情
     */
    @GetMapping("/{id}")
    public Result<TbCalculationRecord> getRecord(@PathVariable Long id) {
        TbCalculationRecord record = calculationRecordService.getRecord(id);
        if (record == null) {
            return Result.error("记录不存在");
        }
        return Result.success(record);
    }

    /**
     * 获取当前用户的所有记录
     */
    @GetMapping
    public Result<List<TbCalculationRecord>> getUserRecords(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestParam(required = false) String recordType,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = authUtil.requireUserId(token);
        
        if (userId == null) {
            return Result.error("用户未登录");
        }

        List<TbCalculationRecord> records;
        if (recordType != null && !recordType.isEmpty()) {
            records = calculationRecordService.getUserRecordsByType(userId, recordType);
        } else {
            records = calculationRecordService.getUserRecordsPaged(userId, page, size);
        }
        
        return Result.success(records);
    }

    /**
     * 更新记录
     */
    @PutMapping("/{id}")
    public Result<TbCalculationRecord> updateRecord(
            @PathVariable Long id,
            @RequestBody TbCalculationRecord record) {
        record.setId(id);
        TbCalculationRecord updated = calculationRecordService.updateRecord(record);
        return Result.success(updated);
    }

    /**
     * 删除记录
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteRecord(@PathVariable Long id) {
        calculationRecordService.deleteRecord(id);
        return Result.success();
    }

    /**
     * 获取用户记录统计
     */
    @GetMapping("/stats")
    public Result<Map<String, Integer>> getStats(
            @RequestHeader(value = "Authorization", required = false) String token) {
        Long userId = authUtil.requireUserId(token);

        int total = calculationRecordService.getUserRecordCount(userId);
        int baziCount = calculationRecordService.getUserRecordCountByType(userId, "bazi");
        int tarotCount = calculationRecordService.getUserRecordCountByType(userId, "tarot");
        int yijingCount = calculationRecordService.getUserRecordCountByType(userId, "yijing");
        int compatibilityCount = calculationRecordService.getUserRecordCountByType(userId, "compatibility");

        Map<String, Integer> stats = new HashMap<>();
        stats.put("total", total);
        stats.put("bazi", baziCount);
        stats.put("tarot", tarotCount);
        stats.put("yijing", yijingCount);
        stats.put("compatibility", compatibilityCount);

        return Result.success(stats);
    }
}

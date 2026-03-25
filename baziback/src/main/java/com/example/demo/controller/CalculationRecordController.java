package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.entity.TbCalculationRecord;
import com.example.demo.service.CalculationRecordService;
import com.example.demo.util.AuthUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

    @PostMapping
    public Result<TbCalculationRecord> saveRecord(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestBody TbCalculationRecord record) {
        Long userId = authUtil.tryGetUserId(token);
        if (userId != null) {
            record.setUserId(userId);
        } else {
            log.warn("保存记录时用户未登录, recordType={}, recordTitle={}",
                    record.getRecordType(), record.getRecordTitle());
        }
        return Result.success(calculationRecordService.saveRecord(record));
    }

    @GetMapping("/{id}")
    public Result<TbCalculationRecord> getRecord(
            @RequestHeader(value = "Authorization", required = false) String token,
            @PathVariable Long id) {
        Long userId = authUtil.requireUserId(token);
        return Result.success(calculationRecordService.getUserRecord(userId, id));
    }

    @GetMapping
    public Result<List<TbCalculationRecord>> getUserRecords(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestParam(required = false) String recordType,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = authUtil.requireUserId(token);
        return Result.success(calculationRecordService.getUserRecordsPaged(userId, recordType, page, size));
    }

    @PutMapping("/{id}")
    public Result<TbCalculationRecord> updateRecord(
            @RequestHeader(value = "Authorization", required = false) String token,
            @PathVariable Long id,
            @RequestBody TbCalculationRecord record) {
        Long userId = authUtil.requireUserId(token);
        return Result.success(calculationRecordService.updateUserRecord(userId, id, record));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteRecord(
            @RequestHeader(value = "Authorization", required = false) String token,
            @PathVariable Long id) {
        Long userId = authUtil.requireUserId(token);
        calculationRecordService.deleteUserRecord(userId, id);
        return Result.success();
    }

    @GetMapping("/stats")
    public Result<Map<String, Integer>> getStats(
            @RequestHeader(value = "Authorization", required = false) String token) {
        Long userId = authUtil.requireUserId(token);

        Map<String, Integer> stats = new HashMap<>();
        stats.put("total", calculationRecordService.getUserRecordCount(userId));
        stats.put("bazi", calculationRecordService.getUserRecordCountByType(userId, "bazi"));
        stats.put("tarot", calculationRecordService.getUserRecordCountByType(userId, "tarot"));
        stats.put("yijing", calculationRecordService.getUserRecordCountByType(userId, "yijing"));
        stats.put("compatibility", calculationRecordService.getUserRecordCountByType(userId, "compatibility"));
        return Result.success(stats);
    }
}

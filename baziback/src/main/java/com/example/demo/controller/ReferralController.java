package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.entity.InviteRecord;
import com.example.demo.exception.BusinessException;
import com.example.demo.service.ReferralService;
import com.example.demo.util.AuthUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 推荐/邀请控制器
 */
@RestController
@RequestMapping("/api/referral")
@RequiredArgsConstructor
@Slf4j
public class ReferralController {
    
    private final ReferralService referralService;
    private final AuthUtil authUtil;
    
    /**
     * 获取或生成推荐码
     */
    @GetMapping("/code")
    public Result<String> getReferralCode(@RequestHeader(value = "Authorization", required = false) String token) {
        Long userId = authUtil.requireUserId(token);
        log.info("查询用户推荐码: userId={}", userId);
        String referralCode = referralService.getOrCreateReferralCode(userId);
        log.info("获取推荐码成功: userId={}, referralCode={}", userId, referralCode);
        return Result.success(referralCode);
    }
    
    /**
     * 使用推荐码
     */
    @PostMapping("/use")
    public Result<Void> useReferralCode(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestBody Map<String, String> request) {
        Long userId = authUtil.requireUserId(token);
        if (request == null) {
            throw new BusinessException("请求参数不能为空");
        }
        String referralCode = request.get("referralCode");
        if (referralCode == null || referralCode.isEmpty()) {
            throw new BusinessException("推荐码不能为空");
        }
        log.info("用户使用推荐码: userId={}, referralCode={}", userId, referralCode);
        referralService.useReferralCode(userId, referralCode);
        log.info("使用推荐码成功: userId={}, referralCode={}", userId, referralCode);
        return Result.success(null);
    }
    
    /**
     * 获取邀请统计
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> getInviteStats(
            @RequestHeader(value = "Authorization", required = false) String token) {
        Long userId = authUtil.requireUserId(token);
        log.info("查询邀请统计: userId={}", userId);
        Map<String, Object> stats = referralService.getInviteStats(userId);
        return Result.success(stats);
    }
    
    /**
     * 获取邀请记录列表
     */
    @GetMapping("/records")
    public Result<List<InviteRecord>> getInviteRecords(
            @RequestHeader(value = "Authorization", required = false) String token) {
        Long userId = authUtil.requireUserId(token);
        log.info("查询邀请记录: userId={}", userId);
        List<InviteRecord> records = referralService.getInviteRecords(userId);
        log.info("查询到 {} 条邀请记录: userId={}", records.size(), userId);
        return Result.success(records);
    }
    
}

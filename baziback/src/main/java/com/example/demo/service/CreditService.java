package com.example.demo.service;

import com.example.demo.exception.BusinessException;
import com.example.demo.mapper.CreditMapper;
import com.example.demo.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * 积分服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CreditService {
    
    private final CreditMapper creditMapper;
    private final UserMapper userMapper;
    private final SseEmitterService sseEmitterService;
    
    /**
     * 添加积分
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean addPoints(Long userId, Integer points, String description, Long relatedOrderId) {
        if (userId == null || points == null || points <= 0) {
            log.warn("添加积分参数无效: userId={}, points={}", userId, points);
            return false;
        }

        // 1) 锁定余额行（FOR UPDATE），确保并发安全
        Integer balanceBefore = creditMapper.getBalanceByUserIdForUpdate(userId);

        // 2) 若 tb_credit 不存在该用户，先初始化
        if (balanceBefore == null) {
            var user = userMapper.findById(userId);
            balanceBefore = (user != null && user.getCurrentPoints() != null) ? user.getCurrentPoints() : 0;
            creditMapper.initUserCredit(userId, balanceBefore);
            // 再次 FOR UPDATE 读取，确保进入锁定态
            balanceBefore = creditMapper.getBalanceByUserIdForUpdate(userId);
            if (balanceBefore == null) {
                log.error("初始化用户积分记录失败: userId={}", userId);
                throw new BusinessException("初始化用户积分记录失败");
            }
        }

        // 3) 更新 tb_credit
        int updatedCredit = creditMapper.addPoints(userId, points);
        if (updatedCredit <= 0) {
            log.error("更新 tb_credit 失败: userId={}, points={}", userId, points);
            throw new BusinessException("更新积分失败");
        }

        // 4) 更新 tb_user 冗余字段
        int updatedUser = userMapper.updatePoints(userId, points);
        if (updatedUser <= 0) {
            log.error("更新 tb_user 积分失败: userId={}, points={}", userId, points);
            throw new BusinessException("更新用户积分失败");
        }

        // 5) 写流水
        Integer balanceAfter = balanceBefore + points;
        creditMapper.insertTransaction(userId, 4, points, balanceBefore, balanceAfter, description, relatedOrderId);

        log.info("用户 {} 获得 {} 积分，原因：{}，余额：{} -> {}", userId, points, description, balanceBefore, balanceAfter);
        
        // 6) 通过SSE推送积分更新事件
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("type", "CREDIT_UPDATED");
        eventData.put("userId", userId);
        eventData.put("pointsChange", points);
        eventData.put("balanceBefore", balanceBefore);
        eventData.put("balanceAfter", balanceAfter);
        eventData.put("description", description);
        sseEmitterService.sendToUser(userId, "credit", eventData);
        
        return true;
    }
    
    /**
     * 扣除积分
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean deductPoints(Long userId, Integer points, String description) {
        if (userId == null || points == null || points <= 0) {
            log.warn("扣除积分参数无效: userId={}, points={}", userId, points);
            return false;
        }

        // 1) 锁定余额行（FOR UPDATE），确保并发安全
        Integer balanceBefore = creditMapper.getBalanceByUserIdForUpdate(userId);

        // 2) 若 tb_credit 不存在该用户，先初始化（余额按 tb_user 同步）
        if (balanceBefore == null) {
            var user = userMapper.findById(userId);
            balanceBefore = (user != null && user.getCurrentPoints() != null) ? user.getCurrentPoints() : 0;
            creditMapper.initUserCredit(userId, balanceBefore);
            balanceBefore = creditMapper.getBalanceByUserIdForUpdate(userId);
            if (balanceBefore == null) {
                log.error("初始化用户积分记录失败: userId={}", userId);
                throw new BusinessException("初始化用户积分记录失败");
            }
        }

        if (balanceBefore < points) {
            log.warn("用户 {} 积分不足，当前余额：{}，需要扣除：{}", userId, balanceBefore, points);
            return false;
        }

        // 3) 更新 tb_credit（扣减）
        int updatedCredit = creditMapper.addPoints(userId, -points);
        if (updatedCredit <= 0) {
            log.error("扣减 tb_credit 失败: userId={}, points={}", userId, points);
            throw new BusinessException("扣减积分失败");
        }

        // 4) 更新 tb_user 冗余字段
        int updatedUser = userMapper.updatePoints(userId, -points);
        if (updatedUser <= 0) {
            log.error("更新 tb_user 积分失败: userId={}, points={}", userId, points);
            throw new BusinessException("更新用户积分失败");
        }

        // 5) 写流水
        Integer balanceAfter = balanceBefore - points;
        creditMapper.insertTransaction(userId, 2, -points, balanceBefore, balanceAfter, description, null);

        log.info("用户 {} 扣除 {} 积分，原因：{}，余额：{} -> {}", userId, points, description, balanceBefore, balanceAfter);
        
        // 6) 通过SSE推送积分更新事件
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("type", "CREDIT_UPDATED");
        eventData.put("userId", userId);
        eventData.put("pointsChange", -points);
        eventData.put("balanceBefore", balanceBefore);
        eventData.put("balanceAfter", balanceAfter);
        eventData.put("description", description);
        sseEmitterService.sendToUser(userId, "credit", eventData);
        
        return true;
    }
    
    /**
     * 获取用户当前积分
     */
    public Integer getCurrentPoints(Long userId) {
        // 优先从tb_credit表获取
        Integer balance = creditMapper.getBalanceByUserId(userId);
        if (balance != null) {
            return balance;
        }
        
        // 如果tb_credit表中没有，从tb_user表获取
        var user = userMapper.findById(userId);
        if (user != null && user.getCurrentPoints() != null) {
            return user.getCurrentPoints();
        }
        
        return 0;
    }
}

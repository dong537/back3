package com.example.demo.service;

import com.example.demo.exception.BusinessException;
import com.example.demo.mapper.CreditMapper;
import com.example.demo.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

    private static final int TRANSACTION_TYPE_SPEND = 2;
    private static final int TRANSACTION_TYPE_SYSTEM = 4;
    private static final String WATCH_AD_DESCRIPTION = "观看广告";

    @Value("${app.free-features:false}")
    private boolean freeFeatures;

    private final CreditMapper creditMapper;
    private final UserMapper userMapper;
    private final SseEmitterService sseEmitterService;

    @Transactional(rollbackFor = Exception.class)
    public boolean addPoints(Long userId, Integer points, String description, Long relatedOrderId) {
        return addPointsInternal(userId, points, description, relatedOrderId) != null;
    }

    @Transactional(rollbackFor = Exception.class)
    public Integer addPointsAndGetBalance(Long userId, Integer points, String description, Long relatedOrderId) {
        return addPointsInternal(userId, points, description, relatedOrderId);
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean deductPoints(Long userId, Integer points, String description) {
        if (userId == null || points == null || points <= 0) {
            log.warn("扣除积分参数无效: userId={}, points={}", userId, points);
            return false;
        }

        if (freeFeatures) {
            log.info("免积分模式已开启，跳过积分扣除: userId={}, points={}, description={}",
                    userId, points, description);
            return true;
        }

        Integer balanceBefore = getOrCreateBalanceForUpdate(userId);
        if (balanceBefore < points) {
            log.warn("用户 {} 积分不足，当前余额：{}，需要扣除：{}", userId, balanceBefore, points);
            return false;
        }

        int updatedCredit = creditMapper.deductPointsIfEnough(userId, points);
        if (updatedCredit <= 0) {
            log.warn("扣减积分失败，可能是并发导致余额变化: userId={}, points={}, balanceBefore={}",
                    userId, points, balanceBefore);
            return false;
        }

        int updatedUser = userMapper.updatePoints(userId, -points);
        if (updatedUser <= 0) {
            log.error("更新 tb_user 积分失败: userId={}, points={}", userId, points);
            throw new BusinessException("更新用户积分失败");
        }

        Integer balanceAfter = balanceBefore - points;
        creditMapper.insertTransaction(
                userId,
                TRANSACTION_TYPE_SPEND,
                -points,
                balanceBefore,
                balanceAfter,
                description,
                null
        );

        log.info("用户 {} 扣除 {} 积分，原因：{}，余额：{} -> {}",
                userId, points, description, balanceBefore, balanceAfter);
        publishCreditEvent(userId, -points, balanceBefore, balanceAfter, description);
        return true;
    }

    /**
     * Read current balance and lazily backfill tb_credit from tb_user when needed.
     */
    public Integer getCurrentPoints(Long userId) {
        if (userId == null) {
            return 0;
        }

        Integer balance = creditMapper.getBalanceByUserId(userId);
        if (balance != null) {
            return balance;
        }

        Integer initialBalance = resolveInitialBalance(userId);
        creditMapper.initUserCredit(userId, initialBalance);

        Integer syncedBalance = creditMapper.getBalanceByUserId(userId);
        return syncedBalance != null ? syncedBalance : initialBalance;
    }

    public int getTodayWatchAdEarnCount(Long userId) {
        if (userId == null) {
            return 0;
        }
        return creditMapper.countTransactionsTodayByTypeAndDescription(
                userId,
                TRANSACTION_TYPE_SYSTEM,
                WATCH_AD_DESCRIPTION
        );
    }

    private Integer addPointsInternal(Long userId, Integer points, String description, Long relatedOrderId) {
        if (userId == null || points == null || points <= 0) {
            log.warn("添加积分参数无效: userId={}, points={}", userId, points);
            return null;
        }

        Integer balanceBefore = getOrCreateBalanceForUpdate(userId);

        int updatedCredit = creditMapper.addPoints(userId, points);
        if (updatedCredit <= 0) {
            log.error("更新 tb_credit 失败: userId={}, points={}", userId, points);
            throw new BusinessException("更新积分失败");
        }

        int updatedUser = userMapper.updatePoints(userId, points);
        if (updatedUser <= 0) {
            log.error("更新 tb_user 积分失败: userId={}, points={}", userId, points);
            throw new BusinessException("更新用户积分失败");
        }

        Integer balanceAfter = balanceBefore + points;
        creditMapper.insertTransaction(
                userId,
                TRANSACTION_TYPE_SYSTEM,
                points,
                balanceBefore,
                balanceAfter,
                description,
                relatedOrderId
        );

        log.info("用户 {} 获得 {} 积分，原因：{}，余额：{} -> {}",
                userId, points, description, balanceBefore, balanceAfter);
        publishCreditEvent(userId, points, balanceBefore, balanceAfter, description);
        return balanceAfter;
    }

    private Integer getOrCreateBalanceForUpdate(Long userId) {
        Integer balance = creditMapper.getBalanceByUserIdForUpdate(userId);
        if (balance != null) {
            return balance;
        }

        Integer initialBalance = resolveInitialBalance(userId);
        creditMapper.initUserCredit(userId, initialBalance);
        balance = creditMapper.getBalanceByUserIdForUpdate(userId);
        if (balance == null) {
            log.error("初始化用户积分记录失败: userId={}", userId);
            throw new BusinessException("初始化用户积分记录失败");
        }
        return balance;
    }

    private Integer resolveInitialBalance(Long userId) {
        var user = userMapper.findById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return user.getCurrentPoints() != null ? user.getCurrentPoints() : 0;
    }

    private void publishCreditEvent(Long userId,
                                    Integer pointsChange,
                                    Integer balanceBefore,
                                    Integer balanceAfter,
                                    String description) {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("type", "CREDIT_UPDATED");
        eventData.put("userId", userId);
        eventData.put("pointsChange", pointsChange);
        eventData.put("balanceBefore", balanceBefore);
        eventData.put("balanceAfter", balanceAfter);
        eventData.put("description", description);
        sseEmitterService.sendToUser(userId, "credit", eventData);
    }
}

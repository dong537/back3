package com.example.demo.service;

import com.example.demo.entity.Achievement;
import com.example.demo.entity.UserAchievement;
import com.example.demo.mapper.AchievementMapper;
import com.example.demo.mapper.CalculationRecordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 成就服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AchievementService {

    private final AchievementMapper achievementMapper;
    private final CreditService creditService;
    private final SseEmitterService sseEmitterService;
    private final CalculationRecordMapper calculationRecordMapper;
    private final AchievementCacheService achievementCacheService;
    
    /**
     * 获取所有成就列表
     */
    public List<Achievement> getAllAchievements() {
        return achievementMapper.findAllActive();
    }
    
    /**
     * 获取用户已解锁的成就
     */
    public List<AchievementMapper.UserAchievementWithInfo> getUserAchievements(Long userId) {
        return achievementMapper.findUserAchievements(userId);
    }
    
    /**
     * 检查并解锁成就
     */
    @Transactional
    public void checkAndUnlockAchievement(Long userId, String achievementCode) {
        if (userId == null) {
            return;
        }
        if (achievementCode == null || achievementCode.isEmpty()) {
            return;
        }
        
        // 查询成就信息
        Achievement achievement = achievementMapper.findByCode(achievementCode);
        if (achievement == null || achievement.getIsActive() == null || achievement.getIsActive() == 0) {
            return; // 成就不存在或已禁用
        }

        // 解锁成就（数据库唯一索引 uk_user_achievement(user_id, achievement_code) 保障幂等）
        UserAchievement userAchievement = UserAchievement.builder()
                .userId(userId)
                .achievementId(achievement.getId())
                .achievementCode(achievementCode)
                .unlockedTime(LocalDateTime.now())
                .pointsEarned(achievement.getPointsReward())
                .build();

        try {
            achievementMapper.insertUserAchievement(userAchievement);
        } catch (DuplicateKeyException e) {
            // 并发/重复触发：已解锁则忽略
            return;
        }

        // 发放积分奖励
        creditService.addPoints(userId, achievement.getPointsReward(),
                "解锁成就：" + achievement.getAchievementName(), null);

        log.info("用户 {} 解锁成就：{}，获得 {} 积分", userId, achievement.getAchievementName(),
                achievement.getPointsReward());

        // 通过 SSE 通知前端有新的成就解锁
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("type", "ACHIEVEMENT_UNLOCKED");
        eventData.put("userId", userId);
        eventData.put("achievementCode", achievementCode);
        eventData.put("achievementName", achievement.getAchievementName());
        eventData.put("pointsReward", achievement.getPointsReward());
        eventData.put("unlockedTime", userAchievement.getUnlockedTime());

        sseEmitterService.sendToUser(userId, "achievement", eventData);
    }
    
    /**
     * 检查占卜次数相关的成就
     * 在用户完成占卜后调用，检查是否达到10次、50次等成就
     * 使用缓存优化性能，批量检查成就
     */
    @Transactional
    public void checkDivinationAchievements(Long userId) {
        if (userId == null) {
            log.warn("checkDivinationAchievements: userId为null，跳过检查");
            return;
        }
        
        try {
            // 先更新缓存（增加1次）
            achievementCacheService.incrementDivinationCount(userId);
            
            // 从缓存获取占卜次数（如果缓存不存在，会查询数据库）
            int actualCount = calculationRecordMapper.countByUserId(userId);
            int divinationCount = achievementCacheService.getDivinationCount(userId, actualCount);
            
            log.info("用户 {} 当前占卜次数: {} (缓存: {})", userId, actualCount, divinationCount);
            
            // 批量检查成就（按顺序检查，避免重复解锁）
            // 使用数组存储需要检查的成就，提高可维护性
            int[] milestones = {1, 10, 50, 100};
            String[] achievementCodes = {"first_divination", "divination_10", "divination_50", "divination_100"};
            
            for (int i = 0; i < milestones.length; i++) {
                if (divinationCount >= milestones[i]) {
                    log.info("检查成就: {} (占卜次数: {})", achievementCodes[i], divinationCount);
                    checkAndUnlockAchievement(userId, achievementCodes[i]);
                }
            }
        } catch (Exception e) {
            log.error("检查占卜成就失败, userId={}", userId, e);
            // 清除缓存，确保下次查询最新数据
            achievementCacheService.clearCache(userId);
            // 不抛出异常，避免影响占卜流程
        }
    }
    
    /**
     * 获取用户成就统计
     */
    public Map<String, Object> getUserAchievementStats(Long userId) {
        List<AchievementMapper.UserAchievementWithInfo> userAchievements = 
                achievementMapper.findUserAchievements(userId);
        List<Achievement> allAchievements = achievementMapper.findAllActive();
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("unlocked", userAchievements.size());
        stats.put("total", allAchievements.size());
        stats.put("progress", allAchievements.size() > 0 ? 
                (userAchievements.size() * 100 / allAchievements.size()) : 0);
        
        return stats;
    }
}

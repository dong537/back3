package com.example.demo.service;

import com.example.demo.entity.DailyCheckin;
import com.example.demo.exception.BusinessException;
import com.example.demo.mapper.DailyCheckinMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 打卡签到服务
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CheckinService {
    
    private final DailyCheckinMapper checkinMapper;
    private final CreditService creditService;
    private final AchievementService achievementService;
    
    // 连续打卡奖励规则：天数 -> 积分
    private static final Map<Integer, Integer> STREAK_REWARDS = new HashMap<>();
    static {
        STREAK_REWARDS.put(1, 10);   // 第1-2天：10积分
        STREAK_REWARDS.put(2, 10);
        STREAK_REWARDS.put(3, 20);   // 第3-6天：20积分
        STREAK_REWARDS.put(4, 20);
        STREAK_REWARDS.put(5, 20);
        STREAK_REWARDS.put(6, 20);
        STREAK_REWARDS.put(7, 30);   // 第7天及以上：30积分
    }
    
    // 特殊奖励：连续打卡3天和7天的额外奖励
    private static final int STREAK_3_BONUS = 20;  // 连续3天额外奖励
    private static final int STREAK_7_BONUS = 50;  // 连续7天额外奖励
    
    /**
     * 执行打卡
     */
    @Transactional
    public Map<String, Object> doCheckin(Long userId) {
        LocalDate today = LocalDate.now();
        
        // 检查今天是否已打卡
        int count = checkinMapper.countByUserIdAndDate(userId, today);
        if (count > 0) {
            throw new BusinessException("今天已经打卡过了，请明天再来");
        }
        
        // 计算连续打卡天数
        int streakDays = calculateStreakDays(userId, today);
        
        // 计算奖励积分
        int baseReward = getBaseReward(streakDays);
        int bonusReward = getBonusReward(streakDays);
        int totalReward = baseReward + bonusReward;
        
        // 保存打卡记录
        DailyCheckin checkin = DailyCheckin.builder()
                .userId(userId)
                .checkinDate(today)
                .checkinTime(LocalDateTime.now())
                .streakDays(streakDays)
                .pointsEarned(totalReward)
                .build();
        
        checkinMapper.insert(checkin);
        
        // 添加积分
        String description = String.format("连续打卡%d天", streakDays);
        if (bonusReward > 0) {
            description += String.format("（含额外奖励%d积分）", bonusReward);
        }
        creditService.addPoints(userId, totalReward, description, null);

        // 尝试解锁与签到相关的成就（幂等处理在 AchievementService 内部完成）
        try {
            // 连续签到7天成就
            if (streakDays >= 7) {
                achievementService.checkAndUnlockAchievement(userId, "checkin_week");
            }
            // 连续签到30天成就
            if (streakDays >= 30) {
                achievementService.checkAndUnlockAchievement(userId, "checkin_month");
            }
        } catch (Exception e) {
            // 成就解锁失败不影响签到和积分发放，只记录日志
            log.error("检查并解锁签到成就失败, userId={}, streakDays={}", userId, streakDays, e);
        }
        
        // 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("streakDays", streakDays);
        result.put("baseReward", baseReward);
        result.put("bonusReward", bonusReward);
        result.put("totalReward", totalReward);
        result.put("checkinDate", today.toString());
        result.put("message", getRewardMessage(streakDays, totalReward));
        
        log.info("用户 {} 打卡成功，连续 {} 天，获得 {} 积分", userId, streakDays, totalReward);
        
        return result;
    }
    
    /**
     * 计算连续打卡天数
     */
    private int calculateStreakDays(Long userId, LocalDate today) {
        LocalDate lastCheckinDate = checkinMapper.findLastCheckinDate(userId);
        
        if (lastCheckinDate == null) {
            // 首次打卡
            return 1;
        }
        
        LocalDate yesterday = today.minusDays(1);
        
        if (lastCheckinDate.equals(yesterday)) {
            // 连续打卡：昨天打卡了，今天继续
            Integer currentStreak = checkinMapper.findCurrentStreak(userId);
            return (currentStreak != null ? currentStreak : 0) + 1;
        } else if (lastCheckinDate.equals(today)) {
            // 今天已打卡（理论上不应该到这里，但保险起见）
            return checkinMapper.findCurrentStreak(userId);
        } else {
            // 中断了，重新开始
            return 1;
        }
    }
    
    /**
     * 获取基础奖励（根据连续天数）
     */
    private int getBaseReward(int streakDays) {
        if (streakDays >= 7) {
            return STREAK_REWARDS.get(7);
        } else if (streakDays >= 3) {
            return STREAK_REWARDS.get(3);
        } else {
            return STREAK_REWARDS.get(Math.min(streakDays, 2));
        }
    }
    
    /**
     * 获取额外奖励（连续3天和7天的特殊奖励）
     */
    private int getBonusReward(int streakDays) {
        if (streakDays == 3) {
            return STREAK_3_BONUS;
        } else if (streakDays == 7) {
            return STREAK_7_BONUS;
        }
        return 0;
    }
    
    /**
     * 获取奖励消息
     */
    private String getRewardMessage(int streakDays, int totalReward) {
        if (streakDays == 3) {
            return String.format("连续打卡3天！获得%d积分（含额外奖励）", totalReward);
        } else if (streakDays == 7) {
            return String.format("连续打卡7天！获得%d积分（含额外奖励）", totalReward);
        } else {
            return String.format("打卡成功！连续%d天，获得%d积分", streakDays, totalReward);
        }
    }
    
    /**
     * 获取今日打卡状态
     */
    public Map<String, Object> getTodayStatus(Long userId) {
        LocalDate today = LocalDate.now();
        DailyCheckin todayCheckin = checkinMapper.findByUserIdAndDate(userId, today);
        
        Map<String, Object> result = new HashMap<>();
        result.put("hasCheckedIn", todayCheckin != null);
        result.put("checkinDate", today.toString());
        
        if (todayCheckin != null) {
            result.put("streakDays", todayCheckin.getStreakDays());
            result.put("pointsEarned", todayCheckin.getPointsEarned());
        } else {
            // 获取当前连续天数（用于显示）
            Integer currentStreak = checkinMapper.findCurrentStreak(userId);
            result.put("streakDays", currentStreak != null ? currentStreak : 0);
        }
        
        return result;
    }
    
    /**
     * 获取本周打卡记录（用于显示进度）
     */
    public Map<String, Object> getWeeklyProgress(Long userId) {
        LocalDate today = LocalDate.now();
        // 获取本周一
        LocalDate monday = today.minusDays(today.getDayOfWeek().getValue() - 1);
        LocalDate sunday = monday.plusDays(6);
        
        List<DailyCheckin> checkins = checkinMapper.findWeeklyCheckins(userId, monday, sunday);
        
        Map<String, Object> result = new HashMap<>();
        result.put("weekStart", monday.toString());
        result.put("weekEnd", sunday.toString());
        result.put("checkins", checkins);
        
        // 构建7天的打卡状态
        boolean[] weekStatus = new boolean[7];
        for (DailyCheckin checkin : checkins) {
            int dayIndex = checkin.getCheckinDate().getDayOfWeek().getValue() - 1;
            weekStatus[dayIndex] = true;
        }
        result.put("weekStatus", weekStatus);
        
        return result;
    }
    
    /**
     * 获取连续打卡信息
     */
    public Map<String, Object> getStreakInfo(Long userId) {
        LocalDate lastCheckinDate = checkinMapper.findLastCheckinDate(userId);
        LocalDate today = LocalDate.now();
        
        // 获取当前连续天数
        Integer currentStreak = null;
        if (lastCheckinDate != null) {
            // 如果今天已打卡，使用今天的连续天数
            if (lastCheckinDate.equals(today)) {
                DailyCheckin todayCheckin = checkinMapper.findByUserIdAndDate(userId, today);
                if (todayCheckin != null) {
                    currentStreak = todayCheckin.getStreakDays();
                }
            } else {
                // 如果今天未打卡，使用最近一次打卡的连续天数（昨天的）
                currentStreak = checkinMapper.findCurrentStreak(userId);
            }
        }
        
        // 如果今天未打卡，需要判断是否中断
        int displayStreak = currentStreak != null ? currentStreak : 0;
        if (lastCheckinDate != null && !lastCheckinDate.equals(today)) {
            LocalDate yesterday = today.minusDays(1);
            // 如果昨天没打卡，连续天数应该重置为0（用于显示"再打卡X天"）
            if (!lastCheckinDate.equals(yesterday)) {
                displayStreak = 0;
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("currentStreak", displayStreak);
        result.put("lastCheckinDate", lastCheckinDate);
        result.put("canCheckinToday", lastCheckinDate == null || !lastCheckinDate.equals(today));
        
        // 计算距离下一个奖励还需要多少天（基于当前连续天数+1，因为下次打卡会增加1天）
        int nextRewardDay = getNextRewardDay(displayStreak);
        result.put("nextRewardDay", nextRewardDay);
        result.put("daysToNextReward", nextRewardDay > 0 ? Math.max(0, nextRewardDay - displayStreak) : 0);
        
        return result;
    }
    
    /**
     * 获取下一个奖励天数
     */
    private int getNextRewardDay(int currentStreak) {
        if (currentStreak < 3) {
            return 3;
        } else if (currentStreak < 7) {
            return 7;
        } else {
            // 7天之后，每7天一个周期
            return ((currentStreak / 7) + 1) * 7;
        }
    }
}

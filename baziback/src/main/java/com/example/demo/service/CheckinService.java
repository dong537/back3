package com.example.demo.service;

import com.example.demo.entity.DailyCheckin;
import com.example.demo.exception.BusinessException;
import com.example.demo.mapper.DailyCheckinMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 签到服务
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CheckinService {

    private static final Map<Integer, Integer> STREAK_REWARDS = new HashMap<>();
    private static final int STREAK_3_BONUS = 20;
    private static final int STREAK_7_BONUS = 50;

    static {
        STREAK_REWARDS.put(1, 10);
        STREAK_REWARDS.put(2, 10);
        STREAK_REWARDS.put(3, 20);
        STREAK_REWARDS.put(4, 20);
        STREAK_REWARDS.put(5, 20);
        STREAK_REWARDS.put(6, 20);
        STREAK_REWARDS.put(7, 30);
    }

    private final DailyCheckinMapper checkinMapper;
    private final CreditService creditService;
    private final AchievementService achievementService;

    @Transactional
    public Map<String, Object> doCheckin(Long userId) {
        LocalDate today = LocalDate.now();
        int streakDays = calculateStreakDays(userId, today);
        int baseReward = getBaseReward(streakDays);
        int bonusReward = getBonusReward(streakDays);
        int totalReward = baseReward + bonusReward;

        DailyCheckin checkin = DailyCheckin.builder()
                .userId(userId)
                .checkinDate(today)
                .checkinTime(LocalDateTime.now())
                .streakDays(streakDays)
                .pointsEarned(totalReward)
                .build();

        try {
            checkinMapper.insert(checkin);
        } catch (DuplicateKeyException e) {
            throw new BusinessException("今天已经签到过了，请明天再来");
        }

        String description = buildRewardDescription(streakDays, bonusReward);
        Integer currentBalance = creditService.addPointsAndGetBalance(userId, totalReward, description, null);

        try {
            achievementService.checkPointsAchievements(userId);
            if (streakDays >= 7) {
                achievementService.checkAndUnlockAchievement(userId, "checkin_week");
            }
            if (streakDays >= 30) {
                achievementService.checkAndUnlockAchievement(userId, "checkin_month");
            }
        } catch (Exception e) {
            log.error("检查签到成就失败: userId={}, streakDays={}", userId, streakDays, e);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("streakDays", streakDays);
        result.put("baseReward", baseReward);
        result.put("bonusReward", bonusReward);
        result.put("totalReward", totalReward);
        result.put("currentBalance", currentBalance);
        result.put("checkinDate", today.toString());
        result.put("message", getRewardMessage(streakDays, totalReward));

        log.info("用户 {} 签到成功，连续 {} 天，获得 {} 积分", userId, streakDays, totalReward);
        return result;
    }

    public Map<String, Object> getTodayStatus(Long userId) {
        LocalDate today = LocalDate.now();
        DailyCheckin todayCheckin = checkinMapper.findByUserIdAndDate(userId, today);
        LocalDate lastCheckinDate = todayCheckin != null ? today : checkinMapper.findLastCheckinDate(userId);
        int displayStreak = resolveDisplayStreak(userId, today, todayCheckin, lastCheckinDate);
        return buildTodayStatus(today, todayCheckin, displayStreak);
    }

    public Map<String, Object> getWeeklyProgress(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate monday = today.minusDays(today.getDayOfWeek().getValue() - 1L);
        LocalDate sunday = monday.plusDays(6);
        List<DailyCheckin> checkins = checkinMapper.findWeeklyCheckins(userId, monday, sunday);
        return buildWeeklyProgress(monday, sunday, checkins);
    }

    public Map<String, Object> getStreakInfo(Long userId) {
        LocalDate today = LocalDate.now();
        DailyCheckin todayCheckin = checkinMapper.findByUserIdAndDate(userId, today);
        LocalDate lastCheckinDate = todayCheckin != null ? today : checkinMapper.findLastCheckinDate(userId);
        int displayStreak = resolveDisplayStreak(userId, today, todayCheckin, lastCheckinDate);
        return buildStreakInfo(today, lastCheckinDate, displayStreak);
    }

    public Map<String, Object> getOverview(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate monday = today.minusDays(today.getDayOfWeek().getValue() - 1L);
        LocalDate sunday = monday.plusDays(6);

        DailyCheckin todayCheckin = checkinMapper.findByUserIdAndDate(userId, today);
        LocalDate lastCheckinDate = todayCheckin != null ? today : checkinMapper.findLastCheckinDate(userId);
        int displayStreak = resolveDisplayStreak(userId, today, todayCheckin, lastCheckinDate);
        List<DailyCheckin> weeklyCheckins = checkinMapper.findWeeklyCheckins(userId, monday, sunday);
        Integer balance = creditService.getCurrentPoints(userId);

        Map<String, Object> result = new HashMap<>();
        result.put("todayStatus", buildTodayStatus(today, todayCheckin, displayStreak));
        result.put("weeklyProgress", buildWeeklyProgress(monday, sunday, weeklyCheckins));
        result.put("streakInfo", buildStreakInfo(today, lastCheckinDate, displayStreak));
        result.put("balance", balance);
        return result;
    }

    private int calculateStreakDays(Long userId, LocalDate today) {
        LocalDate lastCheckinDate = checkinMapper.findLastCheckinDate(userId);
        if (lastCheckinDate == null) {
            return 1;
        }

        if (lastCheckinDate.equals(today.minusDays(1))) {
            Integer currentStreak = checkinMapper.findCurrentStreak(userId);
            return (currentStreak != null ? currentStreak : 0) + 1;
        }
        if (lastCheckinDate.equals(today)) {
            Integer currentStreak = checkinMapper.findCurrentStreak(userId);
            return currentStreak != null ? currentStreak : 1;
        }
        return 1;
    }

    private int resolveDisplayStreak(Long userId,
                                     LocalDate today,
                                     DailyCheckin todayCheckin,
                                     LocalDate lastCheckinDate) {
        if (todayCheckin != null) {
            return todayCheckin.getStreakDays() != null ? todayCheckin.getStreakDays() : 0;
        }

        if (lastCheckinDate != null && lastCheckinDate.equals(today.minusDays(1))) {
            Integer currentStreak = checkinMapper.findCurrentStreak(userId);
            return currentStreak != null ? currentStreak : 0;
        }

        return 0;
    }

    private Map<String, Object> buildTodayStatus(LocalDate today, DailyCheckin todayCheckin, int displayStreak) {
        Map<String, Object> result = new HashMap<>();
        result.put("hasCheckedIn", todayCheckin != null);
        result.put("checkinDate", today.toString());
        result.put("streakDays", todayCheckin != null ? todayCheckin.getStreakDays() : displayStreak);
        result.put("pointsEarned", todayCheckin != null ? todayCheckin.getPointsEarned() : 0);
        return result;
    }

    private Map<String, Object> buildWeeklyProgress(LocalDate monday,
                                                    LocalDate sunday,
                                                    List<DailyCheckin> checkins) {
        Map<String, Object> result = new HashMap<>();
        result.put("weekStart", monday.toString());
        result.put("weekEnd", sunday.toString());
        result.put("checkins", checkins);

        boolean[] weekStatus = new boolean[7];
        for (DailyCheckin checkin : checkins) {
            int dayIndex = checkin.getCheckinDate().getDayOfWeek().getValue() - 1;
            weekStatus[dayIndex] = true;
        }
        result.put("weekStatus", weekStatus);
        return result;
    }

    private Map<String, Object> buildStreakInfo(LocalDate today, LocalDate lastCheckinDate, int displayStreak) {
        Map<String, Object> result = new HashMap<>();
        result.put("currentStreak", displayStreak);
        result.put("lastCheckinDate", lastCheckinDate);
        result.put("canCheckinToday", lastCheckinDate == null || !lastCheckinDate.equals(today));

        int nextRewardDay = getNextRewardDay(displayStreak);
        result.put("nextRewardDay", nextRewardDay);
        result.put("daysToNextReward", nextRewardDay > 0 ? Math.max(0, nextRewardDay - displayStreak) : 0);
        return result;
    }

    private int getBaseReward(int streakDays) {
        if (streakDays >= 7) {
            return STREAK_REWARDS.get(7);
        }
        if (streakDays >= 3) {
            return STREAK_REWARDS.get(3);
        }
        return STREAK_REWARDS.get(Math.min(streakDays, 2));
    }

    private int getBonusReward(int streakDays) {
        if (streakDays == 3) {
            return STREAK_3_BONUS;
        }
        if (streakDays == 7) {
            return STREAK_7_BONUS;
        }
        return 0;
    }

    private String buildRewardDescription(int streakDays, int bonusReward) {
        String description = String.format("连续签到%d天", streakDays);
        if (bonusReward > 0) {
            description += String.format("（含额外奖励%d积分）", bonusReward);
        }
        return description;
    }

    private String getRewardMessage(int streakDays, int totalReward) {
        if (streakDays == 3) {
            return String.format("连续签到3天，获得%d积分（含额外奖励）", totalReward);
        }
        if (streakDays == 7) {
            return String.format("连续签到7天，获得%d积分（含额外奖励）", totalReward);
        }
        return String.format("签到成功，连续%d天，获得%d积分", streakDays, totalReward);
    }

    private int getNextRewardDay(int currentStreak) {
        if (currentStreak < 3) {
            return 3;
        }
        if (currentStreak < 7) {
            return 7;
        }
        return ((currentStreak / 7) + 1) * 7;
    }
}

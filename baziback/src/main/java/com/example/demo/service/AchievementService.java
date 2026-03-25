package com.example.demo.service;

import com.example.demo.entity.Achievement;
import com.example.demo.entity.UserAchievement;
import com.example.demo.mapper.AchievementMapper;
import com.example.demo.mapper.CalculationRecordMapper;
import com.example.demo.mapper.CreditMapper;
import com.example.demo.mapper.DailyCheckinMapper;
import com.example.demo.mapper.FavoriteMapper;
import com.example.demo.mapper.ReferralMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AchievementService {

    private static final Map<String, List<String>> ACHIEVEMENT_CODE_ALIASES = Map.ofEntries(
            Map.entry("first_divination", List.of("first_divination")),
            Map.entry("divination_10", List.of("divination_10", "divination_master")),
            Map.entry("divination_master", List.of("divination_master", "divination_10")),
            Map.entry("divination_50", List.of("divination_50", "divination_expert")),
            Map.entry("divination_expert", List.of("divination_expert", "divination_50")),
            Map.entry("divination_100", List.of("divination_100")),
            Map.entry("collector", List.of("collector")),
            Map.entry("collector_master", List.of("collector_master")),
            Map.entry("inviter", List.of("inviter")),
            Map.entry("inviter_master", List.of("inviter_master")),
            Map.entry("checkin_week", List.of("checkin_week")),
            Map.entry("checkin_month", List.of("checkin_month")),
            Map.entry("points_rich", List.of("points_rich")),
            Map.entry("points_millionaire", List.of("points_millionaire"))
    );

    private final AchievementMapper achievementMapper;
    private final CreditService creditService;
    private final SseEmitterService sseEmitterService;
    private final CalculationRecordMapper calculationRecordMapper;
    private final AchievementCacheService achievementCacheService;
    private final DailyCheckinMapper dailyCheckinMapper;
    private final FavoriteMapper favoriteMapper;
    private final ReferralMapper referralMapper;
    private final CreditMapper creditMapper;

    public List<Achievement> getAllAchievements() {
        return achievementMapper.findAllActive();
    }

    public List<AchievementMapper.UserAchievementWithInfo> getUserAchievements(Long userId) {
        return achievementMapper.findUserAchievements(userId);
    }

    @Transactional
    public void reconcileAchievements(Long userId) {
        if (userId == null) {
            return;
        }

        int divinationCount = calculationRecordMapper.countByUserId(userId);
        unlockByMilestones(
                userId,
                divinationCount,
                new int[]{1, 10, 50, 100},
                new String[]{"first_divination", "divination_10", "divination_50", "divination_100"}
        );
        checkFavoriteAchievements(userId);
        checkInviteAchievements(userId);
        checkCheckinAchievements(userId);
        checkPointsAchievements(userId);
    }

    @Transactional
    public void checkAndUnlockAchievement(Long userId, String achievementCode) {
        if (userId == null || achievementCode == null || achievementCode.isEmpty()) {
            return;
        }

        Achievement achievement = resolveAchievement(achievementCode);
        if (achievement == null || achievement.getIsActive() == null || achievement.getIsActive() == 0) {
            log.debug("Skip unlocking missing achievement, requestedCode={}", achievementCode);
            return;
        }

        String resolvedAchievementCode = achievement.getAchievementCode();
        if (!achievementCode.equals(resolvedAchievementCode)) {
            log.info("Achievement code alias matched, requestedCode={}, resolvedCode={}",
                    achievementCode, resolvedAchievementCode);
        }

        UserAchievement userAchievement = UserAchievement.builder()
                .userId(userId)
                .achievementId(achievement.getId())
                .achievementCode(resolvedAchievementCode)
                .unlockedTime(LocalDateTime.now())
                .pointsEarned(achievement.getPointsReward())
                .build();

        try {
            achievementMapper.insertUserAchievement(userAchievement);
        } catch (DuplicateKeyException e) {
            return;
        }

        Integer pointsReward = achievement.getPointsReward();
        if (pointsReward != null && pointsReward > 0) {
            creditService.addPoints(
                    userId,
                    pointsReward,
                    "解锁成就：" + achievement.getAchievementName(),
                    null
            );
            if (!"points".equalsIgnoreCase(achievement.getAchievementType())) {
                checkPointsAchievements(userId);
            }
        }

        log.info("用户 {} 解锁成就：{}，获得 {} 积分",
                userId, achievement.getAchievementName(), achievement.getPointsReward());

        Map<String, Object> eventData = new HashMap<>();
        eventData.put("type", "ACHIEVEMENT_UNLOCKED");
        eventData.put("userId", userId);
        eventData.put("achievementCode", resolvedAchievementCode);
        eventData.put("achievementName", achievement.getAchievementName());
        eventData.put("achievementDescription", achievement.getAchievementDescription());
        eventData.put("achievementType", achievement.getAchievementType());
        eventData.put("pointsReward", achievement.getPointsReward());
        eventData.put("unlockedTime", userAchievement.getUnlockedTime());
        sseEmitterService.sendToUser(userId, "achievement", eventData);
    }

    @Transactional
    public void checkDivinationAchievements(Long userId) {
        if (userId == null) {
            log.warn("checkDivinationAchievements skipped because userId is null");
            return;
        }

        try {
            achievementCacheService.incrementDivinationCount(userId);

            int actualCount = calculationRecordMapper.countByUserId(userId);
            int divinationCount = achievementCacheService.getDivinationCount(userId, actualCount);

            log.info("User {} divination count: {} (cache={})", userId, actualCount, divinationCount);

            int[] milestones = {1, 10, 50, 100};
            String[] achievementCodes = {"first_divination", "divination_10", "divination_50", "divination_100"};
            unlockByMilestones(userId, divinationCount, milestones, achievementCodes);
        } catch (Exception e) {
            log.error("检查占卜成就失败, userId={}", userId, e);
            achievementCacheService.clearCache(userId);
        }
    }

    @Transactional
    public void checkFavoriteAchievements(Long userId) {
        if (userId == null) {
            return;
        }

        Integer favoriteCount = favoriteMapper.countAllFavoritesByUserId(userId);
        int totalFavorites = favoriteCount != null ? favoriteCount : 0;
        log.info("User {} favorite count: {}", userId, totalFavorites);

        int[] milestones = {5, 20};
        String[] achievementCodes = {"collector", "collector_master"};
        unlockByMilestones(userId, totalFavorites, milestones, achievementCodes);
    }

    @Transactional
    public void checkInviteAchievements(Long userId) {
        if (userId == null) {
            return;
        }

        Integer inviteCount = referralMapper.countRegisteredInvites(userId);
        int registeredInvites = inviteCount != null ? inviteCount : 0;
        log.info("User {} registered invite count: {}", userId, registeredInvites);

        int[] milestones = {3, 10};
        String[] achievementCodes = {"inviter", "inviter_master"};
        unlockByMilestones(userId, registeredInvites, milestones, achievementCodes);
    }

    @Transactional
    public void checkCheckinAchievements(Long userId) {
        if (userId == null) {
            return;
        }

        Integer maxStreak = dailyCheckinMapper.findMaxStreak(userId);
        int achievedStreak = maxStreak != null ? maxStreak : 0;
        log.info("User {} max check-in streak: {}", userId, achievedStreak);

        int[] milestones = {7, 30};
        String[] achievementCodes = {"checkin_week", "checkin_month"};
        unlockByMilestones(userId, achievedStreak, milestones, achievementCodes);
    }

    @Transactional
    public void checkPointsAchievements(Long userId) {
        if (userId == null) {
            return;
        }

        Integer earnedPoints = creditMapper.sumEarnedPoints(userId);
        int totalEarnedPoints = earnedPoints != null ? earnedPoints : 0;
        log.info("User {} earned points total: {}", userId, totalEarnedPoints);

        int[] milestones = {500, 2000};
        String[] achievementCodes = {"points_rich", "points_millionaire"};
        unlockByMilestones(userId, totalEarnedPoints, milestones, achievementCodes);
    }

    public Map<String, Object> getUserAchievementStats(Long userId) {
        List<AchievementMapper.UserAchievementWithInfo> userAchievements =
                achievementMapper.findUserAchievements(userId);
        List<Achievement> allAchievements = achievementMapper.findAllActive();

        Map<String, Object> stats = new HashMap<>();
        stats.put("unlocked", userAchievements.size());
        stats.put("total", allAchievements.size());
        stats.put("progress", allAchievements.isEmpty()
                ? 0
                : userAchievements.size() * 100 / allAchievements.size());
        return stats;
    }

    private void unlockByMilestones(Long userId,
                                    int currentValue,
                                    int[] milestones,
                                    String[] achievementCodes) {
        for (int index = 0; index < milestones.length; index++) {
            if (currentValue >= milestones[index]) {
                checkAndUnlockAchievement(userId, achievementCodes[index]);
            }
        }
    }

    private Achievement resolveAchievement(String achievementCode) {
        List<String> candidateCodes = ACHIEVEMENT_CODE_ALIASES.getOrDefault(
                achievementCode,
                List.of(achievementCode)
        );

        for (String candidateCode : candidateCodes) {
            Achievement achievement = achievementMapper.findByCode(candidateCode);
            if (achievement != null) {
                return achievement;
            }
        }
        return null;
    }
}

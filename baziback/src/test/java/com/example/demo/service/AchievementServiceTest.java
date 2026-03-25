package com.example.demo.service;

import com.example.demo.entity.Achievement;
import com.example.demo.entity.UserAchievement;
import com.example.demo.mapper.AchievementMapper;
import com.example.demo.mapper.CalculationRecordMapper;
import com.example.demo.mapper.CreditMapper;
import com.example.demo.mapper.DailyCheckinMapper;
import com.example.demo.mapper.FavoriteMapper;
import com.example.demo.mapper.ReferralMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AchievementServiceTest {

    @Mock
    private AchievementMapper achievementMapper;

    @Mock
    private CreditService creditService;

    @Mock
    private SseEmitterService sseEmitterService;

    @Mock
    private CalculationRecordMapper calculationRecordMapper;

    @Mock
    private AchievementCacheService achievementCacheService;

    @Mock
    private DailyCheckinMapper dailyCheckinMapper;

    @Mock
    private FavoriteMapper favoriteMapper;

    @Mock
    private ReferralMapper referralMapper;

    @Mock
    private CreditMapper creditMapper;

    @InjectMocks
    private AchievementService achievementService;

    @Test
    void shouldFallbackToLegacyDivinationAchievementCode() {
        Long userId = 5L;
        Achievement legacyAchievement = Achievement.builder()
                .id(2L)
                .achievementCode("divination_master")
                .achievementName("占卜大师")
                .achievementDescription("完成10次占卜")
                .achievementType("divination")
                .pointsReward(100)
                .isActive(1)
                .build();

        when(achievementMapper.findByCode("divination_10")).thenReturn(null);
        when(achievementMapper.findByCode("divination_master")).thenReturn(legacyAchievement);
        when(creditMapper.sumEarnedPoints(userId)).thenReturn(100);

        achievementService.checkAndUnlockAchievement(userId, "divination_10");

        ArgumentCaptor<UserAchievement> achievementCaptor = ArgumentCaptor.forClass(UserAchievement.class);
        verify(achievementMapper).insertUserAchievement(achievementCaptor.capture());
        assertEquals("divination_master", achievementCaptor.getValue().getAchievementCode());
        assertEquals(2L, achievementCaptor.getValue().getAchievementId());

        verify(creditService).addPoints(eq(userId), eq(100), eq("解锁成就：占卜大师"), eq(null));

        ArgumentCaptor<Map<String, Object>> eventCaptor = ArgumentCaptor.forClass(Map.class);
        verify(sseEmitterService).sendToUser(eq(userId), eq("achievement"), eventCaptor.capture());
        assertEquals("divination_master", eventCaptor.getValue().get("achievementCode"));
        assertEquals("ACHIEVEMENT_UNLOCKED", eventCaptor.getValue().get("type"));
    }

    @Test
    void shouldUnlockCollectorWhenFavoriteMilestoneReached() {
        Long userId = 8L;
        Achievement collector = Achievement.builder()
                .id(4L)
                .achievementCode("collector")
                .achievementName("收藏家")
                .achievementDescription("收藏5个结果")
                .achievementType("favorite")
                .pointsReward(30)
                .isActive(1)
                .build();

        when(favoriteMapper.countAllFavoritesByUserId(userId)).thenReturn(5);
        when(achievementMapper.findByCode("collector")).thenReturn(collector);
        when(creditMapper.sumEarnedPoints(userId)).thenReturn(30);

        achievementService.checkFavoriteAchievements(userId);

        verify(achievementMapper).insertUserAchievement(any(UserAchievement.class));
        verify(creditService).addPoints(eq(userId), eq(30), eq("解锁成就：收藏家"), eq(null));
    }

    @Test
    void shouldUnlockPointsAchievementWhenEarnedPointsMilestoneReached() {
        Long userId = 9L;
        Achievement rich = Achievement.builder()
                .id(10L)
                .achievementCode("points_rich")
                .achievementName("积分富翁")
                .achievementDescription("累计获得500积分")
                .achievementType("points")
                .pointsReward(100)
                .isActive(1)
                .build();

        when(creditMapper.sumEarnedPoints(userId)).thenReturn(500, 600);
        when(achievementMapper.findByCode("points_rich")).thenReturn(rich);

        achievementService.checkPointsAchievements(userId);

        verify(achievementMapper).insertUserAchievement(any(UserAchievement.class));
        verify(creditService).addPoints(eq(userId), eq(100), eq("解锁成就：积分富翁"), eq(null));
    }
}

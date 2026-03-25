package com.example.demo.service;

import com.example.demo.entity.InviteRecord;
import com.example.demo.entity.UserReferral;
import com.example.demo.exception.BusinessException;
import com.example.demo.mapper.ReferralMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReferralService {

    private final ReferralMapper referralMapper;
    private final CreditService creditService;
    private final AchievementService achievementService;

    public String getOrCreateReferralCode(Long userId) {
        UserReferral referral = referralMapper.findByUserId(userId);
        if (referral != null) {
            return referral.getReferralCode();
        }

        String referralCode = generateReferralCode();
        while (referralMapper.findByReferralCode(referralCode) != null) {
            referralCode = generateReferralCode();
        }

        UserReferral newReferral = UserReferral.builder()
                .userId(userId)
                .referralCode(referralCode)
                .referredBy(null)
                .referralCodeUsed(0)
                .build();

        referralMapper.insert(newReferral);
        return referralCode;
    }

    @Transactional
    public void useReferralCode(Long userId, String referralCode) {
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }
        if (referralCode == null || referralCode.isEmpty()) {
            throw new BusinessException("推荐码不能为空");
        }

        UserReferral referral = referralMapper.findByReferralCode(referralCode);
        if (referral == null) {
            throw new BusinessException("推荐码不存在");
        }

        if (referral.getUserId() != null && referral.getUserId().equals(userId)) {
            throw new BusinessException("不能使用自己的推荐码");
        }

        UserReferral userReferral = referralMapper.findByUserId(userId);
        if (userReferral == null) {
            userReferral = UserReferral.builder()
                    .userId(userId)
                    .referralCode(generateReferralCode())
                    .referredBy(referral.getUserId())
                    .referralCodeUsed(1)
                    .build();
            referralMapper.insert(userReferral);
        } else {
            userReferral.setReferredBy(referral.getUserId());
            userReferral.setReferralCodeUsed(1);
            referralMapper.updateReferralCodeUsed(userId, 1);
        }

        InviteRecord inviteRecord = InviteRecord.builder()
                .inviterId(referral.getUserId())
                .inviteeId(userId)
                .referralCode(referralCode)
                .inviteStatus(1)
                .registerTime(LocalDateTime.now())
                .rewardGiven(0)
                .build();
        referralMapper.insertInviteRecord(inviteRecord);

        creditService.addPoints(referral.getUserId(), 20, "好友注册奖励", null);
        creditService.addPoints(userId, 20, "使用推荐码注册奖励", null);

        try {
            achievementService.checkInviteAchievements(referral.getUserId());
            achievementService.checkPointsAchievements(referral.getUserId());
            achievementService.checkPointsAchievements(userId);
        } catch (Exception e) {
            log.error("检查邀请或积分成就失败: inviterId={}, inviteeId={}", referral.getUserId(), userId, e);
        }
    }

    @Transactional
    public void recordFirstDivination(Long userId) {
        if (userId == null) {
            return;
        }

        UserReferral referral = referralMapper.findByUserId(userId);
        if (referral == null || referral.getReferredBy() == null) {
            return;
        }

        InviteRecord inviteRecord = referralMapper.findByInviteeId(userId);
        if (inviteRecord == null) {
            return;
        }

        Integer inviteStatus = inviteRecord.getInviteStatus();
        if (inviteStatus != null && inviteStatus >= 2) {
            return;
        }

        inviteRecord.setInviteStatus(2);
        inviteRecord.setFirstDivinationTime(LocalDateTime.now());
        referralMapper.updateInviteRecord(inviteRecord);

        Integer rewardGiven = inviteRecord.getRewardGiven();
        if (rewardGiven == null || rewardGiven == 0) {
            creditService.addPoints(referral.getReferredBy(), 30, "好友首次占卜奖励", null);
            inviteRecord.setRewardGiven(1);
            inviteRecord.setInviteStatus(3);
            referralMapper.updateInviteRecord(inviteRecord);

            try {
                achievementService.checkPointsAchievements(referral.getReferredBy());
            } catch (Exception e) {
                log.error("检查邀请首占积分成就失败: inviterId={}, inviteeId={}", referral.getReferredBy(), userId, e);
            }
        }
    }

    public Map<String, Object> getInviteStats(Long userId) {
        Map<String, Object> stats = referralMapper.getInviteStats(userId);
        if (stats == null || stats.isEmpty()) {
            stats = new HashMap<>();
            stats.put("total", 0);
            stats.put("registered", 0);
            stats.put("divined", 0);
            stats.put("completed", 0);
        }

        Number totalNum = (Number) stats.getOrDefault("total", 0);
        Number completedNum = (Number) stats.getOrDefault("completed", 0);
        int total = totalNum.intValue();
        int completed = completedNum.intValue();
        stats.put("total", total);
        stats.put("registered", ((Number) stats.getOrDefault("registered", 0)).intValue());
        stats.put("divined", ((Number) stats.getOrDefault("divined", 0)).intValue());
        stats.put("completed", completed);
        stats.put("pending", total - completed);

        return stats;
    }

    public List<InviteRecord> getInviteRecords(Long userId) {
        return referralMapper.findInviteRecordsByInviterId(userId);
    }

    private String generateReferralCode() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }
}

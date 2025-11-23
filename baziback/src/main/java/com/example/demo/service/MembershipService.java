package com.example.demo.service;

import com.example.demo.entity.Membership;
import com.example.demo.entity.MembershipPackage;
import com.example.demo.mapper.MembershipMapper;
import com.example.demo.mapper.MembershipPackageMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * 会员服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MembershipService {

    private final MembershipMapper membershipMapper;
    private final MembershipPackageMapper membershipPackageMapper;

    /**
     * 创建会员
     */
    @Transactional
    public Membership createMembership(Long userId, Integer membershipType, Long orderId) {
        // 查询套餐信息
        MembershipPackage pkg = membershipPackageMapper.findByPackageType(membershipType);
        if (pkg == null) {
            throw new RuntimeException("会员套餐不存在");
        }

        // 查询用户当前会员
        Membership currentMembership = membershipMapper.findActiveByUserId(userId);
        
        LocalDateTime startTime;
        LocalDateTime endTime;
        
        if (currentMembership != null && currentMembership.getEndTime().isAfter(LocalDateTime.now())) {
            // 如果有有效会员，从当前会员结束时间开始计算
            startTime = currentMembership.getEndTime();
            endTime = startTime.plusDays(pkg.getDurationDays());
        } else {
            // 否则从现在开始计算
            startTime = LocalDateTime.now();
            endTime = startTime.plusDays(pkg.getDurationDays());
        }

        // 创建会员记录
        Membership membership = Membership.builder()
                .userId(userId)
                .membershipType(membershipType)
                .startTime(startTime)
                .endTime(endTime)
                .status(1) // 正常
                .orderId(orderId)
                .build();

        membershipMapper.insert(membership);
        log.info("创建会员成功，用户ID：{}，会员类型：{}，结束时间：{}", userId, membershipType, endTime);
        
        return membership;
    }

    /**
     * 查询用户有效会员
     */
    public Membership getActiveMembership(Long userId) {
        return membershipMapper.findActiveByUserId(userId);
    }

    /**
     * 检查用户是否是会员
     */
    public boolean isMember(Long userId) {
        Membership membership = membershipMapper.findActiveByUserId(userId);
        return membership != null && membership.getEndTime().isAfter(LocalDateTime.now());
    }

    /**
     * 获取所有会员套餐
     */
    public List<MembershipPackage> getAllPackages() {
        return membershipPackageMapper.findAllActive();
    }

    /**
     * 根据ID获取会员套餐
     */
    public MembershipPackage getPackageById(Long id) {
        return membershipPackageMapper.findById(id);
    }

    /**
     * 获取会员类型描述
     */
    public String getMembershipTypeDesc(Integer type) {
        return switch (type) {
            case 1 -> "月度会员";
            case 2 -> "季度会员";
            case 3 -> "年度会员";
            default -> "未知类型";
        };
    }

    /**
     * 计算剩余天数
     */
    public long getRemainingDays(LocalDateTime endTime) {
        if (endTime == null || endTime.isBefore(LocalDateTime.now())) {
            return 0;
        }
        return ChronoUnit.DAYS.between(LocalDateTime.now(), endTime);
    }

    /**
     * 更新过期会员状态
     */
    @Transactional
    public void updateExpiredMemberships() {
        int count = membershipMapper.updateExpiredMemberships();
        if (count > 0) {
            log.info("更新过期会员状态，数量：{}", count);
        }
    }
}

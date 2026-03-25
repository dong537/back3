package com.example.demo.mapper;

import com.example.demo.entity.InviteRecord;
import com.example.demo.entity.UserReferral;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

/**
 * 推荐/邀请数据访问层
 */
@Mapper
public interface ReferralMapper {
    
    /**
     * 根据用户ID查询推荐关系
     */
    @Select("SELECT * FROM tb_user_referral WHERE user_id = #{userId}")
    UserReferral findByUserId(Long userId);
    
    /**
     * 根据推荐码查询推荐关系
     */
    @Select("SELECT * FROM tb_user_referral WHERE referral_code = #{referralCode}")
    UserReferral findByReferralCode(String referralCode);
    
    /**
     * 插入推荐关系
     */
    @Insert("INSERT INTO tb_user_referral (user_id, referral_code, referred_by, referral_code_used) " +
            "VALUES (#{userId}, #{referralCode}, #{referredBy}, #{referralCodeUsed})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(UserReferral referral);
    
    /**
     * 更新推荐码使用状态
     */
    @Update("UPDATE tb_user_referral SET referral_code_used = #{referralCodeUsed} WHERE user_id = #{userId}")
    int updateReferralCodeUsed(@Param("userId") Long userId, @Param("referralCodeUsed") Integer referralCodeUsed);
    
    /**
     * 查询用户的邀请记录
     */
    @Select("SELECT * FROM tb_invite_record WHERE inviter_id = #{inviterId} ORDER BY create_time DESC")
    List<InviteRecord> findInviteRecordsByInviterId(Long inviterId);
    
    /**
     * 根据被邀请人ID查询邀请记录
     */
    @Select("SELECT * FROM tb_invite_record WHERE invitee_id = #{inviteeId}")
    InviteRecord findByInviteeId(Long inviteeId);
    
    /**
     * 插入邀请记录
     */
    @Insert("INSERT INTO tb_invite_record (inviter_id, referral_code, invite_status) " +
            "VALUES (#{inviterId}, #{referralCode}, #{inviteStatus})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertInviteRecord(InviteRecord record);
    
    /**
     * 更新邀请记录
     */
    @Update("UPDATE tb_invite_record SET invitee_id = #{inviteeId}, invite_status = #{inviteStatus}, " +
            "register_time = #{registerTime}, first_divination_time = #{firstDivinationTime}, " +
            "reward_given = #{rewardGiven} WHERE id = #{id}")
    int updateInviteRecord(InviteRecord record);
    
    /**
     * 统计邀请数据（返回Map）
     */
    @Select("SELECT " +
            "COUNT(*) as total, " +
            "COALESCE(SUM(CASE WHEN invite_status >= 1 THEN 1 ELSE 0 END), 0) as registered, " +
            "COALESCE(SUM(CASE WHEN invite_status >= 2 THEN 1 ELSE 0 END), 0) as divined, " +
            "COALESCE(SUM(CASE WHEN invite_status >= 3 THEN 1 ELSE 0 END), 0) as completed " +
            "FROM tb_invite_record WHERE inviter_id = #{inviterId}")
    Map<String, Object> getInviteStats(Long inviterId);

    @Select("SELECT COALESCE(SUM(CASE WHEN invite_status >= 1 THEN 1 ELSE 0 END), 0) " +
            "FROM tb_invite_record WHERE inviter_id = #{inviterId}")
    Integer countRegisteredInvites(Long inviterId);
}

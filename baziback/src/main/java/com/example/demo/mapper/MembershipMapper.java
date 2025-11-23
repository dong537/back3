package com.example.demo.mapper;

import com.example.demo.entity.Membership;
import org.apache.ibatis.annotations.*;

/**
 * 会员Mapper接口
 */
@Mapper
public interface MembershipMapper {

    /**
     * 插入会员记录
     */
    @Insert("INSERT INTO tb_membership (user_id, membership_type, start_time, end_time, status, order_id) " +
            "VALUES (#{userId}, #{membershipType}, #{startTime}, #{endTime}, #{status}, #{orderId})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Membership membership);

    /**
     * 根据用户ID查询有效会员
     */
    @Select("SELECT * FROM tb_membership WHERE user_id = #{userId} AND status = 1 AND end_time > NOW() ORDER BY end_time DESC LIMIT 1")
    Membership findActiveByUserId(Long userId);

    /**
     * 根据订单ID查询会员
     */
    @Select("SELECT * FROM tb_membership WHERE order_id = #{orderId}")
    Membership findByOrderId(Long orderId);

    /**
     * 更新会员状态
     */
    @Update("UPDATE tb_membership SET status = #{status} WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    /**
     * 更新过期会员状态
     */
    @Update("UPDATE tb_membership SET status = 0 WHERE status = 1 AND end_time < NOW()")
    int updateExpiredMemberships();
}

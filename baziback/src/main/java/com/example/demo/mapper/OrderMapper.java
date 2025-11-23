package com.example.demo.mapper;

import com.example.demo.entity.Order;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 订单Mapper接口
 */
@Mapper
public interface OrderMapper {

    /**
     * 插入订单
     */
    @Insert("INSERT INTO tb_order (order_no, user_id, order_type, product_name, product_desc, amount, status, expire_time) " +
            "VALUES (#{orderNo}, #{userId}, #{orderType}, #{productName}, #{productDesc}, #{amount}, #{status}, #{expireTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Order order);

    /**
     * 根据订单号查询订单
     */
    @Select("SELECT * FROM tb_order WHERE order_no = #{orderNo}")
    Order findByOrderNo(String orderNo);

    /**
     * 根据用户ID查询订单列表
     */
    @Select("SELECT * FROM tb_order WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<Order> findByUserId(Long userId);

    /**
     * 更新订单状态
     */
    @Update("UPDATE tb_order SET status = #{status}, trade_no = #{tradeNo}, pay_time = #{payTime} WHERE order_no = #{orderNo}")
    int updateStatus(@Param("orderNo") String orderNo, 
                     @Param("status") Integer status, 
                     @Param("tradeNo") String tradeNo, 
                     @Param("payTime") java.time.LocalDateTime payTime);

    /**
     * 取消订单
     */
    @Update("UPDATE tb_order SET status = 2, cancel_time = NOW() WHERE order_no = #{orderNo} AND status = 0")
    int cancelOrder(String orderNo);

    /**
     * 查询过期未支付订单
     */
    @Select("SELECT * FROM tb_order WHERE status = 0 AND expire_time < NOW()")
    List<Order> findExpiredOrders();
}

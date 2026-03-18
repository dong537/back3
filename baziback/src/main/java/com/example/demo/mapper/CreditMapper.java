package com.example.demo.mapper;

import com.example.demo.entity.ExchangeProduct;
import com.example.demo.entity.ExchangeRecord;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 积分数据访问层
 */
@Mapper
public interface CreditMapper {
    
    /**
     * 查询所有可用的兑换商品
     */
    @Select("SELECT * FROM tb_exchange_product WHERE is_active = 1 ORDER BY sort_order ASC")
    List<ExchangeProduct> findAllActiveProducts();
    
    /**
     * 根据商品代码查询商品
     */
    @Select("SELECT * FROM tb_exchange_product WHERE product_code = #{productCode} AND is_active = 1")
    ExchangeProduct findByProductCode(String productCode);
    
    /**
     * 插入兑换记录
     */
    @Insert("INSERT INTO tb_exchange_record (user_id, product_id, product_code, product_name, " +
            "points_cost, product_value, status, expire_time) " +
            "VALUES (#{userId}, #{productId}, #{productCode}, #{productName}, " +
            "#{pointsCost}, #{productValue}, #{status}, #{expireTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertExchangeRecord(ExchangeRecord record);
    
    /**
     * 查询用户的兑换记录
     */
    @Select("SELECT * FROM tb_exchange_record WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<ExchangeRecord> findExchangeRecordsByUserId(Long userId);
    
    /**
     * 查询用户今日兑换次数
     */
    @Select("SELECT COUNT(*) FROM tb_exchange_record " +
            "WHERE user_id = #{userId} AND product_code = #{productCode} " +
            "AND DATE(create_time) = CURDATE()")
    int countTodayExchange(@Param("userId") Long userId, @Param("productCode") String productCode);
    
    /**
     * 查询用户总兑换次数
     */
    @Select("SELECT COUNT(*) FROM tb_exchange_record " +
            "WHERE user_id = #{userId} AND product_code = #{productCode}")
    int countTotalExchange(@Param("userId") Long userId, @Param("productCode") String productCode);
    
    /**
     * 更新兑换记录状态
     */
    @Update("UPDATE tb_exchange_record SET status = #{status}, used_time = #{usedTime} WHERE id = #{id}")
    int updateExchangeRecordStatus(ExchangeRecord record);
    
    /**
     * 查询用户积分余额
     */
    @Select("SELECT balance FROM tb_credit WHERE user_id = #{userId}")
    Integer getBalanceByUserId(Long userId);

    /**
     * 查询用户积分余额（FOR UPDATE，用于事务内锁定余额行，防并发写入不一致）
     */
    @Select("SELECT balance FROM tb_credit WHERE user_id = #{userId} FOR UPDATE")
    Integer getBalanceByUserIdForUpdate(Long userId);

    /**
     * 初始化用户积分记录（当 tb_credit 不存在该用户行时）
     */
    @Insert("INSERT INTO tb_credit (user_id, balance) VALUES (#{userId}, #{balance})")
    int initUserCredit(@Param("userId") Long userId, @Param("balance") Integer balance);

    /**
     * 变更积分（原子更新；正数增加，负数减少）
     */
    @Update("UPDATE tb_credit SET balance = balance + #{delta} WHERE user_id = #{userId}")
    int addPoints(@Param("userId") Long userId, @Param("delta") Integer delta);
    
    /**
     * 插入积分流水
     */
    @Insert("INSERT INTO tb_credit_transaction (user_id, transaction_type, amount, balance_before, balance_after, description, related_order_id) " +
            "VALUES (#{userId}, #{transactionType}, #{amount}, #{balanceBefore}, #{balanceAfter}, #{description}, #{relatedOrderId})")
    int insertTransaction(@Param("userId") Long userId,
                         @Param("transactionType") Integer transactionType,
                         @Param("amount") Integer amount,
                         @Param("balanceBefore") Integer balanceBefore,
                         @Param("balanceAfter") Integer balanceAfter,
                         @Param("description") String description,
                         @Param("relatedOrderId") Long relatedOrderId);
}

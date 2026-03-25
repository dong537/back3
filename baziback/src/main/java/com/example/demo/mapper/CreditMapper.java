package com.example.demo.mapper;

import com.example.demo.entity.ExchangeProduct;
import com.example.demo.entity.ExchangeRecord;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 积分数据访问层
 */
@Mapper
public interface CreditMapper {

    @Select("SELECT * FROM tb_exchange_product WHERE is_active = 1 ORDER BY sort_order ASC")
    List<ExchangeProduct> findAllActiveProducts();

    @Select("SELECT * FROM tb_exchange_product WHERE product_code = #{productCode} AND is_active = 1")
    ExchangeProduct findByProductCode(String productCode);

    @Insert("INSERT INTO tb_exchange_record (user_id, product_id, product_code, product_name, " +
            "points_cost, product_value, status, expire_time) " +
            "VALUES (#{userId}, #{productId}, #{productCode}, #{productName}, " +
            "#{pointsCost}, #{productValue}, #{status}, #{expireTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertExchangeRecord(ExchangeRecord record);

    @Select("SELECT * FROM tb_exchange_record WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<ExchangeRecord> findExchangeRecordsByUserId(Long userId);

    @Select("SELECT COUNT(*) FROM tb_exchange_record " +
            "WHERE user_id = #{userId} AND product_code = #{productCode} " +
            "AND DATE(create_time) = CURDATE()")
    int countTodayExchange(@Param("userId") Long userId, @Param("productCode") String productCode);

    @Select("SELECT COUNT(*) FROM tb_exchange_record " +
            "WHERE user_id = #{userId} AND product_code = #{productCode}")
    int countTotalExchange(@Param("userId") Long userId, @Param("productCode") String productCode);

    @Update("UPDATE tb_exchange_record SET status = #{status}, used_time = #{usedTime} WHERE id = #{id}")
    int updateExchangeRecordStatus(ExchangeRecord record);

    @Select("SELECT balance FROM tb_credit WHERE user_id = #{userId}")
    Integer getBalanceByUserId(Long userId);

    @Select("SELECT balance FROM tb_credit WHERE user_id = #{userId} FOR UPDATE")
    Integer getBalanceByUserIdForUpdate(Long userId);

    /**
     * Safe under concurrent initialization. Existing row is preserved.
     */
    @Insert("INSERT IGNORE INTO tb_credit (user_id, balance) VALUES (#{userId}, #{balance})")
    int initUserCredit(@Param("userId") Long userId, @Param("balance") Integer balance);

    @Update("UPDATE tb_credit SET balance = balance + #{delta} WHERE user_id = #{userId}")
    int addPoints(@Param("userId") Long userId, @Param("delta") Integer delta);

    @Update("UPDATE tb_credit SET balance = balance - #{points} " +
            "WHERE user_id = #{userId} AND balance >= #{points}")
    int deductPointsIfEnough(@Param("userId") Long userId, @Param("points") Integer points);

    @Insert("INSERT INTO tb_credit_transaction (user_id, transaction_type, amount, balance_before, balance_after, description, related_order_id) " +
            "VALUES (#{userId}, #{transactionType}, #{amount}, #{balanceBefore}, #{balanceAfter}, #{description}, #{relatedOrderId})")
    int insertTransaction(@Param("userId") Long userId,
                          @Param("transactionType") Integer transactionType,
                          @Param("amount") Integer amount,
                          @Param("balanceBefore") Integer balanceBefore,
                          @Param("balanceAfter") Integer balanceAfter,
                          @Param("description") String description,
                          @Param("relatedOrderId") Long relatedOrderId);

    @Select("SELECT COUNT(*) FROM tb_credit_transaction " +
            "WHERE user_id = #{userId} " +
            "AND transaction_type = #{transactionType} " +
            "AND description = #{description} " +
            "AND DATE(create_time) = CURDATE()")
    int countTransactionsTodayByTypeAndDescription(@Param("userId") Long userId,
                                                   @Param("transactionType") Integer transactionType,
                                                   @Param("description") String description);

    @Select("SELECT COALESCE(SUM(amount), 0) FROM tb_credit_transaction " +
            "WHERE user_id = #{userId} AND amount > 0")
    Integer sumEarnedPoints(Long userId);
}

package com.example.demo.mapper;

import com.example.demo.entity.PaymentLog;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 支付日志Mapper接口
 */
@Mapper
public interface PaymentLogMapper {

    /**
     * 插入支付日志
     */
    @Insert("INSERT INTO tb_payment_log (order_no, trade_no, notify_type, notify_data, notify_time) " +
            "VALUES (#{orderNo}, #{tradeNo}, #{notifyType}, #{notifyData}, #{notifyTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(PaymentLog paymentLog);

    /**
     * 根据订单号查询日志
     */
    @Select("SELECT * FROM tb_payment_log WHERE order_no = #{orderNo} ORDER BY create_time DESC")
    List<PaymentLog> findByOrderNo(String orderNo);

    /**
     * 根据交易号查询日志
     */
    @Select("SELECT * FROM tb_payment_log WHERE trade_no = #{tradeNo} ORDER BY create_time DESC")
    List<PaymentLog> findByTradeNo(String tradeNo);
}

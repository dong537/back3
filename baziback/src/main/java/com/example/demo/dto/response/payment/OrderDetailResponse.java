package com.example.demo.dto.response.payment;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单详情响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailResponse {

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 订单类型：1-普通支付，2-会员购买
     */
    private Integer orderType;

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 商品描述
     */
    private String productDesc;

    /**
     * 订单金额
     */
    private BigDecimal amount;

    /**
     * 订单状态：0-待支付，1-已支付，2-已取消，3-已退款
     */
    private Integer status;

    /**
     * 订单状态描述
     */
    private String statusDesc;

    /**
     * 支付宝交易号
     */
    private String tradeNo;

    /**
     * 支付时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime payTime;

    /**
     * 订单过期时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime expireTime;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime createTime;
}

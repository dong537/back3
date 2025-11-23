package com.example.demo.dto.response.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建订单响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderResponse {

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 支付表单HTML（用于前端提交）
     */
    private String payForm;

    /**
     * 直接可跳转的支付链接（已带签名参数）
     */
    private String payUrl;

    /**
     * 订单过期时间（分钟）
     */
    private Integer expireMinutes;
}

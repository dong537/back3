package com.example.demo.dto.request.payment;

import lombok.Data;

import java.util.Map;

/**
 * 支付宝异步通知请求DTO
 */
@Data
public class PaymentNotifyRequest {

    /**
     * 通知参数
     */
    private Map<String, String> params;
}

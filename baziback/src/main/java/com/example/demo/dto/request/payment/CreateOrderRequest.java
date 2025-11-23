package com.example.demo.dto.request.payment;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 创建订单请求DTO
 */
@Data
public class CreateOrderRequest {

    /**
     * 订单类型：1-普通支付，2-会员购买
     */
    @NotNull(message = "订单类型不能为空")
    private Integer orderType;

    /**
     * 商品名称
     */
    @NotBlank(message = "商品名称不能为空")
    private String productName;

    /**
     * 商品描述
     */
    private String productDesc;

    /**
     * 订单金额（普通支付时必填）
     */
    @DecimalMin(value = "0.01", message = "订单金额必须大于0")
    private BigDecimal amount;

    /**
     * 会员套餐ID（会员购买时必填）
     */
    private Long packageId;
}

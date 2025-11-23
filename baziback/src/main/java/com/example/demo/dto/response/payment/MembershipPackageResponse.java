package com.example.demo.dto.response.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 会员套餐响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MembershipPackageResponse {

    /**
     * 套餐ID
     */
    private Long id;

    /**
     * 套餐名称
     */
    private String packageName;

    /**
     * 套餐类型：1-月度会员，2-季度会员，3-年度会员
     */
    private Integer packageType;

    /**
     * 有效天数
     */
    private Integer durationDays;

    /**
     * 原价
     */
    private BigDecimal originalPrice;

    /**
     * 售价
     */
    private BigDecimal salePrice;

    /**
     * 折扣
     */
    private String discount;

    /**
     * 套餐描述
     */
    private String description;

    /**
     * 套餐特权列表
     */
    private List<String> features;
}

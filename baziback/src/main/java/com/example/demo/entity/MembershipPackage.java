package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 会员套餐实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MembershipPackage {

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
     * 套餐描述
     */
    private String description;

    /**
     * 套餐特权（JSON格式）
     */
    private String features;

    /**
     * 状态：0-下架，1-上架
     */
    private Integer status;

    /**
     * 排序
     */
    private Integer sortOrder;

    /**
     * 创建时间
     */
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime updateTime;
}

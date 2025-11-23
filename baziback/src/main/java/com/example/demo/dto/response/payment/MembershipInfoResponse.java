package com.example.demo.dto.response.payment;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 会员信息响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MembershipInfoResponse {

    /**
     * 是否是会员
     */
    private Boolean isMember;

    /**
     * 会员类型：1-月度会员，2-季度会员，3-年度会员
     */
    private Integer membershipType;

    /**
     * 会员类型描述
     */
    private String membershipTypeDesc;

    /**
     * 会员开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime startTime;

    /**
     * 会员结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime endTime;

    /**
     * 剩余天数
     */
    private Long remainingDays;
}

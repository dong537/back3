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

import java.time.LocalDateTime;

/**
 * 联系方式记录实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TbContactRecord {
    private Long id;
    private Long userId;
    private String contactType; // 联系方式类型：wechat, phone, email等
    private String contactName; // 联系人姓名
    private String contactInfo; // 联系信息（微信ID、手机号、邮箱等）
    private String sourcePage; // 来源页面
    private String sourceType; // 来源类型（如：divination_result, homepage等）
    private Long relatedRecordId; // 关联的记录ID（如占卜记录ID）
    private String actionType; // 操作类型：view（查看）, click（点击）, scan（扫码）
    private String ipAddress; // IP地址
    private String userAgent; // 用户代理信息
    
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime createTime;
    
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime updateTime;
}

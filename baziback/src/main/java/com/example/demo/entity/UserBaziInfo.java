package com.example.demo.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户八字信息实体
 */
@Data
public class UserBaziInfo {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 姓名
     */
    private String name;
    
    /**
     * 性别：0-女，1-男
     */
    private Integer gender;
    
    /**
     * 出生年份
     */
    private Integer birthYear;
    
    /**
     * 出生月份
     */
    private Integer birthMonth;
    
    /**
     * 出生日期
     */
    private Integer birthDay;
    
    /**
     * 出生时辰（0-23）
     */
    private Integer birthHour;
    
    /**
     * 出生分钟
     */
    private Integer birthMinute;
    
    /**
     * 是否农历：0-公历，1-农历
     */
    private Integer isLunar;
    
    /**
     * 时区
     */
    private String timezone;
    
    /**
     * 出生地
     */
    private String birthplace;
    
    /**
     * 八字数据（JSON格式）
     */
    private String baziData;
    
    /**
     * 是否默认八字：0-否，1-是
     */
    private Integer isDefault;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}

package com.example.demo.dto.request.bazi;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 更新八字信息请求
 */
@Data
public class UpdateBaziInfoRequest {
    
    /**
     * 八字信息ID
     */
    @NotNull(message = "八字信息ID不能为空")
    private Long id;
    
    /**
     * 姓名
     */
    private String name;
    
    /**
     * 性别：0-女，1-男
     */
    @Min(value = 0, message = "性别值必须为0或1")
    @Max(value = 1, message = "性别值必须为0或1")
    private Integer gender;
    
    /**
     * 出生年份
     */
    @Min(value = 1900, message = "出生年份不能早于1900年")
    @Max(value = 2100, message = "出生年份不能晚于2100年")
    private Integer birthYear;
    
    /**
     * 出生月份
     */
    @Min(value = 1, message = "月份必须在1-12之间")
    @Max(value = 12, message = "月份必须在1-12之间")
    private Integer birthMonth;
    
    /**
     * 出生日期
     */
    @Min(value = 1, message = "日期必须在1-31之间")
    @Max(value = 31, message = "日期必须在1-31之间")
    private Integer birthDay;
    
    /**
     * 出生时辰（0-23）
     */
    @Min(value = 0, message = "时辰必须在0-23之间")
    @Max(value = 23, message = "时辰必须在0-23之间")
    private Integer birthHour;
    
    /**
     * 出生分钟
     */
    @Min(value = 0, message = "分钟必须在0-59之间")
    @Max(value = 59, message = "分钟必须在0-59之间")
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
     * 是否设为默认：0-否，1-是
     */
    private Integer isDefault;
}

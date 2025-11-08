package com.example.demo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class GenerateChartRequest {

    @NotBlank(message = "出生日期不能为空")
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "出生日期格式应为yyyy-MM-dd")
    private String birthDate;

    private String birthLocation; // 可选参数

    @NotBlank(message = "出生时间不能为空")
    @Pattern(regexp = "^\\d{4}$", message = "出生时间格式应为HHmm（如0830表示8点30分）")
    private String birthTime;

    @NotBlank(message = "性别不能为空")
    @Pattern(regexp = "^(男|女)$", message = "性别只能为'男'或'女'")
    private String gender;

    private String name; // 可选参数

    /**
     * 将性别转换为数字格式（0=女，1=男）
     * /
    public Integer getGenderAsNumber() {
        return "男".equals(gender) ? 1 : 0;
    }
    /**
     * 格式化日期时间为紫微斗数接口需要的格式
     */
    public String getFormattedSolarDatetime() {
        // 将0830转换为08:30:00
        String formattedTime = birthTime.substring(0, 2) + ":" + birthTime.substring(2) + ":00";
        return birthDate + " " + formattedTime + "+0800";
    }
}
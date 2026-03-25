package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 每日幸运实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DailyLucky {
    
    private Long id;
    
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "Asia/Shanghai")
    private LocalDate luckyDate;
    
    private String luckyNumber;          // 幸运数字
    private String luckyColor;           // 幸运颜色
    private String luckyConstellation;   // 幸运星座
    private String luckyFood;            // 幸运食物
    private String suitableActions;      // 宜：适合做的事情
    private String unsuitableActions;    // 忌：不适合做的事情
    private String description;          // 描述
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime createTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime updateTime;
}

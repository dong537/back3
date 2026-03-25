package com.example.demo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDate;

/**
 * 每日塔罗牌抽牌记录实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TbTarotDailyDraw {
    private Integer id;  // 改为Integer以匹配数据库INT类型
    private Long userId;
    private LocalDate drawDate;
    private Integer cardId;
    private Boolean isReversed;
    private java.time.LocalDateTime createdAt;
}

package com.example.demo.entity;

import lombok.Data;
import lombok.Builder;
import java.time.LocalDateTime;

@Data
@Builder
public class CreditTransaction {
    private Long id;
    private Long userId;
    private Integer transactionType;
    private Integer amount;
    private Integer balanceBefore;
    private Integer balanceAfter;
    private String description;
    private Long relatedOrderId;
    private LocalDateTime createTime;
}

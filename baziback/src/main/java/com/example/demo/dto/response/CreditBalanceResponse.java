package com.example.demo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 积分余额响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreditBalanceResponse {
    private Integer balance;
}

package com.example.demo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一错误响应（可选）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiErrorResponse {
    private Integer code;
    private String message;
}

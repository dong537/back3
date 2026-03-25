package com.example.demo.dto.response.bazi;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BaziQiYunResponse {
    private Integer qiYunAge;
    private Integer qiYunYear;
    private String description;
}

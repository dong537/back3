package com.example.demo.dto.response.bazi;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BaziPalaceResponse {
    private String taiYuan;
    private String mingGong;
    private String shenGong;
}

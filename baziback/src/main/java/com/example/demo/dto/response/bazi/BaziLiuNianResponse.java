package com.example.demo.dto.response.bazi;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BaziLiuNianResponse {
    private Integer year;
    private String age;
    private String ganZhi;
    private String tianGan;
    private String diZhi;
    private String tianGanShiShen;
    private String diZhiShiShen;
    private String tianGanWuXing;
    private String diZhiWuXing;
    private String naYin;
    private List<String> diZhiCangGan;
}

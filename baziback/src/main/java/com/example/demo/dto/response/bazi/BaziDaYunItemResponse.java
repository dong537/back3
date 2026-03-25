package com.example.demo.dto.response.bazi;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BaziDaYunItemResponse {
    private String ganZhi;
    private Integer startYear;
    private Integer endYear;
    private String tianGan;
    private String diZhi;
    private String tianGanShiShen;
    private String diZhiShiShen;
    private String tianGanWuXing;
    private String diZhiWuXing;
    private String naYin;
    private List<String> diZhiCangGan;
    private Map<String, Object> benQi;
    private Map<String, Object> zhongQi;
    private Map<String, Object> yuQi;
}

package com.example.demo.dto.response.bazi;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BaziAnalysisResponse {
    private String id;
    private String baZi;
    private String birthDateTime;
    private String calculatedTime;
    private String trueSolarTime;
    private Integer trueSolarTimeOffsetMinutes;
    private Double longitude;
    private String gender;
    private String season;
    private String zodiac;
    private String dayMaster;
    private String dayMasterElement;
    private String bodyStrength;
    private String geJu;
    private String renYuanSiLing;
    @Builder.Default
    private Map<String, Integer> fiveElements = new LinkedHashMap<>();
    private BaziPalaceResponse palaces;
    @Builder.Default
    private Map<String, BaziPillarResponse> pillars = new LinkedHashMap<>();
    @Builder.Default
    private Map<String, String> xingYun = new LinkedHashMap<>();
    @Builder.Default
    private Map<String, String> ziZuo = new LinkedHashMap<>();
    @Builder.Default
    private Map<String, String> kongWang = new LinkedHashMap<>();
    @Builder.Default
    private Map<String, String> naYin = new LinkedHashMap<>();
    @Builder.Default
    private Map<String, List<String>> shenSha = new LinkedHashMap<>();
    private BaziQiYunResponse qiYun;
    private BaziDaYunResponse daYun;
    @Builder.Default
    private List<BaziLiuNianResponse> liuNian = new ArrayList<>();
    @Builder.Default
    private List<BaziLiuYueResponse> liuYue = new ArrayList<>();
    @Builder.Default
    private Map<String, Object> riZhuInfo = new LinkedHashMap<>();
    @Builder.Default
    private Map<String, Object> xiYongShen = new LinkedHashMap<>();
    @Builder.Default
    private Map<String, Object> xingChongHeHui = new LinkedHashMap<>();
    @Builder.Default
    private Map<String, Object> yinYangAnalysis = new LinkedHashMap<>();
    @Builder.Default
    private Map<String, Object> caiXing = new LinkedHashMap<>();
    @Builder.Default
    private Map<String, Object> fuQi = new LinkedHashMap<>();
    @Builder.Default
    private Map<String, Object> fuMu = new LinkedHashMap<>();
    @Builder.Default
    private Map<String, Object> ziNv = new LinkedHashMap<>();
    @Builder.Default
    private Map<String, Object> tiaoHou = new LinkedHashMap<>();
}

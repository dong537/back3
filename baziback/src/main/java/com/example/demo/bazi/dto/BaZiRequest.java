package com.example.demo.bazi.dto;

/**
 * 八字分析请求DTO
 */
public class BaZiRequest {
    private String baZi;           // 八字字符串，格式：乙酉 己丑 甲辰 戊辰
    private Integer birthYear;     // 出生年份
    private Boolean isMale;        // 是否男命
    private Integer qiYunAge;      // 起运年龄（可选，默认4岁）

    public BaZiRequest() {
        this.isMale = true;
        this.qiYunAge = 4;
    }

    public BaZiRequest(String baZi, Integer birthYear, Boolean isMale, Integer qiYunAge) {
        this.baZi = baZi;
        this.birthYear = birthYear;
        this.isMale = isMale;
        this.qiYunAge = qiYunAge != null ? qiYunAge : 4;
    }

    public String getBaZi() { return baZi; }
    public void setBaZi(String baZi) { this.baZi = baZi; }
    public Integer getBirthYear() { return birthYear; }
    public void setBirthYear(Integer birthYear) { this.birthYear = birthYear; }
    public Boolean getIsMale() { return isMale; }
    public void setIsMale(Boolean isMale) { this.isMale = isMale; }
    public Integer getQiYunAge() { return qiYunAge; }
    public void setQiYunAge(Integer qiYunAge) { this.qiYunAge = qiYunAge; }
}

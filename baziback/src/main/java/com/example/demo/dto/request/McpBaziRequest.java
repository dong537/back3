package com.example.demo.dto.request;

import lombok.Data;

@Data
public class McpBaziRequest {
    // 阳历时间（格式：yyyy-MM-dd HH:mm:ss+0800）
    private String solarDatetime;
    // 农历时间（格式同上）
    private String lunarDatetime;
    // 性别（0=女，1=男）
    private Integer gender;
    // 早晚子时配置（1=次日，2=当日）
    private Integer eightCharProviderSect;

    public String getSolarDatetime() {
        return solarDatetime;
    }

    public void setSolarDatetime(String solarDatetime) {
        this.solarDatetime = solarDatetime;
    }

    public String getLunarDatetime() {
        return lunarDatetime;
    }

    public void setLunarDatetime(String lunarDatetime) {
        this.lunarDatetime = lunarDatetime;
    }

    public Integer getGender() {
        return gender;
    }

    public void setGender(Integer gender) {
        this.gender = gender;
    }

    public Integer getEightCharProviderSect() {
        return eightCharProviderSect;
    }

    public void setEightCharProviderSect(Integer eightCharProviderSect) {
        this.eightCharProviderSect = eightCharProviderSect;
    }

    // 校验：阳历和农历必须二选一
    public boolean isEitherSolarOrLunar() {
        boolean hasSolar = solarDatetime != null && !solarDatetime.trim().isEmpty();
        boolean hasLunar = lunarDatetime != null && !lunarDatetime.trim().isEmpty();
        return hasSolar ^ hasLunar; // 异或：只有一个不为空
    }
}
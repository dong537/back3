package com.example.demo.bazi.model;

import com.example.demo.bazi.constants.*;
import java.util.*;

/**
 * 大运模型
 */
public class DaYun {
    private String ganZhi;          // 干支
    private int startYear;          // 开始年份
    private int endYear;            // 结束年份
    private String tianGan;         // 天干
    private String tianGanShiShen;  // 天干十神
    private String tianGanWuXing;   // 天干五行
    private String diZhi;           // 地支
    private String diZhiWuXing;     // 地支五行
    private List<String> diZhiShiShen;  // 地支十神
    private List<String> diZhiCangGan;  // 地支藏干
    private Map<String, Object> diZhiBenQi;   // 地支本气
    private Map<String, Object> diZhiZhongQi; // 地支中气
    private Map<String, Object> diZhiYuQi;    // 地支余气

    public DaYun() {
        this.diZhiShiShen = new ArrayList<>();
        this.diZhiCangGan = new ArrayList<>();
    }

    public DaYun(String ganZhi, int startYear, String riZhuTianGan) {
        this();
        this.ganZhi = ganZhi;
        this.startYear = startYear;
        this.endYear = startYear + 9;
        
        if (ganZhi.length() == 2) {
            this.tianGan = ganZhi.substring(0, 1);
            this.diZhi = ganZhi.substring(1, 2);
            
            TianGan tg = TianGan.fromName(this.tianGan);
            DiZhi dz = DiZhi.fromName(this.diZhi);
            
            this.tianGanWuXing = tg.getWuXing();
            this.diZhiWuXing = dz.getWuXing();
            
            // 计算天干十神
            this.tianGanShiShen = ShiShen.calculate(riZhuTianGan, this.tianGan).getName();
            
            // 设置藏干
            String[] cangGan = dz.getCangGan();
            Collections.addAll(this.diZhiCangGan, cangGan);
            
            // 计算藏干十神
            for (String cg : cangGan) {
                this.diZhiShiShen.add(ShiShen.calculate(riZhuTianGan, cg).getName());
            }
            
            // 本气
            if (cangGan.length > 0) {
                this.diZhiBenQi = new LinkedHashMap<>();
                diZhiBenQi.put("名称", cangGan[0]);
                diZhiBenQi.put("十神", ShiShen.calculate(riZhuTianGan, cangGan[0]).getName());
                diZhiBenQi.put("五行", TianGan.fromName(cangGan[0]).getWuXing());
            }
            
            // 中气
            if (cangGan.length > 1) {
                this.diZhiZhongQi = new LinkedHashMap<>();
                diZhiZhongQi.put("名称", cangGan[1]);
                diZhiZhongQi.put("十神", ShiShen.calculate(riZhuTianGan, cangGan[1]).getName());
                diZhiZhongQi.put("五行", TianGan.fromName(cangGan[1]).getWuXing());
            }
            
            // 余气
            if (cangGan.length > 2) {
                this.diZhiYuQi = new LinkedHashMap<>();
                diZhiYuQi.put("名称", cangGan[2]);
                diZhiYuQi.put("十神", ShiShen.calculate(riZhuTianGan, cangGan[2]).getName());
                diZhiYuQi.put("五行", TianGan.fromName(cangGan[2]).getWuXing());
            }
        }
    }

    /**
     * 转换为Map格式
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("干支", ganZhi);
        map.put("开始", startYear);
        map.put("结束", endYear);
        map.put("天干", tianGan);
        map.put("天干十神", tianGanShiShen);
        map.put("天干五行", tianGanWuXing);
        map.put("地支", diZhi);
        map.put("地支五行", diZhiWuXing);
        map.put("地支十神", diZhiShiShen);
        map.put("地支藏干", diZhiCangGan);
        
        if (diZhiBenQi != null) {
            map.put("地支本气", diZhiBenQi);
        }
        if (diZhiZhongQi != null) {
            map.put("地支中气", diZhiZhongQi);
        }
        if (diZhiYuQi != null) {
            map.put("地支余气", diZhiYuQi);
        }
        
        return map;
    }

    // Getters and Setters
    public String getGanZhi() { return ganZhi; }
    public void setGanZhi(String ganZhi) { this.ganZhi = ganZhi; }
    public int getStartYear() { return startYear; }
    public void setStartYear(int startYear) { this.startYear = startYear; }
    public int getEndYear() { return endYear; }
    public void setEndYear(int endYear) { this.endYear = endYear; }
    public String getTianGan() { return tianGan; }
    public void setTianGan(String tianGan) { this.tianGan = tianGan; }
    public String getTianGanShiShen() { return tianGanShiShen; }
    public void setTianGanShiShen(String tianGanShiShen) { this.tianGanShiShen = tianGanShiShen; }
    public String getTianGanWuXing() { return tianGanWuXing; }
    public void setTianGanWuXing(String tianGanWuXing) { this.tianGanWuXing = tianGanWuXing; }
    public String getDiZhi() { return diZhi; }
    public void setDiZhi(String diZhi) { this.diZhi = diZhi; }
    public String getDiZhiWuXing() { return diZhiWuXing; }
    public void setDiZhiWuXing(String diZhiWuXing) { this.diZhiWuXing = diZhiWuXing; }
    public List<String> getDiZhiShiShen() { return diZhiShiShen; }
    public void setDiZhiShiShen(List<String> diZhiShiShen) { this.diZhiShiShen = diZhiShiShen; }
    public List<String> getDiZhiCangGan() { return diZhiCangGan; }
    public void setDiZhiCangGan(List<String> diZhiCangGan) { this.diZhiCangGan = diZhiCangGan; }
    public Map<String, Object> getDiZhiBenQi() { return diZhiBenQi; }
    public void setDiZhiBenQi(Map<String, Object> diZhiBenQi) { this.diZhiBenQi = diZhiBenQi; }
    public Map<String, Object> getDiZhiZhongQi() { return diZhiZhongQi; }
    public void setDiZhiZhongQi(Map<String, Object> diZhiZhongQi) { this.diZhiZhongQi = diZhiZhongQi; }
    public Map<String, Object> getDiZhiYuQi() { return diZhiYuQi; }
    public void setDiZhiYuQi(Map<String, Object> diZhiYuQi) { this.diZhiYuQi = diZhiYuQi; }
}

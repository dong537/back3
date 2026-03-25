package com.example.demo.bazi.model;

import com.example.demo.bazi.constants.*;
import java.util.*;

/**
 * 流月模型
 */
public class LiuYue {
    private int month;                 // 月份（1-12）
    private String solarTerm;           // 节气名称
    private String solarTermDate;       // 节气日期（格式：M/d）
    private String ganZhi;              // 干支
    private String tianGan;             // 天干
    private String tianGanShiShen;      // 天干十神
    private String tianGanWuXing;       // 天干五行
    private String diZhi;               // 地支
    private String diZhiWuXing;        // 地支五行
    private List<String> diZhiCangGan; // 地支藏干
    private List<String> diZhiShiShen; // 地支十神（藏干对应的十神）

    public LiuYue() {
        this.diZhiCangGan = new ArrayList<>();
        this.diZhiShiShen = new ArrayList<>();
    }

    public LiuYue(int month, String solarTerm, String solarTermDate, int year, String riZhuTianGan) {
        this();
        this.month = month;
        this.solarTerm = solarTerm;
        this.solarTermDate = solarTermDate;
        
        // 流月天干：年干起月干（五虎遁）
        // 甲己之年丙作首，乙庚之年戊为头，丙辛之年寻庚起，丁壬壬寅顺水流，若问戊癸何处起，甲寅之上好追求
        int yearGanIndex = (year - 4) % 10;
        if (yearGanIndex == 0) yearGanIndex = 10;
        TianGan yearGan = TianGan.fromIndex(yearGanIndex);
        
        int monthGanIndex;
        String ganName = yearGan.getName();
        if ("甲".equals(ganName) || "己".equals(ganName)) {
            monthGanIndex = (2 + month - 1) % 10; // 丙为2，正月为丙
        } else if ("乙".equals(ganName) || "庚".equals(ganName)) {
            monthGanIndex = (4 + month - 1) % 10; // 戊为4
        } else if ("丙".equals(ganName) || "辛".equals(ganName)) {
            monthGanIndex = (6 + month - 1) % 10; // 庚为6
        } else if ("丁".equals(ganName) || "壬".equals(ganName)) {
            monthGanIndex = (8 + month - 1) % 10; // 壬为8
        } else { // 戊或癸
            monthGanIndex = (0 + month - 1) % 10; // 甲为0
        }
        if (monthGanIndex == 0) monthGanIndex = 10;
        
        TianGan monthGan = TianGan.fromIndex(monthGanIndex);
        DiZhi monthZhi = DiZhi.fromIndex(month);
        
        this.ganZhi = monthGan.getName() + monthZhi.getName();
        this.tianGan = monthGan.getName();
        this.diZhi = monthZhi.getName();
        this.tianGanWuXing = monthGan.getWuXing();
        this.diZhiWuXing = monthZhi.getWuXing();
        
        // 计算天干十神
        this.tianGanShiShen = ShiShen.calculate(riZhuTianGan, this.tianGan).getName();
        
        // 设置藏干
        String[] cangGan = monthZhi.getCangGan();
        Collections.addAll(this.diZhiCangGan, cangGan);
        
        // 计算藏干十神
        for (String cg : cangGan) {
            this.diZhiShiShen.add(ShiShen.calculate(riZhuTianGan, cg).getName());
        }
    }

    /**
     * 转换为Map格式
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("月份", month);
        map.put("节气", solarTerm);
        map.put("节气日期", solarTermDate);
        map.put("干支", ganZhi);
        map.put("天干", tianGan);
        map.put("天干十神", tianGanShiShen);
        map.put("天干五行", tianGanWuXing);
        map.put("地支", diZhi);
        map.put("地支五行", diZhiWuXing);
        map.put("地支藏干", diZhiCangGan);
        map.put("地支十神", diZhiShiShen);
        return map;
    }

    // Getters and Setters
    public int getMonth() { return month; }
    public void setMonth(int month) { this.month = month; }
    public String getSolarTerm() { return solarTerm; }
    public void setSolarTerm(String solarTerm) { this.solarTerm = solarTerm; }
    public String getSolarTermDate() { return solarTermDate; }
    public void setSolarTermDate(String solarTermDate) { this.solarTermDate = solarTermDate; }
    public String getGanZhi() { return ganZhi; }
    public void setGanZhi(String ganZhi) { this.ganZhi = ganZhi; }
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
    public List<String> getDiZhiCangGan() { return diZhiCangGan; }
    public void setDiZhiCangGan(List<String> diZhiCangGan) { this.diZhiCangGan = diZhiCangGan; }
    public List<String> getDiZhiShiShen() { return diZhiShiShen; }
    public void setDiZhiShiShen(List<String> diZhiShiShen) { this.diZhiShiShen = diZhiShiShen; }
}

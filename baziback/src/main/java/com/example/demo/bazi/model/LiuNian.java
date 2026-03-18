package com.example.demo.bazi.model;

import com.example.demo.bazi.constants.*;
import java.util.*;

/**
 * 流年模型
 */
public class LiuNian {
    private int year;                  // 年份
    private int age;                   // 年龄
    private String ganZhi;             // 干支
    private String tianGan;            // 天干
    private String tianGanShiShen;     // 天干十神
    private String tianGanWuXing;      // 天干五行
    private String diZhi;              // 地支
    private String diZhiWuXing;        // 地支五行
    private List<String> diZhiCangGan; // 地支藏干
    private List<String> diZhiShiShen; // 地支十神（藏干对应的十神）
    private String naYin;              // 纳音

    public LiuNian() {
        this.diZhiCangGan = new ArrayList<>();
        this.diZhiShiShen = new ArrayList<>();
    }

    public LiuNian(int year, int age, String riZhuTianGan) {
        this();
        this.year = year;
        this.age = age;
        
        // 计算流年干支（年份对应的天干地支）
        int ganIndex = (year - 4) % 10;
        int zhiIndex = (year - 4) % 12;
        
        TianGan gan = TianGan.fromIndex(ganIndex == 0 ? 10 : ganIndex);
        DiZhi zhi = DiZhi.fromIndex(zhiIndex == 0 ? 12 : zhiIndex);
        
        this.ganZhi = gan.getName() + zhi.getName();
        this.tianGan = gan.getName();
        this.diZhi = zhi.getName();
        this.tianGanWuXing = gan.getWuXing();
        this.diZhiWuXing = zhi.getWuXing();
        this.naYin = NaYin.getNaYin(this.ganZhi);
        
        // 计算天干十神
        this.tianGanShiShen = ShiShen.calculate(riZhuTianGan, this.tianGan).getName();
        
        // 设置藏干
        String[] cangGan = zhi.getCangGan();
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
        map.put("年份", year);
        map.put("年龄", age + "岁");
        map.put("干支", ganZhi);
        map.put("天干", tianGan);
        map.put("天干十神", tianGanShiShen);
        map.put("天干五行", tianGanWuXing);
        map.put("地支", diZhi);
        map.put("地支五行", diZhiWuXing);
        map.put("地支藏干", diZhiCangGan);
        map.put("地支十神", diZhiShiShen);
        map.put("纳音", naYin);
        return map;
    }

    // Getters and Setters
    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
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
    public String getNaYin() { return naYin; }
    public void setNaYin(String naYin) { this.naYin = naYin; }
}

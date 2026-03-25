package com.example.demo.bazi.model;

import com.example.demo.bazi.constants.*;
import java.util.*;

/**
 * 柱模型 - 表示年/月/日/时柱
 */
public class Pillar {
    private String name;           // 柱名称：年/月/日/时
    private String tianGan;        // 天干
    private String diZhi;          // 地支
    private String tianGanShiShen; // 天干十神
    private List<String> diZhiCangGan;    // 地支藏干
    private List<String> diZhiShiShen;    // 地支十神（藏干对应的十神）
    private String tianGanWuXing;  // 天干五行
    private String diZhiWuXing;    // 地支五行
    private String naYin;          // 纳音
    private String xingYun;        // 星运（十二长生）
    private String ziZuo;          // 自坐
    private String kongWang;       // 空亡
    private String xunShou;        // 旬首
    private List<String> shenSha;  // 神煞

    public Pillar() {
        this.diZhiCangGan = new ArrayList<>();
        this.diZhiShiShen = new ArrayList<>();
        this.shenSha = new ArrayList<>();
    }

    public Pillar(String name, String tianGan, String diZhi) {
        this();
        this.name = name;
        this.tianGan = tianGan;
        this.diZhi = diZhi;
        this.tianGanWuXing = TianGan.fromName(tianGan).getWuXing();
        this.diZhiWuXing = DiZhi.fromName(diZhi).getWuXing();
        this.naYin = NaYin.getNaYin(tianGan + diZhi);
        
        // 设置藏干
        DiZhi dz = DiZhi.fromName(diZhi);
        Collections.addAll(this.diZhiCangGan, dz.getCangGan());
        
        // 计算旬首
        this.xunShou = XunShou.getXunShou(tianGan + diZhi);
    }

    /**
     * 计算十神（需要日主天干）
     */
    public void calculateShiShen(String riZhuTianGan) {
        if (this.tianGan.equals(riZhuTianGan) && "日".equals(this.name)) {
            this.tianGanShiShen = "日主";
        } else {
            this.tianGanShiShen = ShiShen.calculate(riZhuTianGan, this.tianGan).getName();
        }
        
        // 计算藏干十神
        this.diZhiShiShen.clear();
        for (String cangGan : this.diZhiCangGan) {
            this.diZhiShiShen.add(ShiShen.calculate(riZhuTianGan, cangGan).getName());
        }
    }

    /**
     * 计算星运（十二长生）
     */
    public void calculateXingYun(String riZhuTianGan) {
        this.xingYun = ShiErChangSheng.calculate(riZhuTianGan, this.diZhi);
    }

    /**
     * 计算自坐
     */
    public void calculateZiZuo() {
        this.ziZuo = ShiErChangSheng.calculate(this.tianGan, this.diZhi);
    }

    /**
     * 获取干支组合
     */
    public String getGanZhi() {
        return tianGan + diZhi;
    }

    /**
     * 转换为Map格式（用于JSON输出）
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("天干", tianGan);
        map.put("天干十神", tianGanShiShen);
        map.put("地支", diZhi);
        map.put("地支藏干", diZhiCangGan);
        map.put("地支十神", diZhiShiShen);
        map.put("天干五行", tianGanWuXing);
        map.put("地支五行", diZhiWuXing);
        map.put("纳音", naYin);
        map.put("星运", xingYun);
        map.put("自坐", ziZuo);
        map.put("空亡", kongWang);
        map.put("旬首", xunShou);
        map.put("神煞", shenSha);
        return map;
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getTianGan() { return tianGan; }
    public void setTianGan(String tianGan) { this.tianGan = tianGan; }
    public String getDiZhi() { return diZhi; }
    public void setDiZhi(String diZhi) { this.diZhi = diZhi; }
    public String getTianGanShiShen() { return tianGanShiShen; }
    public void setTianGanShiShen(String tianGanShiShen) { this.tianGanShiShen = tianGanShiShen; }
    public List<String> getDiZhiCangGan() { return diZhiCangGan; }
    public void setDiZhiCangGan(List<String> diZhiCangGan) { this.diZhiCangGan = diZhiCangGan; }
    public List<String> getDiZhiShiShen() { return diZhiShiShen; }
    public void setDiZhiShiShen(List<String> diZhiShiShen) { this.diZhiShiShen = diZhiShiShen; }
    public String getTianGanWuXing() { return tianGanWuXing; }
    public void setTianGanWuXing(String tianGanWuXing) { this.tianGanWuXing = tianGanWuXing; }
    public String getDiZhiWuXing() { return diZhiWuXing; }
    public void setDiZhiWuXing(String diZhiWuXing) { this.diZhiWuXing = diZhiWuXing; }
    public String getNaYin() { return naYin; }
    public void setNaYin(String naYin) { this.naYin = naYin; }
    public String getXingYun() { return xingYun; }
    public void setXingYun(String xingYun) { this.xingYun = xingYun; }
    public String getZiZuo() { return ziZuo; }
    public void setZiZuo(String ziZuo) { this.ziZuo = ziZuo; }
    public String getKongWang() { return kongWang; }
    public void setKongWang(String kongWang) { this.kongWang = kongWang; }
    public String getXunShou() { return xunShou; }
    public void setXunShou(String xunShou) { this.xunShou = xunShou; }
    public List<String> getShenSha() { return shenSha; }
    public void setShenSha(List<String> shenSha) { this.shenSha = shenSha; }
}

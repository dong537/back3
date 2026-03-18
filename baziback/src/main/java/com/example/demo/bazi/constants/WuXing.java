package com.example.demo.bazi.constants;

import java.util.*;

/**
 * 五行枚举
 */
public enum WuXing {
    MU("木", "东", "春", "青", 1),
    HUO("火", "南", "夏", "红", 2),
    TU("土", "中", "四季", "黄", 3),
    JIN("金", "西", "秋", "白", 4),
    SHUI("水", "北", "冬", "黑", 5);

    private final String name;
    private final String direction;
    private final String season;
    private final String color;
    private final int index;

    WuXing(String name, String direction, String season, String color, int index) {
        this.name = name;
        this.direction = direction;
        this.season = season;
        this.color = color;
        this.index = index;
    }

    public String getName() { return name; }
    public String getDirection() { return direction; }
    public String getSeason() { return season; }
    public String getColor() { return color; }
    public int getIndex() { return index; }

    public static WuXing fromName(String name) {
        for (WuXing wx : values()) {
            if (wx.name.equals(name)) return wx;
        }
        throw new IllegalArgumentException("无效五行: " + name);
    }

    /**
     * 获取我生的五行（食伤）
     */
    public WuXing getSheng() {
        switch (this) {
            case MU: return HUO;   // 木生火
            case HUO: return TU;   // 火生土
            case TU: return JIN;   // 土生金
            case JIN: return SHUI; // 金生水
            case SHUI: return MU;  // 水生木
            default: return null;
        }
    }

    /**
     * 获取生我的五行（印星）
     */
    public WuXing getBeSheng() {
        switch (this) {
            case MU: return SHUI;  // 水生木
            case HUO: return MU;   // 木生火
            case TU: return HUO;   // 火生土
            case JIN: return TU;   // 土生金
            case SHUI: return JIN; // 金生水
            default: return null;
        }
    }

    /**
     * 获取我克的五行（财星）
     */
    public WuXing getKe() {
        switch (this) {
            case MU: return TU;    // 木克土
            case HUO: return JIN;  // 火克金
            case TU: return SHUI;  // 土克水
            case JIN: return MU;   // 金克木
            case SHUI: return HUO; // 水克火
            default: return null;
        }
    }

    /**
     * 获取克我的五行（官杀）
     */
    public WuXing getBeKe() {
        switch (this) {
            case MU: return JIN;   // 金克木
            case HUO: return SHUI; // 水克火
            case TU: return MU;    // 木克土
            case JIN: return HUO;  // 火克金
            case SHUI: return TU;  // 土克水
            default: return null;
        }
    }

    /**
     * 判断与另一五行的关系
     */
    public String getRelation(WuXing other) {
        if (this == other) return "比和";
        if (this.getSheng() == other) return "我生";
        if (this.getBeSheng() == other) return "生我";
        if (this.getKe() == other) return "我克";
        if (this.getBeKe() == other) return "克我";
        return "未知";
    }

    /**
     * 获取五行对应的库
     */
    public String getKu() {
        switch (this) {
            case MU: return "未";  // 木库在未
            case HUO: return "戌"; // 火库在戌
            case TU: return "戌";  // 土库在戌
            case JIN: return "丑"; // 金库在丑
            case SHUI: return "辰"; // 水库在辰
            default: return null;
        }
    }

    /**
     * 获取五行的旺相休囚死状态
     */
    public static Map<String, Map<String, String>> getWangXiangXiuQiuSi() {
        Map<String, Map<String, String>> result = new HashMap<>();
        
        // 春季
        Map<String, String> spring = new HashMap<>();
        spring.put("木", "旺"); spring.put("火", "相"); spring.put("水", "休");
        spring.put("金", "囚"); spring.put("土", "死");
        result.put("春", spring);
        
        // 夏季
        Map<String, String> summer = new HashMap<>();
        summer.put("火", "旺"); summer.put("土", "相"); summer.put("木", "休");
        summer.put("水", "囚"); summer.put("金", "死");
        result.put("夏", summer);
        
        // 秋季
        Map<String, String> autumn = new HashMap<>();
        autumn.put("金", "旺"); autumn.put("水", "相"); autumn.put("土", "休");
        autumn.put("火", "囚"); autumn.put("木", "死");
        result.put("秋", autumn);
        
        // 冬季
        Map<String, String> winter = new HashMap<>();
        winter.put("水", "旺"); winter.put("木", "相"); winter.put("金", "休");
        winter.put("土", "囚"); winter.put("火", "死");
        result.put("冬", winter);
        
        return result;
    }
}

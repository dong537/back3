package com.example.demo.bazi.constants;

import java.util.*;

/**
 * 地支枚举
 */
public enum DiZhi {
    ZI("子", "水", true, 1, new String[]{"癸"}, "鼠"),
    CHOU("丑", "土", false, 2, new String[]{"己", "癸", "辛"}, "牛"),
    YIN("寅", "木", true, 3, new String[]{"甲", "丙", "戊"}, "虎"),
    MAO("卯", "木", false, 4, new String[]{"乙"}, "兔"),
    CHEN("辰", "土", true, 5, new String[]{"戊", "乙", "癸"}, "龙"),
    SI("巳", "火", false, 6, new String[]{"丙", "庚", "戊"}, "蛇"),
    WU("午", "火", true, 7, new String[]{"丁", "己"}, "马"),
    WEI("未", "土", false, 8, new String[]{"己", "丁", "乙"}, "羊"),
    SHEN("申", "金", true, 9, new String[]{"庚", "壬", "戊"}, "猴"),
    YOU("酉", "金", false, 10, new String[]{"辛"}, "鸡"),
    XU("戌", "土", true, 11, new String[]{"戊", "辛", "丁"}, "狗"),
    HAI("亥", "水", false, 12, new String[]{"壬", "甲"}, "猪");

    private final String name;
    private final String wuXing;
    private final boolean yang;
    private final int index;
    private final String[] cangGan; // 藏干：本气、中气、余气
    private final String shengXiao;

    DiZhi(String name, String wuXing, boolean yang, int index, String[] cangGan, String shengXiao) {
        this.name = name;
        this.wuXing = wuXing;
        this.yang = yang;
        this.index = index;
        this.cangGan = cangGan;
        this.shengXiao = shengXiao;
    }

    public String getName() { return name; }
    public String getWuXing() { return wuXing; }
    public boolean isYang() { return yang; }
    public int getIndex() { return index; }
    public String[] getCangGan() { return cangGan; }
    public String getShengXiao() { return shengXiao; }

    /**
     * 获取本气（主气）
     */
    public String getBenQi() {
        return cangGan[0];
    }

    /**
     * 获取中气
     */
    public String getZhongQi() {
        return cangGan.length > 1 ? cangGan[1] : null;
    }

    /**
     * 获取余气
     */
    public String getYuQi() {
        return cangGan.length > 2 ? cangGan[2] : null;
    }

    public static DiZhi fromName(String name) {
        for (DiZhi dz : values()) {
            if (dz.name.equals(name)) return dz;
        }
        throw new IllegalArgumentException("无效地支: " + name);
    }

    public static DiZhi fromIndex(int index) {
        int normalizedIndex = ((index - 1) % 12) + 1;
        if (normalizedIndex <= 0) normalizedIndex += 12;
        for (DiZhi dz : values()) {
            if (dz.index == normalizedIndex) return dz;
        }
        throw new IllegalArgumentException("无效索引: " + index);
    }

    /**
     * 获取地支六合
     */
    public DiZhi getLiuHe() {
        switch (this) {
            case ZI: return CHOU;   // 子丑合土
            case CHOU: return ZI;
            case YIN: return HAI;   // 寅亥合木
            case HAI: return YIN;
            case MAO: return XU;    // 卯戌合火
            case XU: return MAO;
            case CHEN: return YOU;  // 辰酉合金
            case YOU: return CHEN;
            case SI: return SHEN;   // 巳申合水
            case SHEN: return SI;
            case WU: return WEI;    // 午未合火
            case WEI: return WU;
            default: return null;
        }
    }

    /**
     * 获取六合五行
     */
    public String getLiuHeWuXing() {
        switch (this) {
            case ZI: case CHOU: return "土";
            case YIN: case HAI: return "木";
            case MAO: case XU: return "火";
            case CHEN: case YOU: return "金";
            case SI: case SHEN: return "水";
            case WU: case WEI: return "火";
            default: return null;
        }
    }

    /**
     * 获取地支六冲
     */
    public DiZhi getChong() {
        int chongIndex = (this.index + 6 - 1) % 12 + 1;
        return fromIndex(chongIndex);
    }

    /**
     * 获取三合局
     */
    public static Map<String, List<DiZhi>> getSanHe() {
        Map<String, List<DiZhi>> sanHe = new HashMap<>();
        sanHe.put("水", Arrays.asList(SHEN, ZI, CHEN));  // 申子辰三合水
        sanHe.put("木", Arrays.asList(HAI, MAO, WEI));   // 亥卯未三合木
        sanHe.put("火", Arrays.asList(YIN, WU, XU));     // 寅午戌三合火
        sanHe.put("金", Arrays.asList(SI, YOU, CHOU));   // 巳酉丑三合金
        return sanHe;
    }

    /**
     * 获取三会局
     */
    public static Map<String, List<DiZhi>> getSanHui() {
        Map<String, List<DiZhi>> sanHui = new HashMap<>();
        sanHui.put("水", Arrays.asList(HAI, ZI, CHOU));   // 亥子丑三会水
        sanHui.put("木", Arrays.asList(YIN, MAO, CHEN));  // 寅卯辰三会木
        sanHui.put("火", Arrays.asList(SI, WU, WEI));     // 巳午未三会火
        sanHui.put("金", Arrays.asList(SHEN, YOU, XU));   // 申酉戌三会金
        return sanHui;
    }

    /**
     * 获取半合
     */
    public List<Map<String, Object>> getBanHe(DiZhi other) {
        List<Map<String, Object>> result = new ArrayList<>();
        Map<String, List<DiZhi>> sanHe = getSanHe();
        for (Map.Entry<String, List<DiZhi>> entry : sanHe.entrySet()) {
            List<DiZhi> group = entry.getValue();
            if (group.contains(this) && group.contains(other) && !this.equals(other)) {
                Map<String, Object> banHe = new HashMap<>();
                banHe.put("元素", entry.getKey());
                banHe.put("地支", Arrays.asList(this.name, other.name));
                result.add(banHe);
            }
        }
        return result;
    }
}

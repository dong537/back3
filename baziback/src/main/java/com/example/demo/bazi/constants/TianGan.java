package com.example.demo.bazi.constants;

/**
 * 天干枚举
 */
public enum TianGan {
    JIA("甲", "木", true, 1),
    YI("乙", "木", false, 2),
    BING("丙", "火", true, 3),
    DING("丁", "火", false, 4),
    WU("戊", "土", true, 5),
    JI("己", "土", false, 6),
    GENG("庚", "金", true, 7),
    XIN("辛", "金", false, 8),
    REN("壬", "水", true, 9),
    GUI("癸", "水", false, 10);

    private final String name;
    private final String wuXing;
    private final boolean yang;
    private final int index;

    TianGan(String name, String wuXing, boolean yang, int index) {
        this.name = name;
        this.wuXing = wuXing;
        this.yang = yang;
        this.index = index;
    }

    public String getName() { return name; }
    public String getWuXing() { return wuXing; }
    public boolean isYang() { return yang; }
    public int getIndex() { return index; }

    public static TianGan fromName(String name) {
        for (TianGan tg : values()) {
            if (tg.name.equals(name)) return tg;
        }
        throw new IllegalArgumentException("无效天干: " + name);
    }

    public static TianGan fromIndex(int index) {
        int normalizedIndex = ((index - 1) % 10) + 1;
        if (normalizedIndex <= 0) normalizedIndex += 10;
        for (TianGan tg : values()) {
            if (tg.index == normalizedIndex) return tg;
        }
        throw new IllegalArgumentException("无效索引: " + index);
    }

    /**
     * 获取天干相合
     */
    public TianGan getHe() {
        switch (this) {
            case JIA: return JI;   // 甲己合土
            case JI: return JIA;
            case YI: return GENG;  // 乙庚合金
            case GENG: return YI;
            case BING: return XIN; // 丙辛合水
            case XIN: return BING;
            case DING: return REN; // 丁壬合木
            case REN: return DING;
            case WU: return GUI;   // 戊癸合火
            case GUI: return WU;
            default: return null;
        }
    }

    /**
     * 获取天干合化五行
     */
    public String getHeWuXing() {
        switch (this) {
            case JIA: case JI: return "土";
            case YI: case GENG: return "金";
            case BING: case XIN: return "水";
            case DING: case REN: return "木";
            case WU: case GUI: return "火";
            default: return null;
        }
    }

    /**
     * 获取天干相冲（相克）
     */
    public TianGan getChong() {
        int chongIndex = (this.index + 4) % 10;
        if (chongIndex == 0) chongIndex = 10;
        return fromIndex(chongIndex);
    }
}

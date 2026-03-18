package com.example.demo.bazi.constants;

/**
 * 十神枚举
 */
public enum ShiShen {
    BI_JIAN("比肩", "比和", true, "兄弟姐妹、朋友、同辈"),
    JIE_CAI("劫财", "比和", false, "兄弟姐妹、竞争者"),
    SHI_SHEN("食神", "我生", true, "子女、才华、口福"),
    SHANG_GUAN("伤官", "我生", false, "子女、叛逆、才华"),
    ZHENG_CAI("正财", "我克", false, "妻子、稳定财富"),
    PIAN_CAI("偏财", "我克", true, "父亲、意外之财、偏房"),
    ZHENG_GUAN("正官", "克我", false, "丈夫、事业、名誉"),
    QI_SHA("七杀", "克我", true, "权力、压力、小人"),
    ZHENG_YIN("正印", "生我", false, "母亲、学业、贵人"),
    PIAN_YIN("偏印", "生我", true, "继母、偏学、孤独"),
    RI_ZHU("日主", "自身", true, "自身");

    private final String name;
    private final String relation;
    private final boolean yang;
    private final String meaning;

    ShiShen(String name, String relation, boolean yang, String meaning) {
        this.name = name;
        this.relation = relation;
        this.yang = yang;
        this.meaning = meaning;
    }

    public String getName() { return name; }
    public String getRelation() { return relation; }
    public boolean isYang() { return yang; }
    public String getMeaning() { return meaning; }

    public static ShiShen fromName(String name) {
        for (ShiShen ss : values()) {
            if (ss.name.equals(name)) return ss;
        }
        throw new IllegalArgumentException("无效十神: " + name);
    }

    /**
     * 根据日主和目标天干计算十神
     */
    public static ShiShen calculate(TianGan riZhu, TianGan target) {
        if (riZhu == target) return RI_ZHU;
        
        WuXing riWuXing = WuXing.fromName(riZhu.getWuXing());
        WuXing targetWuXing = WuXing.fromName(target.getWuXing());
        boolean sameYinYang = riZhu.isYang() == target.isYang();

        // 比和 - 同五行
        if (riWuXing == targetWuXing) {
            return sameYinYang ? BI_JIAN : JIE_CAI;
        }
        // 我生 - 食伤
        if (riWuXing.getSheng() == targetWuXing) {
            return sameYinYang ? SHI_SHEN : SHANG_GUAN;
        }
        // 我克 - 财星
        if (riWuXing.getKe() == targetWuXing) {
            return sameYinYang ? PIAN_CAI : ZHENG_CAI;
        }
        // 克我 - 官杀
        if (riWuXing.getBeKe() == targetWuXing) {
            return sameYinYang ? QI_SHA : ZHENG_GUAN;
        }
        // 生我 - 印星
        if (riWuXing.getBeSheng() == targetWuXing) {
            return sameYinYang ? PIAN_YIN : ZHENG_YIN;
        }
        
        throw new IllegalArgumentException("无法计算十神关系");
    }

    /**
     * 根据日主和目标天干名称计算十神
     */
    public static ShiShen calculate(String riZhuName, String targetName) {
        return calculate(TianGan.fromName(riZhuName), TianGan.fromName(targetName));
    }

    /**
     * 判断是否为财星
     */
    public boolean isCai() {
        return this == ZHENG_CAI || this == PIAN_CAI;
    }

    /**
     * 判断是否为官杀
     */
    public boolean isGuan() {
        return this == ZHENG_GUAN || this == QI_SHA;
    }

    /**
     * 判断是否为印星
     */
    public boolean isYin() {
        return this == ZHENG_YIN || this == PIAN_YIN;
    }

    /**
     * 判断是否为比劫
     */
    public boolean isBiJie() {
        return this == BI_JIAN || this == JIE_CAI;
    }

    /**
     * 判断是否为食伤
     */
    public boolean isShiShang() {
        return this == SHI_SHEN || this == SHANG_GUAN;
    }
}

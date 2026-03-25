package com.example.demo.bazi.constants;

import java.util.*;

/**
 * 空亡常量类
 */
public class KongWang {
    
    // 空亡表：以日柱天干地支组合确定空亡地支
    private static final Map<String, String[]> KONG_WANG_MAP = new HashMap<>();
    
    static {
        // 甲子旬：甲子、乙丑、丙寅、丁卯、戊辰、己巳、庚午、辛未、壬申、癸酉 -> 空亡：戌亥
        KONG_WANG_MAP.put("甲子", new String[]{"戌", "亥"});
        KONG_WANG_MAP.put("乙丑", new String[]{"戌", "亥"});
        KONG_WANG_MAP.put("丙寅", new String[]{"戌", "亥"});
        KONG_WANG_MAP.put("丁卯", new String[]{"戌", "亥"});
        KONG_WANG_MAP.put("戊辰", new String[]{"戌", "亥"});
        KONG_WANG_MAP.put("己巳", new String[]{"戌", "亥"});
        KONG_WANG_MAP.put("庚午", new String[]{"戌", "亥"});
        KONG_WANG_MAP.put("辛未", new String[]{"戌", "亥"});
        KONG_WANG_MAP.put("壬申", new String[]{"戌", "亥"});
        KONG_WANG_MAP.put("癸酉", new String[]{"戌", "亥"});
        
        // 甲戌旬：空亡：申酉
        KONG_WANG_MAP.put("甲戌", new String[]{"申", "酉"});
        KONG_WANG_MAP.put("乙亥", new String[]{"申", "酉"});
        KONG_WANG_MAP.put("丙子", new String[]{"申", "酉"});
        KONG_WANG_MAP.put("丁丑", new String[]{"申", "酉"});
        KONG_WANG_MAP.put("戊寅", new String[]{"申", "酉"});
        KONG_WANG_MAP.put("己卯", new String[]{"申", "酉"});
        KONG_WANG_MAP.put("庚辰", new String[]{"申", "酉"});
        KONG_WANG_MAP.put("辛巳", new String[]{"申", "酉"});
        KONG_WANG_MAP.put("壬午", new String[]{"申", "酉"});
        KONG_WANG_MAP.put("癸未", new String[]{"申", "酉"});
        
        // 甲申旬：空亡：午未
        KONG_WANG_MAP.put("甲申", new String[]{"午", "未"});
        KONG_WANG_MAP.put("乙酉", new String[]{"午", "未"});
        KONG_WANG_MAP.put("丙戌", new String[]{"午", "未"});
        KONG_WANG_MAP.put("丁亥", new String[]{"午", "未"});
        KONG_WANG_MAP.put("戊子", new String[]{"午", "未"});
        KONG_WANG_MAP.put("己丑", new String[]{"午", "未"});
        KONG_WANG_MAP.put("庚寅", new String[]{"午", "未"});
        KONG_WANG_MAP.put("辛卯", new String[]{"午", "未"});
        KONG_WANG_MAP.put("壬辰", new String[]{"午", "未"});
        KONG_WANG_MAP.put("癸巳", new String[]{"午", "未"});
        
        // 甲午旬：空亡：辰巳
        KONG_WANG_MAP.put("甲午", new String[]{"辰", "巳"});
        KONG_WANG_MAP.put("乙未", new String[]{"辰", "巳"});
        KONG_WANG_MAP.put("丙申", new String[]{"辰", "巳"});
        KONG_WANG_MAP.put("丁酉", new String[]{"辰", "巳"});
        KONG_WANG_MAP.put("戊戌", new String[]{"辰", "巳"});
        KONG_WANG_MAP.put("己亥", new String[]{"辰", "巳"});
        KONG_WANG_MAP.put("庚子", new String[]{"辰", "巳"});
        KONG_WANG_MAP.put("辛丑", new String[]{"辰", "巳"});
        KONG_WANG_MAP.put("壬寅", new String[]{"辰", "巳"});
        KONG_WANG_MAP.put("癸卯", new String[]{"辰", "巳"});
        
        // 甲辰旬：空亡：寅卯
        KONG_WANG_MAP.put("甲辰", new String[]{"寅", "卯"});
        KONG_WANG_MAP.put("乙巳", new String[]{"寅", "卯"});
        KONG_WANG_MAP.put("丙午", new String[]{"寅", "卯"});
        KONG_WANG_MAP.put("丁未", new String[]{"寅", "卯"});
        KONG_WANG_MAP.put("戊申", new String[]{"寅", "卯"});
        KONG_WANG_MAP.put("己酉", new String[]{"寅", "卯"});
        KONG_WANG_MAP.put("庚戌", new String[]{"寅", "卯"});
        KONG_WANG_MAP.put("辛亥", new String[]{"寅", "卯"});
        KONG_WANG_MAP.put("壬子", new String[]{"寅", "卯"});
        KONG_WANG_MAP.put("癸丑", new String[]{"寅", "卯"});
        
        // 甲寅旬：空亡：子丑
        KONG_WANG_MAP.put("甲寅", new String[]{"子", "丑"});
        KONG_WANG_MAP.put("乙卯", new String[]{"子", "丑"});
        KONG_WANG_MAP.put("丙辰", new String[]{"子", "丑"});
        KONG_WANG_MAP.put("丁巳", new String[]{"子", "丑"});
        KONG_WANG_MAP.put("戊午", new String[]{"子", "丑"});
        KONG_WANG_MAP.put("己未", new String[]{"子", "丑"});
        KONG_WANG_MAP.put("庚申", new String[]{"子", "丑"});
        KONG_WANG_MAP.put("辛酉", new String[]{"子", "丑"});
        KONG_WANG_MAP.put("壬戌", new String[]{"子", "丑"});
        KONG_WANG_MAP.put("癸亥", new String[]{"子", "丑"});
    }

    /**
     * 获取某柱的空亡地支
     */
    public static String[] getKongWang(String ganZhi) {
        return KONG_WANG_MAP.getOrDefault(ganZhi, new String[]{});
    }

    /**
     * 获取某柱的空亡地支（拼接字符串）
     */
    public static String getKongWangStr(String ganZhi) {
        String[] kw = getKongWang(ganZhi);
        if (kw.length == 2) {
            return kw[0] + kw[1];
        }
        return "";
    }

    /**
     * 判断某地支是否在某柱空亡中
     */
    public static boolean isKongWang(String ganZhi, String diZhi) {
        String[] kw = getKongWang(ganZhi);
        for (String k : kw) {
            if (k.equals(diZhi)) return true;
        }
        return false;
    }
}

package com.example.demo.bazi.constants;

import java.util.*;

/**
 * 纳音常量类 - 六十甲子纳音
 */
public class NaYin {
    
    private static final Map<String, String> NAYIN_MAP = new HashMap<>();
    
    static {
        // 甲子、乙丑
        NAYIN_MAP.put("甲子", "海中金");
        NAYIN_MAP.put("乙丑", "海中金");
        // 丙寅、丁卯
        NAYIN_MAP.put("丙寅", "炉中火");
        NAYIN_MAP.put("丁卯", "炉中火");
        // 戊辰、己巳
        NAYIN_MAP.put("戊辰", "大林木");
        NAYIN_MAP.put("己巳", "大林木");
        // 庚午、辛未
        NAYIN_MAP.put("庚午", "路旁土");
        NAYIN_MAP.put("辛未", "路旁土");
        // 壬申、癸酉
        NAYIN_MAP.put("壬申", "剑锋金");
        NAYIN_MAP.put("癸酉", "剑锋金");
        // 甲戌、乙亥
        NAYIN_MAP.put("甲戌", "山头火");
        NAYIN_MAP.put("乙亥", "山头火");
        // 丙子、丁丑
        NAYIN_MAP.put("丙子", "涧下水");
        NAYIN_MAP.put("丁丑", "涧下水");
        // 戊寅、己卯
        NAYIN_MAP.put("戊寅", "城头土");
        NAYIN_MAP.put("己卯", "城头土");
        // 庚辰、辛巳
        NAYIN_MAP.put("庚辰", "白蜡金");
        NAYIN_MAP.put("辛巳", "白蜡金");
        // 壬午、癸未
        NAYIN_MAP.put("壬午", "杨柳木");
        NAYIN_MAP.put("癸未", "杨柳木");
        // 甲申、乙酉
        NAYIN_MAP.put("甲申", "泉中水");
        NAYIN_MAP.put("乙酉", "泉中水");
        // 丙戌、丁亥
        NAYIN_MAP.put("丙戌", "屋上土");
        NAYIN_MAP.put("丁亥", "屋上土");
        // 戊子、己丑
        NAYIN_MAP.put("戊子", "霹雳火");
        NAYIN_MAP.put("己丑", "霹雳火");
        // 庚寅、辛卯
        NAYIN_MAP.put("庚寅", "松柏木");
        NAYIN_MAP.put("辛卯", "松柏木");
        // 壬辰、癸巳
        NAYIN_MAP.put("壬辰", "长流水");
        NAYIN_MAP.put("癸巳", "长流水");
        // 甲午、乙未
        NAYIN_MAP.put("甲午", "沙中金");
        NAYIN_MAP.put("乙未", "沙中金");
        // 丙申、丁酉
        NAYIN_MAP.put("丙申", "山下火");
        NAYIN_MAP.put("丁酉", "山下火");
        // 戊戌、己亥
        NAYIN_MAP.put("戊戌", "平地木");
        NAYIN_MAP.put("己亥", "平地木");
        // 庚子、辛丑
        NAYIN_MAP.put("庚子", "壁上土");
        NAYIN_MAP.put("辛丑", "壁上土");
        // 壬寅、癸卯
        NAYIN_MAP.put("壬寅", "金箔金");
        NAYIN_MAP.put("癸卯", "金箔金");
        // 甲辰、乙巳
        NAYIN_MAP.put("甲辰", "覆灯火");
        NAYIN_MAP.put("乙巳", "覆灯火");
        // 丙午、丁未
        NAYIN_MAP.put("丙午", "天河水");
        NAYIN_MAP.put("丁未", "天河水");
        // 戊申、己酉
        NAYIN_MAP.put("戊申", "大驿土");
        NAYIN_MAP.put("己酉", "大驿土");
        // 庚戌、辛亥
        NAYIN_MAP.put("庚戌", "钗钏金");
        NAYIN_MAP.put("辛亥", "钗钏金");
        // 壬子、癸丑
        NAYIN_MAP.put("壬子", "桑柘木");
        NAYIN_MAP.put("癸丑", "桑柘木");
        // 甲寅、乙卯
        NAYIN_MAP.put("甲寅", "大溪水");
        NAYIN_MAP.put("乙卯", "大溪水");
        // 丙辰、丁巳
        NAYIN_MAP.put("丙辰", "沙中土");
        NAYIN_MAP.put("丁巳", "沙中土");
        // 戊午、己未
        NAYIN_MAP.put("戊午", "天上火");
        NAYIN_MAP.put("己未", "天上火");
        // 庚申、辛酉
        NAYIN_MAP.put("庚申", "石榴木");
        NAYIN_MAP.put("辛酉", "石榴木");
        // 壬戌、癸亥
        NAYIN_MAP.put("壬戌", "大海水");
        NAYIN_MAP.put("癸亥", "大海水");
    }

    /**
     * 获取纳音
     */
    public static String getNaYin(String ganZhi) {
        return NAYIN_MAP.getOrDefault(ganZhi, "未知纳音");
    }

    /**
     * 获取纳音
     */
    public static String getNaYin(String tianGan, String diZhi) {
        return getNaYin(tianGan + diZhi);
    }

    /**
     * 获取纳音五行
     */
    public static String getNaYinWuXing(String ganZhi) {
        String nayin = getNaYin(ganZhi);
        if (nayin.contains("金")) return "金";
        if (nayin.contains("木")) return "木";
        if (nayin.contains("水")) return "水";
        if (nayin.contains("火")) return "火";
        if (nayin.contains("土")) return "土";
        return "未知";
    }

    public static Map<String, String> getAllNaYin() {
        return new HashMap<>(NAYIN_MAP);
    }
}

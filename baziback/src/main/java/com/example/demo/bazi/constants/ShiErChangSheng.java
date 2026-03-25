package com.example.demo.bazi.constants;

import java.util.*;

/**
 * 十二长生常量类
 */
public class ShiErChangSheng {
    
    // 十二长生名称
    public static final String[] NAMES = {
        "长生", "沐浴", "冠带", "临官", "帝旺", "衰", 
        "病", "死", "墓", "绝", "胎", "养"
    };

    // 阳干起始地支索引 (从长生位开始)
    private static final Map<String, Integer> YANG_GAN_START = new HashMap<>();
    // 阴干起始地支索引 (从长生位开始)
    private static final Map<String, Integer> YIN_GAN_START = new HashMap<>();
    
    static {
        // 阳干长生位置 (地支索引，1-12)
        YANG_GAN_START.put("甲", 12); // 甲木长生在亥
        YANG_GAN_START.put("丙", 3);  // 丙火长生在寅
        YANG_GAN_START.put("戊", 3);  // 戊土长生在寅
        YANG_GAN_START.put("庚", 6);  // 庚金长生在巳
        YANG_GAN_START.put("壬", 9);  // 壬水长生在申
        
        // 阴干长生位置 (逆行)
        YIN_GAN_START.put("乙", 7);   // 乙木长生在午
        YIN_GAN_START.put("丁", 10);  // 丁火长生在酉
        YIN_GAN_START.put("己", 10);  // 己土长生在酉
        YIN_GAN_START.put("辛", 1);   // 辛金长生在子
        YIN_GAN_START.put("癸", 4);   // 癸水长生在卯
    }

    /**
     * 计算天干在某地支的十二长生状态
     */
    public static String calculate(String tianGan, String diZhi) {
        TianGan tg = TianGan.fromName(tianGan);
        DiZhi dz = DiZhi.fromName(diZhi);
        
        int startIndex;
        boolean isYang = tg.isYang();
        
        if (isYang) {
            startIndex = YANG_GAN_START.getOrDefault(tianGan, 1);
        } else {
            startIndex = YIN_GAN_START.getOrDefault(tianGan, 1);
        }
        
        int diZhiIndex = dz.getIndex();
        int position;
        
        if (isYang) {
            // 阳干顺行
            position = (diZhiIndex - startIndex + 12) % 12;
        } else {
            // 阴干逆行
            position = (startIndex - diZhiIndex + 12) % 12;
        }
        
        return NAMES[position];
    }

    /**
     * 计算天干在某地支的十二长生状态（自坐）
     * 自坐指天干坐在其本支上的状态
     */
    public static String calculateZiZuo(String tianGan, String diZhi) {
        return calculate(tianGan, diZhi);
    }

    /**
     * 判断是否为长生、冠带、临官、帝旺（旺相状态）
     */
    public static boolean isWangXiang(String state) {
        return "长生".equals(state) || "冠带".equals(state) || 
               "临官".equals(state) || "帝旺".equals(state);
    }

    /**
     * 判断是否为衰、病、死、墓、绝（衰败状态）
     */
    public static boolean isShuaiBai(String state) {
        return "衰".equals(state) || "病".equals(state) || 
               "死".equals(state) || "墓".equals(state) || "绝".equals(state);
    }

    /**
     * 判断是否为胎、养（孕育状态）
     */
    public static boolean isYunYu(String state) {
        return "胎".equals(state) || "养".equals(state);
    }

    /**
     * 获取状态等级（用于评估）
     */
    public static int getStateLevel(String state) {
        switch (state) {
            case "帝旺": return 10;
            case "临官": return 9;
            case "冠带": return 8;
            case "长生": return 7;
            case "养": return 6;
            case "胎": return 5;
            case "沐浴": return 4;
            case "衰": return 3;
            case "病": return 2;
            case "死": return 1;
            case "墓": return 0;
            case "绝": return -1;
            default: return 0;
        }
    }
}

package com.example.demo.bazi.constants;

import java.util.*;

/**
 * 神煞常量类
 */
public class ShenSha {
    
    /**
     * 天乙贵人
     */
    public static List<String> getTianYiGuiRen(String riGan) {
        Map<String, List<String>> map = new HashMap<>();
        map.put("甲", Arrays.asList("丑", "未"));
        map.put("乙", Arrays.asList("子", "申"));
        map.put("丙", Arrays.asList("亥", "酉"));
        map.put("丁", Arrays.asList("亥", "酉"));
        map.put("戊", Arrays.asList("丑", "未"));
        map.put("己", Arrays.asList("子", "申"));
        map.put("庚", Arrays.asList("丑", "未"));
        map.put("辛", Arrays.asList("寅", "午"));
        map.put("壬", Arrays.asList("卯", "巳"));
        map.put("癸", Arrays.asList("卯", "巳"));
        return map.getOrDefault(riGan, new ArrayList<>());
    }

    /**
     * 文昌贵人
     */
    public static String getWenChangGuiRen(String riGan) {
        Map<String, String> map = new HashMap<>();
        map.put("甲", "巳"); map.put("乙", "午"); map.put("丙", "申");
        map.put("丁", "酉"); map.put("戊", "申"); map.put("己", "酉");
        map.put("庚", "亥"); map.put("辛", "子"); map.put("壬", "寅");
        map.put("癸", "卯");
        return map.get(riGan);
    }

    /**
     * 驿马
     */
    public static String getYiMa(String niZhi) {
        Map<String, String> map = new HashMap<>();
        map.put("申", "寅"); map.put("子", "寅"); map.put("辰", "寅");
        map.put("寅", "申"); map.put("午", "申"); map.put("戌", "申");
        map.put("巳", "亥"); map.put("酉", "亥"); map.put("丑", "亥");
        map.put("亥", "巳"); map.put("卯", "巳"); map.put("未", "巳");
        return map.get(niZhi);
    }

    /**
     * 桃花（咸池）
     */
    public static String getTaoHua(String niZhi) {
        Map<String, String> map = new HashMap<>();
        map.put("申", "酉"); map.put("子", "酉"); map.put("辰", "酉");
        map.put("寅", "卯"); map.put("午", "卯"); map.put("戌", "卯");
        map.put("巳", "午"); map.put("酉", "午"); map.put("丑", "午");
        map.put("亥", "子"); map.put("卯", "子"); map.put("未", "子");
        return map.get(niZhi);
    }

    /**
     * 华盖
     */
    public static String getHuaGai(String niZhi) {
        Map<String, String> map = new HashMap<>();
        map.put("申", "辰"); map.put("子", "辰"); map.put("辰", "辰");
        map.put("寅", "戌"); map.put("午", "戌"); map.put("戌", "戌");
        map.put("巳", "丑"); map.put("酉", "丑"); map.put("丑", "丑");
        map.put("亥", "未"); map.put("卯", "未"); map.put("未", "未");
        return map.get(niZhi);
    }

    /**
     * 将星
     */
    public static String getJiangXing(String niZhi) {
        Map<String, String> map = new HashMap<>();
        map.put("申", "子"); map.put("子", "子"); map.put("辰", "子");
        map.put("寅", "午"); map.put("午", "午"); map.put("戌", "午");
        map.put("巳", "酉"); map.put("酉", "酉"); map.put("丑", "酉");
        map.put("亥", "卯"); map.put("卯", "卯"); map.put("未", "卯");
        return map.get(niZhi);
    }

    /**
     * 天德贵人
     */
    public static String getTianDeGuiRen(String yueZhi) {
        Map<String, String> map = new HashMap<>();
        map.put("寅", "丁"); map.put("卯", "申"); map.put("辰", "壬");
        map.put("巳", "辛"); map.put("午", "亥"); map.put("未", "甲");
        map.put("申", "癸"); map.put("酉", "寅"); map.put("戌", "丙");
        map.put("亥", "乙"); map.put("子", "巳"); map.put("丑", "庚");
        return map.get(yueZhi);
    }

    /**
     * 月德贵人
     */
    public static String getYueDeGuiRen(String yueZhi) {
        Map<String, String> map = new HashMap<>();
        map.put("寅", "丙"); map.put("卯", "甲"); map.put("辰", "壬");
        map.put("巳", "庚"); map.put("午", "丙"); map.put("未", "甲");
        map.put("申", "壬"); map.put("酉", "庚"); map.put("戌", "丙");
        map.put("亥", "甲"); map.put("子", "壬"); map.put("丑", "庚");
        return map.get(yueZhi);
    }

    /**
     * 天德合
     */
    public static String getTianDeHe(String yueZhi) {
        String tianDe = getTianDeGuiRen(yueZhi);
        if (tianDe == null) return null;
        try {
            TianGan tg = TianGan.fromName(tianDe);
            return tg.getHe().getName();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 月德合
     */
    public static String getYueDeHe(String yueZhi) {
        String yueDe = getYueDeGuiRen(yueZhi);
        if (yueDe == null) return null;
        try {
            TianGan tg = TianGan.fromName(yueDe);
            return tg.getHe().getName();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 羊刃
     */
    public static String getYangRen(String riGan) {
        Map<String, String> map = new HashMap<>();
        map.put("甲", "卯"); map.put("乙", "辰"); map.put("丙", "午");
        map.put("丁", "未"); map.put("戊", "午"); map.put("己", "未");
        map.put("庚", "酉"); map.put("辛", "戌"); map.put("壬", "子");
        map.put("癸", "丑");
        return map.get(riGan);
    }

    /**
     * 飞刃
     */
    public static String getFeiRen(String riGan) {
        Map<String, String> map = new HashMap<>();
        map.put("甲", "酉"); map.put("乙", "申"); map.put("丙", "子");
        map.put("丁", "亥"); map.put("戊", "子"); map.put("己", "亥");
        map.put("庚", "卯"); map.put("辛", "寅"); map.put("壬", "午");
        map.put("癸", "巳");
        return map.get(riGan);
    }

    /**
     * 金舆
     */
    public static String getJinYu(String riGan) {
        Map<String, String> map = new HashMap<>();
        map.put("甲", "辰"); map.put("乙", "巳"); map.put("丙", "未");
        map.put("丁", "申"); map.put("戊", "未"); map.put("己", "申");
        map.put("庚", "戌"); map.put("辛", "亥"); map.put("壬", "丑");
        map.put("癸", "寅");
        return map.get(riGan);
    }

    /**
     * 福星贵人
     */
    public static String getFuXingGuiRen(String riGan) {
        Map<String, String> map = new HashMap<>();
        map.put("甲", "寅"); map.put("乙", "丑"); map.put("丙", "子");
        map.put("丁", "亥"); map.put("戊", "子"); map.put("己", "亥");
        map.put("庚", "午"); map.put("辛", "巳"); map.put("壬", "辰");
        map.put("癸", "卯");
        return map.get(riGan);
    }

    /**
     * 天官贵人
     */
    public static String getTianGuanGuiRen(String riGan) {
        Map<String, String> map = new HashMap<>();
        map.put("甲", "未"); map.put("乙", "辰"); map.put("丙", "巳");
        map.put("丁", "寅"); map.put("戊", "巳"); map.put("己", "寅");
        map.put("庚", "亥"); map.put("辛", "酉"); map.put("壬", "申");
        map.put("癸", "卯");
        return map.get(riGan);
    }

    /**
     * 德秀贵人 - 检查是否为德秀贵人
     */
    public static boolean isDeXiuGuiRen(String yueZhi, String tianGan, String diZhi) {
        Map<String, List<String>> map = new HashMap<>();
        map.put("寅", Arrays.asList("丙", "甲"));
        map.put("卯", Arrays.asList("甲", "乙"));
        map.put("辰", Arrays.asList("壬", "癸"));
        map.put("巳", Arrays.asList("庚", "丙"));
        map.put("午", Arrays.asList("丙", "丁"));
        map.put("未", Arrays.asList("甲", "乙"));
        map.put("申", Arrays.asList("壬", "庚"));
        map.put("酉", Arrays.asList("庚", "辛"));
        map.put("戌", Arrays.asList("丙", "戊"));
        map.put("亥", Arrays.asList("甲", "壬"));
        map.put("子", Arrays.asList("壬", "癸"));
        map.put("丑", Arrays.asList("庚", "辛"));
        
        List<String> deXiu = map.get(yueZhi);
        if (deXiu != null) {
            return deXiu.contains(tianGan);
        }
        return false;
    }

    /**
     * 天罗地网
     */
    public static boolean isTianLuoDiWang(String diZhi) {
        return "辰".equals(diZhi) || "戌".equals(diZhi);
    }

    /**
     * 十恶大败日
     */
    public static boolean isShiEDaBai(String ganZhi) {
        List<String> shiEDaBai = Arrays.asList(
            "甲辰", "乙巳", "丙申", "丁亥", "戊戌",
            "己丑", "庚辰", "辛巳", "壬申", "癸亥"
        );
        return shiEDaBai.contains(ganZhi);
    }

    /**
     * 十灵日
     */
    public static boolean isShiLing(String ganZhi) {
        List<String> shiLing = Arrays.asList(
            "甲辰", "乙卯", "丙子", "丁酉", "戊午",
            "己卯", "庚子", "辛酉", "壬午", "癸卯"
        );
        return shiLing.contains(ganZhi);
    }

    /**
     * 童子煞
     */
    public static boolean isTongZiSha(String yueZhi, String riZhi, String shiZhi) {
        // 春秋寅子贵，冬夏卯未辰
        List<String> chunQiu = Arrays.asList("寅", "卯", "辰", "申", "酉", "戌");
        List<String> dongXia = Arrays.asList("巳", "午", "未", "亥", "子", "丑");
        
        if (chunQiu.contains(yueZhi)) {
            return "寅".equals(riZhi) || "子".equals(riZhi) ||
                   "寅".equals(shiZhi) || "子".equals(shiZhi);
        } else if (dongXia.contains(yueZhi)) {
            return "卯".equals(riZhi) || "未".equals(riZhi) || "辰".equals(riZhi) ||
                   "卯".equals(shiZhi) || "未".equals(shiZhi) || "辰".equals(shiZhi);
        }
        return false;
    }

    /**
     * 流霞
     */
    public static String getLiuXia(String riGan) {
        Map<String, String> map = new HashMap<>();
        map.put("甲", "酉"); map.put("乙", "戌"); map.put("丙", "未");
        map.put("丁", "申"); map.put("戊", "未"); map.put("己", "申");
        map.put("庚", "巳"); map.put("辛", "午"); map.put("壬", "卯");
        map.put("癸", "辰");
        return map.get(riGan);
    }

    /**
     * 分析某柱的所有神煞
     */
    public static List<String> analyzeShenSha(String riGan, String yueZhi, String nianZhi, 
                                               String targetGan, String targetZhi, String ganZhi) {
        List<String> result = new ArrayList<>();
        
        // 天乙贵人
        if (getTianYiGuiRen(riGan).contains(targetZhi)) {
            result.add("天乙贵人");
        }
        
        // 文昌贵人
        if (getWenChangGuiRen(riGan) != null && getWenChangGuiRen(riGan).equals(targetZhi)) {
            result.add("文昌贵人");
        }
        
        // 驿马
        if (getYiMa(nianZhi) != null && getYiMa(nianZhi).equals(targetZhi)) {
            result.add("驿马");
        }
        
        // 桃花
        if (getTaoHua(nianZhi) != null && getTaoHua(nianZhi).equals(targetZhi)) {
            result.add("桃花");
        }
        
        // 华盖
        if (getHuaGai(nianZhi) != null && getHuaGai(nianZhi).equals(targetZhi)) {
            result.add("华盖");
        }
        
        // 将星
        if (getJiangXing(nianZhi) != null && getJiangXing(nianZhi).equals(targetZhi)) {
            result.add("将星");
        }
        
        // 天德贵人
        if (getTianDeGuiRen(yueZhi) != null && getTianDeGuiRen(yueZhi).equals(targetGan)) {
            result.add("天德贵人");
        }
        
        // 月德贵人
        if (getYueDeGuiRen(yueZhi) != null && getYueDeGuiRen(yueZhi).equals(targetGan)) {
            result.add("月德贵人");
        }
        
        // 天德合
        if (getTianDeHe(yueZhi) != null && getTianDeHe(yueZhi).equals(targetGan)) {
            result.add("天德合");
        }
        
        // 月德合
        if (getYueDeHe(yueZhi) != null && getYueDeHe(yueZhi).equals(targetGan)) {
            result.add("月德合");
        }
        
        // 羊刃
        if (getYangRen(riGan) != null && getYangRen(riGan).equals(targetZhi)) {
            result.add("羊刃");
        }
        
        // 飞刃
        if (getFeiRen(riGan) != null && getFeiRen(riGan).equals(targetZhi)) {
            result.add("飞刃");
        }
        
        // 金舆
        if (getJinYu(riGan) != null && getJinYu(riGan).equals(targetZhi)) {
            result.add("金舆");
        }
        
        // 福星贵人
        if (getFuXingGuiRen(riGan) != null && getFuXingGuiRen(riGan).equals(targetZhi)) {
            result.add("福星贵人");
        }
        
        // 天官贵人
        if (getTianGuanGuiRen(riGan) != null && getTianGuanGuiRen(riGan).equals(targetZhi)) {
            result.add("天官贵人");
        }
        
        // 德秀贵人
        if (isDeXiuGuiRen(yueZhi, targetGan, targetZhi)) {
            result.add("德秀贵人");
        }
        
        // 天罗地网
        if (isTianLuoDiWang(targetZhi)) {
            result.add("天罗地网");
        }
        
        // 十恶大败
        if (isShiEDaBai(ganZhi)) {
            result.add("十恶大败");
        }
        
        // 十灵
        if (isShiLing(ganZhi)) {
            result.add("十灵");
        }
        
        // 流霞
        if (getLiuXia(riGan) != null && getLiuXia(riGan).equals(targetZhi)) {
            result.add("流霞");
        }
        
        return result;
    }
}

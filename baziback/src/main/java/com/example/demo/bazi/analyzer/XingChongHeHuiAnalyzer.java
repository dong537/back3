package com.example.demo.bazi.analyzer;

import com.example.demo.bazi.constants.*;
import com.example.demo.bazi.model.*;
import java.util.*;

/**
 * 刑冲合会分析器
 */
public class XingChongHeHuiAnalyzer {
    
    /**
     * 分析八字的刑冲合会关系
     */
    public Map<String, Map<String, Object>> analyze(BaZiChart chart) {
        Map<String, Map<String, Object>> result = new LinkedHashMap<>();
        
        Pillar[] pillars = {chart.getNianZhu(), chart.getYueZhu(), chart.getRiZhu(), chart.getShiZhu()};
        String[] names = {"年", "月", "日", "时"};
        
        for (int i = 0; i < pillars.length; i++) {
            Map<String, Object> pillarRelations = new LinkedHashMap<>();
            
            // 天干关系
            Map<String, List<Map<String, Object>>> tianGanRelations = new LinkedHashMap<>();
            for (int j = 0; j < pillars.length; j++) {
                if (i != j) {
                    analyzeTianGanRelation(pillars[i].getTianGan(), pillars[j].getTianGan(), 
                                          names[j], tianGanRelations);
                }
            }
            if (!tianGanRelations.isEmpty()) {
                pillarRelations.put("天干", tianGanRelations);
            }
            
            // 地支关系
            Map<String, List<Map<String, Object>>> diZhiRelations = new LinkedHashMap<>();
            for (int j = 0; j < pillars.length; j++) {
                if (i != j) {
                    analyzeDiZhiRelation(pillars[i].getDiZhi(), pillars[j].getDiZhi(), 
                                        names[j], diZhiRelations);
                }
            }
            if (!diZhiRelations.isEmpty()) {
                pillarRelations.put("地支", diZhiRelations);
            }
            
            result.put(names[i], pillarRelations);
        }
        
        return result;
    }

    /**
     * 分析天干关系
     */
    private void analyzeTianGanRelation(String gan1, String gan2, String targetName,
                                        Map<String, List<Map<String, Object>>> relations) {
        TianGan tg1 = TianGan.fromName(gan1);
        TianGan tg2 = TianGan.fromName(gan2);
        
        // 天干相合
        if (tg1.getHe() == tg2) {
            addRelation(relations, "合", targetName, 
                       gan1 + gan2 + "合" + tg1.getHeWuXing(), tg1.getHeWuXing());
        }
        
        // 天干相克（冲）
        WuXing wx1 = WuXing.fromName(tg1.getWuXing());
        WuXing wx2 = WuXing.fromName(tg2.getWuXing());
        if (wx1.getKe() == wx2) {
            addRelation(relations, "冲", targetName, 
                       gan1 + gan2 + "相尅", null);
        }
    }

    /**
     * 分析地支关系
     */
    private void analyzeDiZhiRelation(String zhi1, String zhi2, String targetName,
                                      Map<String, List<Map<String, Object>>> relations) {
        DiZhi dz1 = DiZhi.fromName(zhi1);
        DiZhi dz2 = DiZhi.fromName(zhi2);
        
        // 六合
        if (dz1.getLiuHe() == dz2) {
            addRelation(relations, "合", targetName, 
                       zhi1 + zhi2 + "合" + dz1.getLiuHeWuXing(), dz1.getLiuHeWuXing());
        }
        
        // 六冲
        if (dz1.getChong() == dz2) {
            addRelation(relations, "冲", targetName, 
                       zhi1 + zhi2 + "相冲", null);
        }
        
        // 半合
        List<Map<String, Object>> banHeList = dz1.getBanHe(dz2);
        for (Map<String, Object> banHe : banHeList) {
            addRelation(relations, "半合", targetName, 
                       zhi1 + zhi2 + "半合" + banHe.get("元素"), (String) banHe.get("元素"));
        }
        
        // 相刑
        String xingResult = checkXing(zhi1, zhi2);
        if (xingResult != null) {
            addRelation(relations, "刑", targetName, xingResult, null);
        }
        
        // 相破
        String poResult = checkPo(zhi1, zhi2);
        if (poResult != null) {
            addRelation(relations, "破", targetName, poResult, "破");
        }
        
        // 相害
        String haiResult = checkHai(zhi1, zhi2);
        if (haiResult != null) {
            addRelation(relations, "害", targetName, haiResult, null);
        }
    }

    private void addRelation(Map<String, List<Map<String, Object>>> relations, 
                            String type, String targetName, String zhiShiDian, String element) {
        relations.computeIfAbsent(type, k -> new ArrayList<>());
        Map<String, Object> relation = new LinkedHashMap<>();
        relation.put("柱", targetName);
        relation.put("知识点", zhiShiDian);
        if (element != null) {
            relation.put("元素", element);
        }
        relations.get(type).add(relation);
    }

    /**
     * 检查相刑
     */
    private String checkXing(String zhi1, String zhi2) {
        // 子卯刑
        if ((zhi1.equals("子") && zhi2.equals("卯")) || (zhi1.equals("卯") && zhi2.equals("子"))) {
            return zhi1 + zhi2 + "相刑";
        }
        // 寅巳申三刑
        List<String> yinSiShen = Arrays.asList("寅", "巳", "申");
        if (yinSiShen.contains(zhi1) && yinSiShen.contains(zhi2) && !zhi1.equals(zhi2)) {
            return zhi1 + zhi2 + "相刑";
        }
        // 丑未戌三刑
        List<String> chouWeiXu = Arrays.asList("丑", "未", "戌");
        if (chouWeiXu.contains(zhi1) && chouWeiXu.contains(zhi2) && !zhi1.equals(zhi2)) {
            return zhi1 + zhi2 + "相刑";
        }
        // 辰午酉亥自刑
        List<String> ziXing = Arrays.asList("辰", "午", "酉", "亥");
        if (ziXing.contains(zhi1) && zhi1.equals(zhi2)) {
            return zhi1 + zhi2 + "相刑";
        }
        return null;
    }

    /**
     * 检查相破
     */
    private String checkPo(String zhi1, String zhi2) {
        Map<String, String> poMap = new HashMap<>();
        poMap.put("子", "酉"); poMap.put("酉", "子");
        poMap.put("丑", "辰"); poMap.put("辰", "丑");
        poMap.put("寅", "亥"); poMap.put("亥", "寅");
        poMap.put("卯", "午"); poMap.put("午", "卯");
        poMap.put("巳", "申"); poMap.put("申", "巳");
        poMap.put("未", "戌"); poMap.put("戌", "未");
        
        if (poMap.containsKey(zhi1) && poMap.get(zhi1).equals(zhi2)) {
            return zhi1 + zhi2 + "相破";
        }
        return null;
    }

    /**
     * 检查相害
     */
    private String checkHai(String zhi1, String zhi2) {
        Map<String, String> haiMap = new HashMap<>();
        haiMap.put("子", "未"); haiMap.put("未", "子");
        haiMap.put("丑", "午"); haiMap.put("午", "丑");
        haiMap.put("寅", "巳"); haiMap.put("巳", "寅");
        haiMap.put("卯", "辰"); haiMap.put("辰", "卯");
        haiMap.put("申", "亥"); haiMap.put("亥", "申");
        haiMap.put("酉", "戌"); haiMap.put("戌", "酉");
        
        if (haiMap.containsKey(zhi1) && haiMap.get(zhi1).equals(zhi2)) {
            return zhi1 + zhi2 + "相害";
        }
        return null;
    }

    /**
     * 检查三合局
     */
    public List<Map<String, Object>> checkSanHe(BaZiChart chart) {
        List<Map<String, Object>> result = new ArrayList<>();
        List<String> diZhis = Arrays.asList(
            chart.getNianZhu().getDiZhi(),
            chart.getYueZhu().getDiZhi(),
            chart.getRiZhu().getDiZhi(),
            chart.getShiZhu().getDiZhi()
        );
        
        Map<String, List<DiZhi>> sanHeMap = DiZhi.getSanHe();
        for (Map.Entry<String, List<DiZhi>> entry : sanHeMap.entrySet()) {
            List<String> sanHeZhi = new ArrayList<>();
            for (DiZhi dz : entry.getValue()) {
                sanHeZhi.add(dz.getName());
            }
            
            // 检查四柱中是否包含完整三合
            if (diZhis.containsAll(sanHeZhi)) {
                Map<String, Object> sanHe = new LinkedHashMap<>();
                sanHe.put("类型", "三合" + entry.getKey() + "局");
                sanHe.put("地支", sanHeZhi);
                result.add(sanHe);
            }
        }
        
        return result;
    }

    /**
     * 检查三会局
     */
    public List<Map<String, Object>> checkSanHui(BaZiChart chart) {
        List<Map<String, Object>> result = new ArrayList<>();
        List<String> diZhis = Arrays.asList(
            chart.getNianZhu().getDiZhi(),
            chart.getYueZhu().getDiZhi(),
            chart.getRiZhu().getDiZhi(),
            chart.getShiZhu().getDiZhi()
        );
        
        Map<String, List<DiZhi>> sanHuiMap = DiZhi.getSanHui();
        for (Map.Entry<String, List<DiZhi>> entry : sanHuiMap.entrySet()) {
            List<String> sanHuiZhi = new ArrayList<>();
            for (DiZhi dz : entry.getValue()) {
                sanHuiZhi.add(dz.getName());
            }
            
            if (diZhis.containsAll(sanHuiZhi)) {
                Map<String, Object> sanHui = new LinkedHashMap<>();
                sanHui.put("类型", "三会" + entry.getKey() + "局");
                sanHui.put("地支", sanHuiZhi);
                result.add(sanHui);
            }
        }
        
        return result;
    }
}

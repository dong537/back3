package com.example.demo.bazi.analyzer;

import com.example.demo.bazi.constants.*;
import com.example.demo.bazi.model.*;
import java.util.*;

/**
 * 财星分析器
 */
public class CaiXingAnalyzer {
    
    /**
     * 分析财星信息
     */
    public Map<String, Object> analyze(BaZiChart chart) {
        Map<String, Object> result = new LinkedHashMap<>();
        
        String riZhu = chart.getRiZhuTianGan();
        WuXing riWuXing = WuXing.fromName(chart.getRiZhuWuXing());
        WuXing caiWuXing = riWuXing.getKe(); // 我克者为财
        
        result.put("财星五行", caiWuXing.getName());
        
        // 查找正财和偏财
        Map<String, List<String>> caiXing = new LinkedHashMap<>();
        caiXing.put("正财", new ArrayList<>());
        caiXing.put("偏财", new ArrayList<>());
        
        // 检查四柱
        analyzePillar(chart.getNianZhu(), "年柱", riZhu, caiXing);
        analyzePillar(chart.getYueZhu(), "月柱", riZhu, caiXing);
        analyzePillar(chart.getRiZhu(), "日柱", riZhu, caiXing);
        analyzePillar(chart.getShiZhu(), "时柱", riZhu, caiXing);
        
        result.put("财星", caiXing);
        
        // 财库分析
        Map<String, Object> caiKu = new LinkedHashMap<>();
        String kuZhi = caiWuXing.getKu();
        caiKu.put("财库", kuZhi);
        
        List<String> kuFenBu = findKuDistribution(chart, kuZhi);
        caiKu.put("财库分布", kuFenBu.isEmpty() ? 
                  Collections.singletonList("四柱无财库") : kuFenBu);
        
        result.put("财库", caiKu);
        
        return result;
    }

    private void analyzePillar(Pillar pillar, String pillarName, String riZhu, 
                               Map<String, List<String>> caiXing) {
        // 检查天干
        ShiShen tianGanShiShen = ShiShen.calculate(riZhu, pillar.getTianGan());
        if (tianGanShiShen == ShiShen.ZHENG_CAI) {
            caiXing.get("正财").add(pillarName + "天干**" + pillar.getTianGan() + "**");
        } else if (tianGanShiShen == ShiShen.PIAN_CAI) {
            caiXing.get("偏财").add(pillarName + "天干**" + pillar.getTianGan() + "**");
        }
        
        // 检查藏干
        for (String cangGan : pillar.getDiZhiCangGan()) {
            ShiShen cangGanShiShen = ShiShen.calculate(riZhu, cangGan);
            if (cangGanShiShen == ShiShen.ZHENG_CAI) {
                caiXing.get("正财").add(pillarName + "地支**" + pillar.getDiZhi() + "**藏正财");
            } else if (cangGanShiShen == ShiShen.PIAN_CAI) {
                caiXing.get("偏财").add(pillarName + "地支**" + pillar.getDiZhi() + "**藏偏财");
            }
        }
    }

    private List<String> findKuDistribution(BaZiChart chart, String kuZhi) {
        List<String> result = new ArrayList<>();
        
        if (chart.getNianZhu().getDiZhi().equals(kuZhi)) {
            result.add("年柱地支有财库");
        }
        if (chart.getYueZhu().getDiZhi().equals(kuZhi)) {
            result.add("月柱地支有财库");
        }
        if (chart.getRiZhu().getDiZhi().equals(kuZhi)) {
            result.add("日柱地支有财库");
        }
        if (chart.getShiZhu().getDiZhi().equals(kuZhi)) {
            result.add("时柱地支有财库");
        }
        
        return result;
    }
}

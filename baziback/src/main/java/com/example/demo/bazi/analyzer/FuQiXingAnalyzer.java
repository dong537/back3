package com.example.demo.bazi.analyzer;

import com.example.demo.bazi.constants.*;
import com.example.demo.bazi.model.*;
import java.util.*;

/**
 * 夫妻星分析器
 */
public class FuQiXingAnalyzer {
    
    /**
     * 分析夫妻星信息（男命以财星为妻，女命以官杀为夫）
     */
    public Map<String, Object> analyze(BaZiChart chart, boolean isMale) {
        Map<String, Object> result = new LinkedHashMap<>();
        
        String riZhu = chart.getRiZhuTianGan();
        WuXing riWuXing = WuXing.fromName(chart.getRiZhuWuXing());
        
        if (isMale) {
            // 男命：财星为妻
            WuXing qiWuXing = riWuXing.getKe();
            result.put("妻星五行", qiWuXing.getName());
            
            Map<String, List<String>> qiXing = new LinkedHashMap<>();
            qiXing.put("正财妻星", new ArrayList<>());
            qiXing.put("偏财妻星", new ArrayList<>());
            
            analyzeQiXing(chart, riZhu, qiXing);
            result.put("妻星", qiXing);
            
            // 妻库（财库）
            Map<String, Object> qiKu = new LinkedHashMap<>();
            String kuZhi = qiWuXing.getKu();
            qiKu.put("妻库", kuZhi);
            qiKu.put("妻库分布", findKuDistribution(chart, kuZhi));
            result.put("妻库(财库)", qiKu);
            
        } else {
            // 女命：官杀为夫
            WuXing fuWuXing = riWuXing.getBeKe();
            result.put("夫星五行", fuWuXing.getName());
            
            Map<String, List<String>> fuXing = new LinkedHashMap<>();
            fuXing.put("正官夫星", new ArrayList<>());
            fuXing.put("七杀夫星", new ArrayList<>());
            
            analyzeFuXing(chart, riZhu, fuXing);
            result.put("夫星", fuXing);
            
            // 夫库（官库）
            Map<String, Object> fuKu = new LinkedHashMap<>();
            String kuZhi = fuWuXing.getKu();
            fuKu.put("夫库", kuZhi);
            fuKu.put("夫库分布", findKuDistribution(chart, kuZhi));
            result.put("夫库(官库)", fuKu);
        }
        
        // 夫妻宫（日支）
        result.put("夫妻宫", chart.getRiZhu().getDiZhi());
        
        return result;
    }

    private void analyzeQiXing(BaZiChart chart, String riZhu, Map<String, List<String>> qiXing) {
        for (Pillar pillar : chart.getAllPillars()) {
            // 检查藏干
            for (String cangGan : pillar.getDiZhiCangGan()) {
                ShiShen ss = ShiShen.calculate(riZhu, cangGan);
                if (ss == ShiShen.ZHENG_CAI) {
                    qiXing.get("正财妻星").add(pillar.getName() + "柱地支**" + pillar.getDiZhi() + "**藏正财");
                } else if (ss == ShiShen.PIAN_CAI) {
                    qiXing.get("偏财妻星").add(pillar.getName() + "柱地支**" + pillar.getDiZhi() + "**藏偏财");
                }
            }
        }
    }

    private void analyzeFuXing(BaZiChart chart, String riZhu, Map<String, List<String>> fuXing) {
        for (Pillar pillar : chart.getAllPillars()) {
            // 检查天干
            ShiShen tianGanSs = ShiShen.calculate(riZhu, pillar.getTianGan());
            if (tianGanSs == ShiShen.ZHENG_GUAN) {
                fuXing.get("正官夫星").add(pillar.getName() + "柱天干**" + pillar.getTianGan() + "**");
            } else if (tianGanSs == ShiShen.QI_SHA) {
                fuXing.get("七杀夫星").add(pillar.getName() + "柱天干**" + pillar.getTianGan() + "**");
            }
            
            // 检查藏干
            for (String cangGan : pillar.getDiZhiCangGan()) {
                ShiShen ss = ShiShen.calculate(riZhu, cangGan);
                if (ss == ShiShen.ZHENG_GUAN) {
                    fuXing.get("正官夫星").add(pillar.getName() + "柱地支**" + pillar.getDiZhi() + "**藏正官");
                } else if (ss == ShiShen.QI_SHA) {
                    fuXing.get("七杀夫星").add(pillar.getName() + "柱地支**" + pillar.getDiZhi() + "**藏七杀");
                }
            }
        }
    }

    private List<String> findKuDistribution(BaZiChart chart, String kuZhi) {
        List<String> result = new ArrayList<>();
        String[] names = {"年", "月", "日", "时"};
        Pillar[] pillars = {chart.getNianZhu(), chart.getYueZhu(), chart.getRiZhu(), chart.getShiZhu()};
        
        for (int i = 0; i < pillars.length; i++) {
            if (pillars[i].getDiZhi().equals(kuZhi)) {
                result.add(names[i] + "柱地支有库");
            }
        }
        
        if (result.isEmpty()) {
            result.add("四柱无妻库");
        }
        
        return result;
    }
}

package com.example.demo.bazi.analyzer;

import com.example.demo.bazi.constants.*;
import com.example.demo.bazi.model.*;
import java.util.*;

/**
 * 父母星分析器
 * 正印为母亲，偏财为父亲
 */
public class FuMuXingAnalyzer {
    
    /**
     * 分析父母星信息
     */
    public Map<String, Object> analyze(BaZiChart chart) {
        Map<String, Object> result = new LinkedHashMap<>();
        
        String riZhu = chart.getRiZhuTianGan();
        
        Map<String, List<String>> fuMuXing = new LinkedHashMap<>();
        fuMuXing.put("正印-母亲", new ArrayList<>());
        fuMuXing.put("偏财-父亲", new ArrayList<>());
        
        // 分析四柱
        for (Pillar pillar : chart.getAllPillars()) {
            String pillarName = pillar.getName() + "柱";
            
            // 检查天干
            ShiShen tianGanSs = ShiShen.calculate(riZhu, pillar.getTianGan());
            if (tianGanSs == ShiShen.ZHENG_YIN) {
                fuMuXing.get("正印-母亲").add(pillarName + "天干**" + pillar.getTianGan() + "**");
            } else if (tianGanSs == ShiShen.PIAN_CAI) {
                fuMuXing.get("偏财-父亲").add(pillarName + "天干**" + pillar.getTianGan() + "**");
            }
            
            // 检查藏干
            for (String cangGan : pillar.getDiZhiCangGan()) {
                ShiShen ss = ShiShen.calculate(riZhu, cangGan);
                if (ss == ShiShen.ZHENG_YIN) {
                    fuMuXing.get("正印-母亲").add(pillarName + "地支**" + pillar.getDiZhi() + "**藏正印");
                } else if (ss == ShiShen.PIAN_CAI) {
                    fuMuXing.get("偏财-父亲").add(pillarName + "地支**" + pillar.getDiZhi() + "**藏偏财");
                }
            }
        }
        
        result.put("父母星", fuMuXing);
        result.put("父母柱", "看年柱和月柱");
        
        return result;
    }
}

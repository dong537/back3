package com.example.demo.bazi.analyzer;

import com.example.demo.bazi.constants.*;
import com.example.demo.bazi.model.*;
import java.util.*;

/**
 * 子女星分析器
 * 男命：七杀为儿子，正官为女儿
 * 女命：伤官为儿子，食神为女儿
 */
public class ZiNvXingAnalyzer {
    
    /**
     * 分析子女星信息
     */
    public Map<String, Object> analyze(BaZiChart chart, boolean isMale) {
        Map<String, Object> result = new LinkedHashMap<>();
        
        String riZhu = chart.getRiZhuTianGan();
        WuXing riWuXing = WuXing.fromName(chart.getRiZhuWuXing());
        
        if (isMale) {
            // 男命：官杀为子女
            WuXing ziNvWuXing = riWuXing.getBeKe();
            result.put("子女星五行", ziNvWuXing.getName());
            
            Map<String, List<String>> ziNvXing = new LinkedHashMap<>();
            ziNvXing.put("正官-女儿", new ArrayList<>());
            ziNvXing.put("七杀-儿子", new ArrayList<>());
            
            analyzeMaleZiNv(chart, riZhu, ziNvXing);
            result.put("子女星", ziNvXing);
            
        } else {
            // 女命：食伤为子女
            WuXing ziNvWuXing = riWuXing.getSheng();
            result.put("子女星五行", ziNvWuXing.getName());
            
            Map<String, List<String>> ziNvXing = new LinkedHashMap<>();
            ziNvXing.put("食神-女儿", new ArrayList<>());
            ziNvXing.put("伤官-儿子", new ArrayList<>());
            
            analyzeFemaleZiNv(chart, riZhu, ziNvXing);
            result.put("子女星", ziNvXing);
        }
        
        // 子女宫（时支）
        result.put("子女宫", chart.getShiZhu().getDiZhi());
        
        return result;
    }

    private void analyzeMaleZiNv(BaZiChart chart, String riZhu, Map<String, List<String>> ziNvXing) {
        boolean hasQiSha = false;
        boolean hasZhengGuan = false;
        
        for (Pillar pillar : chart.getAllPillars()) {
            String pillarName = pillar.getName() + "柱";
            
            // 检查天干
            ShiShen tianGanSs = ShiShen.calculate(riZhu, pillar.getTianGan());
            if (tianGanSs == ShiShen.ZHENG_GUAN) {
                ziNvXing.get("正官-女儿").add(pillarName + "天干**" + pillar.getTianGan() + "**");
                hasZhengGuan = true;
            } else if (tianGanSs == ShiShen.QI_SHA) {
                ziNvXing.get("七杀-儿子").add(pillarName + "天干**" + pillar.getTianGan() + "**");
                hasQiSha = true;
            }
            
            // 检查藏干
            for (String cangGan : pillar.getDiZhiCangGan()) {
                ShiShen ss = ShiShen.calculate(riZhu, cangGan);
                if (ss == ShiShen.ZHENG_GUAN) {
                    ziNvXing.get("正官-女儿").add(pillarName + "地支**" + pillar.getDiZhi() + "**藏正官");
                    hasZhengGuan = true;
                } else if (ss == ShiShen.QI_SHA) {
                    ziNvXing.get("七杀-儿子").add(pillarName + "地支**" + pillar.getDiZhi() + "**藏七杀");
                    hasQiSha = true;
                }
            }
        }
        
        // 如果没有找到，添加说明
        if (!hasQiSha) {
            ziNvXing.get("七杀-儿子").add("四柱无七杀儿子星");
        }
        if (!hasZhengGuan) {
            ziNvXing.get("正官-女儿").add("四柱无正官女儿星");
        }
    }

    private void analyzeFemaleZiNv(BaZiChart chart, String riZhu, Map<String, List<String>> ziNvXing) {
        boolean hasShiShen = false;
        boolean hasShangGuan = false;
        
        for (Pillar pillar : chart.getAllPillars()) {
            String pillarName = pillar.getName() + "柱";
            
            // 检查天干
            ShiShen tianGanSs = ShiShen.calculate(riZhu, pillar.getTianGan());
            if (tianGanSs == ShiShen.SHI_SHEN) {
                ziNvXing.get("食神-女儿").add(pillarName + "天干**" + pillar.getTianGan() + "**");
                hasShiShen = true;
            } else if (tianGanSs == ShiShen.SHANG_GUAN) {
                ziNvXing.get("伤官-儿子").add(pillarName + "天干**" + pillar.getTianGan() + "**");
                hasShangGuan = true;
            }
            
            // 检查藏干
            for (String cangGan : pillar.getDiZhiCangGan()) {
                ShiShen ss = ShiShen.calculate(riZhu, cangGan);
                if (ss == ShiShen.SHI_SHEN) {
                    ziNvXing.get("食神-女儿").add(pillarName + "地支**" + pillar.getDiZhi() + "**藏食神");
                    hasShiShen = true;
                } else if (ss == ShiShen.SHANG_GUAN) {
                    ziNvXing.get("伤官-儿子").add(pillarName + "地支**" + pillar.getDiZhi() + "**藏伤官");
                    hasShangGuan = true;
                }
            }
        }
        
        if (!hasShiShen) {
            ziNvXing.get("食神-女儿").add("四柱无食神女儿星");
        }
        if (!hasShangGuan) {
            ziNvXing.get("伤官-儿子").add("四柱无伤官儿子星");
        }
    }
}

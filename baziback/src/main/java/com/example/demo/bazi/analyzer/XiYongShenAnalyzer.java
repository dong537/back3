package com.example.demo.bazi.analyzer;

import com.example.demo.bazi.constants.*;
import com.example.demo.bazi.model.*;
import java.util.*;

/**
 * 喜用神分析器
 */
public class XiYongShenAnalyzer {

    private TiaoHouAnalyzer tiaoHouAnalyzer = new TiaoHouAnalyzer();

    /**
     * 分析喜用神
     */
    public Map<String, Object> analyze(BaZiChart chart) {
        Map<String, Object> result = new LinkedHashMap<>();
        
        // 分析日主强弱
        Map<String, Object> riZhuQiangRuo = analyzeRiZhuStrength(chart);
        boolean isStrong = (boolean) riZhuQiangRuo.get("身强");
        
        // 日主强弱推荐
        Map<String, Object> riZhuTuiJian = new LinkedHashMap<>();
        if (isStrong) {
            riZhuTuiJian.put("喜用五行", Arrays.asList("金", "水", "木"));
            riZhuTuiJian.put("忌用五行", Arrays.asList("土"));
        } else {
            riZhuTuiJian.put("喜用五行", Arrays.asList("木", "水", "火"));
            riZhuTuiJian.put("忌用五行", Arrays.asList("土"));
        }
        result.put("日主强弱推荐", riZhuTuiJian);
        
        // 调候推荐
        String riGan = chart.getRiZhuTianGan();
        String yueZhi = chart.getYueZhu().getDiZhi();
        String[] tiaoHouYongShen = tiaoHouAnalyzer.getTiaoHouYongShen(riGan, yueZhi);
        
        Map<String, Object> tiaoHouTuiJian = new LinkedHashMap<>();
        if (tiaoHouYongShen != null) {
            tiaoHouTuiJian.put("喜用天干", Arrays.asList(tiaoHouYongShen));
            
            // 转换为五行
            Set<String> wuXingSet = new LinkedHashSet<>();
            for (String gan : tiaoHouYongShen) {
                wuXingSet.add(TianGan.fromName(gan).getWuXing());
            }
            tiaoHouTuiJian.put("喜用五行", new ArrayList<>(wuXingSet));
        }
        result.put("调候推荐", tiaoHouTuiJian);
        
        // 格局推荐
        Map<String, Object> geJuTuiJian = analyzeGeJu(chart);
        result.put("格局推荐", geJuTuiJian);
        
        // 综合推荐
        Map<String, Object> zongHeTuiJian = synthesizeRecommendation(riZhuTuiJian, tiaoHouTuiJian, geJuTuiJian);
        result.put("综合推荐", zongHeTuiJian);
        
        return result;
    }

    /**
     * 分析日主强弱
     */
    private Map<String, Object> analyzeRiZhuStrength(BaZiChart chart) {
        Map<String, Object> result = new LinkedHashMap<>();
        
        String riGan = chart.getRiZhuTianGan();
        WuXing riWuXing = WuXing.fromName(TianGan.fromName(riGan).getWuXing());
        WuXing yinWuXing = riWuXing.getBeSheng(); // 生我者
        
        int strength = 0;
        
        // 检查月令
        String yueZhi = chart.getYueZhu().getDiZhi();
        String yueZhiWuXing = DiZhi.fromName(yueZhi).getWuXing();
        if (yueZhiWuXing.equals(riWuXing.getName()) || yueZhiWuXing.equals(yinWuXing.getName())) {
            strength += 30; // 得月令
        }
        
        // 检查四柱天干地支
        for (Pillar pillar : chart.getAllPillars()) {
            // 天干
            String tianGanWuXing = pillar.getTianGanWuXing();
            if (tianGanWuXing.equals(riWuXing.getName())) {
                strength += 10; // 比劫帮身
            } else if (tianGanWuXing.equals(yinWuXing.getName())) {
                strength += 8; // 印星生身
            }
            
            // 地支
            String diZhiWuXing = pillar.getDiZhiWuXing();
            if (diZhiWuXing.equals(riWuXing.getName())) {
                strength += 8;
            } else if (diZhiWuXing.equals(yinWuXing.getName())) {
                strength += 6;
            }
            
            // 藏干
            for (String cangGan : pillar.getDiZhiCangGan()) {
                String cangGanWuXing = TianGan.fromName(cangGan).getWuXing();
                if (cangGanWuXing.equals(riWuXing.getName())) {
                    strength += 3;
                } else if (cangGanWuXing.equals(yinWuXing.getName())) {
                    strength += 2;
                }
            }
        }
        
        // 检查十二长生状态
        String riZhuXingYun = chart.getXingYun().get("日");
        if (ShiErChangSheng.isWangXiang(riZhuXingYun)) {
            strength += 15;
        } else if (ShiErChangSheng.isShuaiBai(riZhuXingYun)) {
            strength -= 10;
        }
        
        boolean isStrong = strength >= 50;
        result.put("身强", isStrong);
        result.put("强度分值", strength);
        result.put("判断", isStrong ? "身强" : "身弱");
        
        return result;
    }

    /**
     * 分析格局推荐
     */
    private Map<String, Object> analyzeGeJu(BaZiChart chart) {
        Map<String, Object> result = new LinkedHashMap<>();
        
        // 检查月柱天干十神来确定格局
        String yueGanShiShen = chart.getYueZhu().getTianGanShiShen();
        
        // 根据格局推荐喜用神
        switch (yueGanShiShen) {
            case "正财":
            case "偏财":
                result.put("格局", yueGanShiShen + "格");
                result.put("喜用五行", Arrays.asList("金")); // 官星护财
                result.put("喜用十神", Arrays.asList("正官"));
                result.put("忌用五行", Arrays.asList("金", "火"));
                result.put("忌用十神", Arrays.asList("七杀", "伤官"));
                break;
            case "正官":
                result.put("格局", "正官格");
                result.put("喜用五行", Arrays.asList("水")); // 印星化官
                result.put("喜用十神", Arrays.asList("正印"));
                result.put("忌用五行", Arrays.asList("火"));
                result.put("忌用十神", Arrays.asList("伤官"));
                break;
            case "七杀":
                result.put("格局", "七杀格");
                result.put("喜用五行", Arrays.asList("火")); // 食神制杀
                result.put("喜用十神", Arrays.asList("食神"));
                result.put("忌用五行", Arrays.asList("金"));
                result.put("忌用十神", Arrays.asList("财星"));
                break;
            case "正印":
            case "偏印":
                result.put("格局", yueGanShiShen + "格");
                result.put("喜用五行", Arrays.asList("金")); // 官星生印
                result.put("喜用十神", Arrays.asList("正官"));
                result.put("忌用五行", Arrays.asList("土"));
                result.put("忌用十神", Arrays.asList("财星"));
                break;
            case "食神":
            case "伤官":
                result.put("格局", yueGanShiShen + "格");
                result.put("喜用五行", Arrays.asList("土")); // 食伤生财
                result.put("喜用十神", Arrays.asList("正财", "偏财"));
                result.put("忌用五行", Arrays.asList("水"));
                result.put("忌用十神", Arrays.asList("印星"));
                break;
            default:
                result.put("格局", "比劫格");
                result.put("喜用五行", Arrays.asList("金", "土"));
                result.put("喜用十神", Arrays.asList("正官", "正财"));
                result.put("忌用五行", Arrays.asList("木"));
                result.put("忌用十神", Arrays.asList("比肩", "劫财"));
                break;
        }
        
        return result;
    }

    /**
     * 综合推荐
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> synthesizeRecommendation(Map<String, Object> riZhuTuiJian,
                                                          Map<String, Object> tiaoHouTuiJian,
                                                          Map<String, Object> geJuTuiJian) {
        Map<String, Object> result = new LinkedHashMap<>();
        
        // 综合喜用五行
        Set<String> xiYongWuXing = new LinkedHashSet<>();
        if (tiaoHouTuiJian.containsKey("喜用五行")) {
            xiYongWuXing.addAll((List<String>) tiaoHouTuiJian.get("喜用五行"));
        }
        if (geJuTuiJian.containsKey("喜用五行")) {
            xiYongWuXing.addAll((List<String>) geJuTuiJian.get("喜用五行"));
        }
        result.put("喜用五行", new ArrayList<>(xiYongWuXing));
        
        // 综合忌用五行
        Set<String> jiYongWuXing = new LinkedHashSet<>();
        if (riZhuTuiJian.containsKey("忌用五行")) {
            jiYongWuXing.addAll((List<String>) riZhuTuiJian.get("忌用五行"));
        }
        result.put("忌用五行", new ArrayList<>(jiYongWuXing));
        
        return result;
    }
}

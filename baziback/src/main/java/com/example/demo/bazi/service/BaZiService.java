package com.example.demo.bazi.service;

import com.example.demo.bazi.analyzer.*;
import com.example.demo.bazi.model.*;
import org.springframework.stereotype.Service;
import java.util.*;
import java.time.LocalDate;

/**
 * 八字测算主服务类
 */
@Service
public class BaZiService {

    private final CaiXingAnalyzer caiXingAnalyzer = new CaiXingAnalyzer();
    private final FuQiXingAnalyzer fuQiXingAnalyzer = new FuQiXingAnalyzer();
    private final FuMuXingAnalyzer fuMuXingAnalyzer = new FuMuXingAnalyzer();
    private final ZiNvXingAnalyzer ziNvXingAnalyzer = new ZiNvXingAnalyzer();
    private final XingChongHeHuiAnalyzer xingChongHeHuiAnalyzer = new XingChongHeHuiAnalyzer();
    private final ShenShaAnalyzer shenShaAnalyzer = new ShenShaAnalyzer();
    private final TiaoHouAnalyzer tiaoHouAnalyzer = new TiaoHouAnalyzer();
    private final XiYongShenAnalyzer xiYongShenAnalyzer = new XiYongShenAnalyzer();
    private final DaYunAnalyzer daYunAnalyzer = new DaYunAnalyzer();
    private final RiZhuAnalyzer riZhuAnalyzer = new RiZhuAnalyzer();
    private final ShiShenLunMingAnalyzer shiShenLunMingAnalyzer = new ShiShenLunMingAnalyzer();

    /**
     * 完整八字分析
     * @param baZiStr 八字字符串，格式：乙酉 己丑 甲辰 戊辰
     * @param birthYear 出生年份
     * @param isMale 是否男命
     * @return 完整的八字分析报告
     */
    public Map<String, Object> analyze(String baZiStr, int birthYear, boolean isMale) {
        return analyze(baZiStr, birthYear, isMale, 4); // 默认起运年龄4岁
    }

    /**
     * 完整八字分析（含起运年龄）
     * @param baZiStr 八字字符串，格式：乙酉 己丑 甲辰 戊辰
     * @param birthYear 出生年份
     * @param isMale 是否男命
     * @param qiYunAge 起运年龄
     * @return 完整的八字分析报告
     */
    public Map<String, Object> analyze(String baZiStr, int birthYear, boolean isMale, int qiYunAge) {
        Map<String, Object> result = new LinkedHashMap<>();
        
        // 解析八字
        BaZiChart chart = BaZiChart.parse(baZiStr);
        
        // 基础信息
        result.put("_id", chart.getId());
        result.put("季节", chart.getJiJie());
        result.put("生肖", chart.getShengXiao());
        result.put("八字", baZiStr);
        
        // 星运
        result.put("星运", chart.getXingYun());
        
        // 自坐
        result.put("自坐", chart.getZiZuo());
        
        // 空亡
        result.put("空亡", chart.getKongWang());
        
        // 纳音
        result.put("纳音", chart.getNaYin());
        
        // 胎命身
        result.put("胎命身", chart.getTaiMingShen());
        
        // 八字各柱信息
        result.put("八字各柱信息", chart.getBaZiGeZhuInfo());
        
        // 财星信息
        result.put("财星信息", caiXingAnalyzer.analyze(chart));
        
        // 夫妻星信息
        result.put("夫妻星信息", fuQiXingAnalyzer.analyze(chart, isMale));
        
        // 父母星信息
        result.put("父母星信息", fuMuXingAnalyzer.analyze(chart));
        
        // 子女星信息
        result.put("子女星信息", ziNvXingAnalyzer.analyze(chart, isMale));
        
        // 阴阳情况分析
        result.put("阴阳情况分析", chart.getYinYangFenXi());
        
        // 刑冲合会
        result.put("刑冲合会", xingChongHeHuiAnalyzer.analyze(chart));
        
        // 神煞
        result.put("神煞", shenShaAnalyzer.analyze(chart));
        
        // 调候信息
        result.put("调候信息", tiaoHouAnalyzer.analyze(chart));
        
        // 日柱等级信息
        result.put("日柱等级信息", riZhuAnalyzer.analyze(chart));
        
        // 喜用神分析
        result.put("喜用神分析", xiYongShenAnalyzer.analyze(chart));
        
        // 四柱十神论命知识
        result.put("四柱十神论命知识", shiShenLunMingAnalyzer.analyze(chart));
        
        // 四柱关系论命知识
        result.put("四柱关系论命知识", new LinkedHashMap<>());
        
        // 参考格局信息
        result.put("参考格局信息", determineGeJu(chart));
        
        // 大运数据
        Map<String, Object> daYunData = daYunAnalyzer.calculate(chart, birthYear, isMale, qiYunAge);
        result.put("大运数据", daYunData);
        
        // 计算起运详细信息
        Map<String, Object> qiYunInfo = calculateQiYunInfo(birthYear, qiYunAge);
        result.put("起运信息", qiYunInfo);
        chart.setQiYunInfo(qiYunInfo);
        
        // 人元司令
        result.put("人元司令", chart.getRenYuanSiLing());
        
        // 流年数据（当前年份前后各5年）
        List<Map<String, Object>> liuNianList = calculateLiuNian(chart, birthYear, 10);
        result.put("流年数据", liuNianList);
        
        // 流月数据（当前年份的12个月）
        int currentYear = LocalDate.now().getYear();
        List<Map<String, Object>> liuYueList = calculateLiuYue(chart, currentYear);
        result.put("流月数据", liuYueList);
        
        // 扩展各柱信息，包含旬首和神煞
        Map<String, Map<String, Object>> detailedPillarInfo = getDetailedPillarInfo(chart);
        result.put("详细各柱信息", detailedPillarInfo);
        
        return result;
    }

    /**
     * 简易八字分析（不包含大运）
     */
    public Map<String, Object> analyzeSimple(String baZiStr, boolean isMale) {
        Map<String, Object> result = new LinkedHashMap<>();
        
        BaZiChart chart = BaZiChart.parse(baZiStr);
        
        result.put("_id", chart.getId());
        result.put("季节", chart.getJiJie());
        result.put("生肖", chart.getShengXiao());
        result.put("八字", baZiStr);
        result.put("星运", chart.getXingYun());
        result.put("自坐", chart.getZiZuo());
        result.put("空亡", chart.getKongWang());
        result.put("纳音", chart.getNaYin());
        result.put("胎命身", chart.getTaiMingShen());
        result.put("八字各柱信息", chart.getBaZiGeZhuInfo());
        result.put("财星信息", caiXingAnalyzer.analyze(chart));
        result.put("夫妻星信息", fuQiXingAnalyzer.analyze(chart, isMale));
        result.put("父母星信息", fuMuXingAnalyzer.analyze(chart));
        result.put("子女星信息", ziNvXingAnalyzer.analyze(chart, isMale));
        result.put("阴阳情况分析", chart.getYinYangFenXi());
        result.put("刑冲合会", xingChongHeHuiAnalyzer.analyze(chart));
        result.put("神煞", shenShaAnalyzer.analyze(chart));
        result.put("调候信息", tiaoHouAnalyzer.analyze(chart));
        result.put("日柱等级信息", riZhuAnalyzer.analyze(chart));
        result.put("喜用神分析", xiYongShenAnalyzer.analyze(chart));
        result.put("四柱十神论命知识", shiShenLunMingAnalyzer.analyze(chart));
        result.put("参考格局信息", determineGeJu(chart));
        
        return result;
    }

    /**
     * 仅分析大运
     */
    public Map<String, Object> analyzeDaYun(String baZiStr, int birthYear, boolean isMale) {
        BaZiChart chart = BaZiChart.parse(baZiStr);
        return daYunAnalyzer.calculate(chart, birthYear, isMale);
    }

    /**
     * 仅分析刑冲合会
     */
    public Map<String, Map<String, Object>> analyzeXingChongHeHui(String baZiStr) {
        BaZiChart chart = BaZiChart.parse(baZiStr);
        return xingChongHeHuiAnalyzer.analyze(chart);
    }

    /**
     * 仅分析神煞
     */
    public Map<String, List<String>> analyzeShenSha(String baZiStr) {
        BaZiChart chart = BaZiChart.parse(baZiStr);
        return shenShaAnalyzer.analyze(chart);
    }

    /**
     * 仅分析喜用神
     */
    public Map<String, Object> analyzeXiYongShen(String baZiStr) {
        BaZiChart chart = BaZiChart.parse(baZiStr);
        return xiYongShenAnalyzer.analyze(chart);
    }

    /**
     * 确定格局
     */
    private String determineGeJu(BaZiChart chart) {
        String yueGanShiShen = chart.getYueZhu().getTianGanShiShen();
        
        switch (yueGanShiShen) {
            case "正财": return "正财格";
            case "偏财": return "偏财格";
            case "正官": return "正官格";
            case "七杀": return "七杀格";
            case "正印": return "正印格";
            case "偏印": return "偏印格";
            case "食神": return "食神格";
            case "伤官": return "伤官格";
            case "比肩": return "比肩格";
            case "劫财": return "劫财格";
            default: return "普通格局";
        }
    }

    /**
     * 获取八字基础信息
     */
    public BaZiChart parseBaZi(String baZiStr) {
        return BaZiChart.parse(baZiStr);
    }

    /**
     * 计算起运详细信息
     */
    private Map<String, Object> calculateQiYunInfo(int birthYear, int qiYunAge) {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("起运年龄", qiYunAge + "岁");
        info.put("起运年份", birthYear + qiYunAge);
        // 简化处理：假设起运为出生后3年1个月4天22小时
        info.put("起运描述", String.format("出生后%d年1个月4天22个小时", qiYunAge));
        return info;
    }

    /**
     * 计算流年数据
     */
    private List<Map<String, Object>> calculateLiuNian(BaZiChart chart, int birthYear, int count) {
        List<Map<String, Object>> liuNianList = new ArrayList<>();
        String riZhuTianGan = chart.getRiZhuTianGan();
        int currentYear = LocalDate.now().getYear();
        int startYear = currentYear - count / 2;
        
        for (int i = 0; i < count; i++) {
            int year = startYear + i;
            int age = year - birthYear;
            LiuNian liuNian = new LiuNian(year, age, riZhuTianGan);
            liuNianList.add(liuNian.toMap());
        }
        
        return liuNianList;
    }

    /**
     * 计算流月数据
     */
    private List<Map<String, Object>> calculateLiuYue(BaZiChart chart, int year) {
        List<Map<String, Object>> liuYueList = new ArrayList<>();
        String riZhuTianGan = chart.getRiZhuTianGan();
        
        // 12个月的节气信息（简化处理）
        String[] solarTerms = {"立春", "惊蛰", "清明", "立夏", "芒种", "小暑", 
                               "立秋", "白露", "寒露", "立冬", "大雪", "小寒"};
        String[] solarTermDates = {"2/4", "3/5", "4/4", "5/5", "6/5", "7/6",
                                   "8/7", "9/7", "10/8", "11/7", "12/6", "1"};
        
        for (int month = 1; month <= 12; month++) {
            LiuYue liuYue = new LiuYue(month, solarTerms[month - 1], 
                                      solarTermDates[month - 1], year, riZhuTianGan);
            liuYueList.add(liuYue.toMap());
        }
        
        return liuYueList;
    }

    /**
     * 获取详细的各柱信息（包含旬首、神煞等）
     */
    private Map<String, Map<String, Object>> getDetailedPillarInfo(BaZiChart chart) {
        Map<String, Map<String, Object>> result = new LinkedHashMap<>();
        
        // 计算神煞（如果还没有计算）
        Map<String, List<String>> shenShaMap = shenShaAnalyzer.analyze(chart);
        
        // 年柱
        Map<String, Object> nianInfo = chart.getNianZhu().toMap();
        nianInfo.put("神煞", shenShaMap.getOrDefault("年", new ArrayList<>()));
        result.put("年", nianInfo);
        
        // 月柱
        Map<String, Object> yueInfo = chart.getYueZhu().toMap();
        yueInfo.put("神煞", shenShaMap.getOrDefault("月", new ArrayList<>()));
        result.put("月", yueInfo);
        
        // 日柱
        Map<String, Object> riInfo = chart.getRiZhu().toMap();
        riInfo.put("神煞", shenShaMap.getOrDefault("日", new ArrayList<>()));
        result.put("日", riInfo);
        
        // 时柱
        Map<String, Object> shiInfo = chart.getShiZhu().toMap();
        shiInfo.put("神煞", shenShaMap.getOrDefault("时", new ArrayList<>()));
        result.put("时", shiInfo);
        
        return result;
    }
}

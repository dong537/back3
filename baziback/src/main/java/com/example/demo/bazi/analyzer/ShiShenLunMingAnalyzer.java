package com.example.demo.bazi.analyzer;

import com.example.demo.bazi.constants.*;
import com.example.demo.bazi.model.*;
import java.util.*;

/**
 * 四柱十神论命分析器
 */
public class ShiShenLunMingAnalyzer {

    // 十神论命知识库
    private static final Map<String, List<String>> KNOWLEDGE_BASE = new HashMap<>();

    static {
        // 日主坐偏财
        KNOWLEDGE_BASE.put("日主坐偏财", Arrays.asList(
            "[男命][婚姻]偏财在日支为喜，得配偶资助成家立业；若为忌神，配偶花钱如水，爱慕虚荣。",
            "[男命][婚姻][喜神]日支单现偏财，其他各柱无正财星，偏财为喜，妻忠良纯朴，相夫教子。[男命][婚姻][忌神]日支单现偏财，其他各柱无正财星，若偏财为忌神，虽花费大方，但不致不贞；逢刑冲，恐损妻寿元。"
        ));

        // 正官在年柱地支
        KNOWLEDGE_BASE.put("正官在年柱地支", Arrays.asList(
            "[家庭背景]年支独现正官，不受破坏，主出身富贵。"
        ));

        // 正财在月柱天干
        KNOWLEDGE_BASE.put("正财在月柱天干", Arrays.asList(
            "[家庭背景]月柱干支皆正财，出身富贵之家，父辈有财势，本人能继承家产或家业。"
        ));

        // 正印在月柱地支
        KNOWLEDGE_BASE.put("正印在月柱地支", Arrays.asList(
            "[性格]正印不现月干而现月支，高尚人格，端正容貌，高深智慧，沉默寡言。",
            "[六亲]正印现月支日支刑冲，母家有凋落之象。"
        ));

        // 月柱正财坐正印
        KNOWLEDGE_BASE.put("月柱正财坐正印", Arrays.asList(
            "[事业]不论身强身弱均非吉配，印为用被破更凶。家庭易破败，求谋屡遭阻逆，心愿难达。在上不能得长上爱护与支持，在商场上因竞争招来祸患。母亲与父亲经常争吵。生涯中易因财招祸。",
            "[男命][家庭]母亲与妻子不和，不能互相容忍。"
        ));

        // 偏财在时柱地支
        KNOWLEDGE_BASE.put("偏财在时柱地支", Arrays.asList(
            "[财富]日、时地支坐专位偏财者，无刑冲，时干无比肩、劫财，大运无比劫刑冲，主晚年发达。"
        ));

        // 时柱偏财坐偏财
        KNOWLEDGE_BASE.put("时柱偏财坐偏财", Arrays.asList(
            "[身强][财富]日主健旺者大吉，经济头脑好，交际手腕高明，外乡发迹，常得意外财。偏财为忌神，花钱大方，花花公子，奸滑狡诈，多破耗。"
        ));

        // 时柱偏财坐劫财
        KNOWLEDGE_BASE.put("时柱偏财坐劫财", Arrays.asList(
            "[六亲]可作父缘薄之推论",
            "[家庭背景]幼年家境清贫。",
            "[家庭背景]偏财坐专位羊刃、劫财，父去他乡。",
            "[家庭背景]不论身强身弱都不吉，身强财为喜者更忌，父亲病弱或父运不扬，与父有代沟，父亲飘零，重者死于异乡，产业继承多纷争。男命有色情风波之患。"
        ));

        // 时柱偏财坐正印
        KNOWLEDGE_BASE.put("时柱偏财坐正印", Arrays.asList(
            "[婚姻][性格]不论身强身弱均吉，主做事温和理智，外得贵人助，内家庭和谐，婚姻美满。身强者福较大。"
        ));

        // 偏财在时柱其他规则
        KNOWLEDGE_BASE.put("偏财在时柱其他规则", Arrays.asList(
            "[家庭背景]命局偏财为用神时，父亲多助力，姨妾多助力，兄弟朋友姊妹少助力，不宜合伙，母寿短于父，对太太不体贴。",
            "[事业][身强]身强以比劫星为忌神，特别是身强财弱者忌合伙经营事业。",
            "[事业][身弱]身弱以比劫星为喜用，适合合伙经营事业。"
        ));

        // 比肩在年柱
        KNOWLEDGE_BASE.put("比肩在年柱", Arrays.asList(
            "[家庭背景]有兄弟姐妹，或为养子。",
            "[性格]独立心强，有分家或创业倾向。"
        ));

        // 劫财在年柱
        KNOWLEDGE_BASE.put("劫财在年柱", Arrays.asList(
            "[家庭背景]上有兄姐或养子，喜独立分家。",
            "[性格]野心大，欲望强烈。"
        ));

        // 食神在年柱
        KNOWLEDGE_BASE.put("食神在年柱", Arrays.asList(
            "[家庭背景]受祖上福荫，事业发展顺利。",
            "[性格]性格温和，心胸宽广。"
        ));

        // 伤官在年柱
        KNOWLEDGE_BASE.put("伤官在年柱", Arrays.asList(
            "[家庭背景]祖业飘零。",
            "[性格]少年时期聪明有傲气。"
        ));

        // 正官在年柱
        KNOWLEDGE_BASE.put("正官在年柱", Arrays.asList(
            "[家庭背景]出身名门，受祖荫大。",
            "[性格]品行端正，有责任感。"
        ));

        // 七杀在年柱
        KNOWLEDGE_BASE.put("七杀在年柱", Arrays.asList(
            "[家庭背景]祖上或父母权贵，但可能早年辛苦。",
            "[性格]性格刚毅，有领导能力。"
        ));

        // 正印在年柱
        KNOWLEDGE_BASE.put("正印在年柱", Arrays.asList(
            "[家庭背景]出身清白，家庭有文化氛围。",
            "[性格]聪明仁慈，重名誉。"
        ));

        // 偏印在年柱
        KNOWLEDGE_BASE.put("偏印在年柱", Arrays.asList(
            "[家庭背景]祖业零落，家境清贫。",
            "[性格]独立性强，思想怪异。"
        ));
    }

    /**
     * 分析四柱十神论命
     */
    public Map<String, List<String>> analyze(BaZiChart chart) {
        Map<String, List<String>> result = new LinkedHashMap<>();
        
        String riZhu = chart.getRiZhuTianGan();
        
        // 分析日主坐支
        String riZhiShiShen = getMainCangGanShiShen(chart.getRiZhu(), riZhu);
        if (riZhiShiShen != null) {
            String key = "日主坐" + riZhiShiShen;
            if (KNOWLEDGE_BASE.containsKey(key)) {
                result.put(key, KNOWLEDGE_BASE.get(key));
            }
        }
        
        // 分析各柱十神
        analyzeYearPillar(chart, result);
        analyzeMonthPillar(chart, result);
        analyzeHourPillar(chart, result);
        
        return result;
    }

    private String getMainCangGanShiShen(Pillar pillar, String riZhu) {
        if (!pillar.getDiZhiCangGan().isEmpty()) {
            String mainCangGan = pillar.getDiZhiCangGan().get(0);
            return ShiShen.calculate(riZhu, mainCangGan).getName();
        }
        return null;
    }

    private void analyzeYearPillar(BaZiChart chart, Map<String, List<String>> result) {
        Pillar nianZhu = chart.getNianZhu();
        String riZhu = chart.getRiZhuTianGan();
        
        // 年柱地支十神
        for (String cangGan : nianZhu.getDiZhiCangGan()) {
            ShiShen ss = ShiShen.calculate(riZhu, cangGan);
            String key = ss.getName() + "在年柱地支";
            if (KNOWLEDGE_BASE.containsKey(key)) {
                result.put(key, KNOWLEDGE_BASE.get(key));
            }
        }
    }

    private void analyzeMonthPillar(BaZiChart chart, Map<String, List<String>> result) {
        Pillar yueZhu = chart.getYueZhu();
        String riZhu = chart.getRiZhuTianGan();
        
        // 月柱天干十神
        String tianGanSs = ShiShen.calculate(riZhu, yueZhu.getTianGan()).getName();
        String key1 = tianGanSs + "在月柱天干";
        if (KNOWLEDGE_BASE.containsKey(key1)) {
            result.put(key1, KNOWLEDGE_BASE.get(key1));
        }
        
        // 月柱地支十神
        for (String cangGan : yueZhu.getDiZhiCangGan()) {
            ShiShen ss = ShiShen.calculate(riZhu, cangGan);
            String key2 = ss.getName() + "在月柱地支";
            if (KNOWLEDGE_BASE.containsKey(key2)) {
                result.put(key2, KNOWLEDGE_BASE.get(key2));
            }
        }
        
        // 月柱组合
        if (!yueZhu.getDiZhiCangGan().isEmpty()) {
            String diZhiMainSs = ShiShen.calculate(riZhu, yueZhu.getDiZhiCangGan().get(0)).getName();
            String combKey = "月柱" + tianGanSs + "坐" + diZhiMainSs;
            if (KNOWLEDGE_BASE.containsKey(combKey)) {
                result.put(combKey, KNOWLEDGE_BASE.get(combKey));
            }
        }
    }

    private void analyzeHourPillar(BaZiChart chart, Map<String, List<String>> result) {
        Pillar shiZhu = chart.getShiZhu();
        String riZhu = chart.getRiZhuTianGan();
        
        String tianGanSs = ShiShen.calculate(riZhu, shiZhu.getTianGan()).getName();
        
        // 时柱地支十神
        for (String cangGan : shiZhu.getDiZhiCangGan()) {
            ShiShen ss = ShiShen.calculate(riZhu, cangGan);
            String key = ss.getName() + "在时柱地支";
            if (KNOWLEDGE_BASE.containsKey(key)) {
                result.put(key, KNOWLEDGE_BASE.get(key));
            }
        }
        
        // 时柱组合
        for (String cangGan : shiZhu.getDiZhiCangGan()) {
            String diZhiSs = ShiShen.calculate(riZhu, cangGan).getName();
            String combKey = "时柱" + tianGanSs + "坐" + diZhiSs;
            if (KNOWLEDGE_BASE.containsKey(combKey)) {
                result.put(combKey, KNOWLEDGE_BASE.get(combKey));
            }
        }
        
        // 时柱其他规则
        String otherKey = tianGanSs + "在时柱其他规则";
        if (KNOWLEDGE_BASE.containsKey(otherKey)) {
            result.put(otherKey, KNOWLEDGE_BASE.get(otherKey));
        }
    }
}

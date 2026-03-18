package com.example.demo.bazi.analyzer;

import com.example.demo.bazi.constants.*;
import com.example.demo.bazi.model.*;
import java.util.*;

/**
 * 日柱分析器 - 分析日柱等级信息
 */
public class RiZhuAnalyzer {

    // 上等日柱
    private static final Set<String> SHANG_DENG = new HashSet<>(Arrays.asList(
        "甲子", "乙丑", "丙寅", "丁卯", "戊辰", "己巳",
        "庚午", "辛未", "壬申", "癸酉", "甲戌", "乙亥",
        "丙子", "丁丑", "戊寅", "己卯", "庚辰", "辛巳",
        "壬午", "癸未"
    ));

    // 中等日柱
    private static final Set<String> ZHONG_DENG = new HashSet<>(Arrays.asList(
        "甲辰", "乙巳", "丙午", "丁未", "戊申", "己酉",
        "庚戌", "辛亥", "壬子", "癸丑", "甲寅", "乙卯",
        "丙辰", "丁巳", "戊午", "己未", "庚申", "辛酉",
        "壬戌", "癸亥"
    ));

    // 日柱类象
    private static final Map<String, String> LEI_XIANG = new HashMap<>();
    
    static {
        LEI_XIANG.put("甲子", "海中金鼠");
        LEI_XIANG.put("乙丑", "海中金牛");
        LEI_XIANG.put("丙寅", "炉中火虎");
        LEI_XIANG.put("丁卯", "炉中火兔");
        LEI_XIANG.put("戊辰", "大林木龙");
        LEI_XIANG.put("己巳", "大林木蛇");
        LEI_XIANG.put("庚午", "路旁土马");
        LEI_XIANG.put("辛未", "路旁土羊");
        LEI_XIANG.put("壬申", "剑锋金猴");
        LEI_XIANG.put("癸酉", "剑锋金鸡");
        LEI_XIANG.put("甲戌", "山头火狗");
        LEI_XIANG.put("乙亥", "山头火猪");
        LEI_XIANG.put("丙子", "涧下水鼠");
        LEI_XIANG.put("丁丑", "涧下水牛");
        LEI_XIANG.put("戊寅", "城头土虎");
        LEI_XIANG.put("己卯", "城头土兔");
        LEI_XIANG.put("庚辰", "白蜡金龙");
        LEI_XIANG.put("辛巳", "白蜡金蛇");
        LEI_XIANG.put("壬午", "杨柳木马");
        LEI_XIANG.put("癸未", "杨柳木羊");
        LEI_XIANG.put("甲申", "泉中水猴");
        LEI_XIANG.put("乙酉", "泉中水鸡");
        LEI_XIANG.put("丙戌", "屋上土狗");
        LEI_XIANG.put("丁亥", "屋上土猪");
        LEI_XIANG.put("戊子", "霹雳火鼠");
        LEI_XIANG.put("己丑", "霹雳火牛");
        LEI_XIANG.put("庚寅", "松柏木虎");
        LEI_XIANG.put("辛卯", "松柏木兔");
        LEI_XIANG.put("壬辰", "长流水龙");
        LEI_XIANG.put("癸巳", "长流水蛇");
        LEI_XIANG.put("甲午", "沙中金马");
        LEI_XIANG.put("乙未", "沙中金羊");
        LEI_XIANG.put("丙申", "山下火猴");
        LEI_XIANG.put("丁酉", "山下火鸡");
        LEI_XIANG.put("戊戌", "平地木狗");
        LEI_XIANG.put("己亥", "平地木猪");
        LEI_XIANG.put("庚子", "壁上土鼠");
        LEI_XIANG.put("辛丑", "壁上土牛");
        LEI_XIANG.put("壬寅", "金箔金虎");
        LEI_XIANG.put("癸卯", "金箔金兔");
        LEI_XIANG.put("甲辰", "覆灯火龙");
        LEI_XIANG.put("乙巳", "覆灯火蛇");
        LEI_XIANG.put("丙午", "天河水马");
        LEI_XIANG.put("丁未", "天河水羊");
        LEI_XIANG.put("戊申", "大驿土猴");
        LEI_XIANG.put("己酉", "大驿土鸡");
        LEI_XIANG.put("庚戌", "钗钏金狗");
        LEI_XIANG.put("辛亥", "钗钏金蛇");
        LEI_XIANG.put("壬子", "桑柘木鼠");
        LEI_XIANG.put("癸丑", "桑柘木牛");
        LEI_XIANG.put("甲寅", "大溪水虎");
        LEI_XIANG.put("乙卯", "大溪水兔");
        LEI_XIANG.put("丙辰", "沙中土龙");
        LEI_XIANG.put("丁巳", "沙中土蛇");
        LEI_XIANG.put("戊午", "天上火马");
        LEI_XIANG.put("己未", "天上火羊");
        LEI_XIANG.put("庚申", "石榴木猴");
        LEI_XIANG.put("辛酉", "石榴木鸡");
        LEI_XIANG.put("壬戌", "大海水狗");
        LEI_XIANG.put("癸亥", "大海水猪");
    }

    // 日柱特点
    private static final Map<String, String> RI_TE_DIAN = new HashMap<>();
    
    static {
        RI_TE_DIAN.put("甲辰", "龙守财库日");
        RI_TE_DIAN.put("甲戌", "青龙伏库日");
        RI_TE_DIAN.put("乙丑", "金神日");
        RI_TE_DIAN.put("丙辰", "日德日");
        RI_TE_DIAN.put("丙子", "日德日");
        RI_TE_DIAN.put("戊辰", "日德日");
        RI_TE_DIAN.put("庚辰", "魁罡日");
        RI_TE_DIAN.put("庚戌", "魁罡日");
        RI_TE_DIAN.put("壬辰", "魁罡日");
        RI_TE_DIAN.put("壬戌", "魁罡日");
    }

    /**
     * 分析日柱等级信息
     */
    public Map<String, Object> analyze(BaZiChart chart) {
        Map<String, Object> result = new LinkedHashMap<>();
        
        String riZhuGanZhi = chart.getRiZhu().getGanZhi();
        String tianGan = chart.getRiZhu().getTianGan();
        String diZhi = chart.getRiZhu().getDiZhi();
        
        // 等级
        int level;
        String levelLabel;
        if (SHANG_DENG.contains(riZhuGanZhi)) {
            level = 1;
            levelLabel = "上等日柱";
        } else if (ZHONG_DENG.contains(riZhuGanZhi)) {
            level = 2;
            levelLabel = "中等日柱";
        } else {
            level = 3;
            levelLabel = "下等日柱";
        }
        result.put("等级", level);
        result.put("等级标签", levelLabel);
        
        // 干支关系
        WuXing tianGanWuXing = WuXing.fromName(TianGan.fromName(tianGan).getWuXing());
        WuXing diZhiWuXing = WuXing.fromName(DiZhi.fromName(diZhi).getWuXing());
        String ganZhiRelation = determineGanZhiRelation(tianGanWuXing, diZhiWuXing);
        result.put("干支关系", ganZhiRelation);
        
        // 十二运
        String shiErYun = ShiErChangSheng.calculate(tianGan, diZhi);
        result.put("十二运", shiErYun);
        
        // 吉神凶神
        List<String> jiShen = new ArrayList<>();
        List<String> xiongShen = new ArrayList<>();
        
        if (ShenSha.isShiLing(riZhuGanZhi)) {
            jiShen.add("十灵");
        }
        if (ShenSha.isShiEDaBai(riZhuGanZhi)) {
            xiongShen.add("十恶大败");
        }
        
        result.put("吉神", jiShen);
        result.put("凶神", xiongShen);
        
        // 纳音
        result.put("纳音", NaYin.getNaYin(riZhuGanZhi));
        
        // 类象
        result.put("类象", LEI_XIANG.getOrDefault(riZhuGanZhi, "伏潭之龙"));
        
        // 日特点
        result.put("日特点", RI_TE_DIAN.getOrDefault(riZhuGanZhi, "普通日柱"));
        
        return result;
    }

    private String determineGanZhiRelation(WuXing tianGanWuXing, WuXing diZhiWuXing) {
        if (tianGanWuXing == diZhiWuXing) {
            return "比和";
        } else if (tianGanWuXing.getSheng() == diZhiWuXing) {
            return "上生下";
        } else if (tianGanWuXing.getBeSheng() == diZhiWuXing) {
            return "下生上";
        } else if (tianGanWuXing.getKe() == diZhiWuXing) {
            return "上克下";
        } else if (tianGanWuXing.getBeKe() == diZhiWuXing) {
            return "下克上";
        }
        return "未知";
    }
}

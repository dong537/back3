package com.example.demo.bazi.analyzer;

import com.example.demo.bazi.model.*;
import java.util.*;

/**
 * 调候分析器
 */
public class TiaoHouAnalyzer {

    // 调候用神表：日干 + 月支 -> 调候用神
    private static final Map<String, Map<String, String[]>> TIAO_HOU_TABLE = new HashMap<>();
    
    static {
        // 甲木
        Map<String, String[]> jia = new HashMap<>();
        jia.put("子", new String[]{"丁", "庚"});
        jia.put("丑", new String[]{"丁", "丙", "庚"});
        jia.put("寅", new String[]{"丙", "癸"});
        jia.put("卯", new String[]{"丙", "庚"});
        jia.put("辰", new String[]{"庚", "丙", "癸"});
        jia.put("巳", new String[]{"癸", "丁"});
        jia.put("午", new String[]{"癸", "丁"});
        jia.put("未", new String[]{"癸", "丁"});
        jia.put("申", new String[]{"丁", "庚"});
        jia.put("酉", new String[]{"丁", "庚"});
        jia.put("戌", new String[]{"庚", "丁", "壬"});
        jia.put("亥", new String[]{"庚", "丁", "丙"});
        TIAO_HOU_TABLE.put("甲", jia);
        
        // 乙木
        Map<String, String[]> yi = new HashMap<>();
        yi.put("子", new String[]{"丙", "癸"});
        yi.put("丑", new String[]{"丙", "癸"});
        yi.put("寅", new String[]{"丙", "癸"});
        yi.put("卯", new String[]{"丙", "癸"});
        yi.put("辰", new String[]{"丙", "癸"});
        yi.put("巳", new String[]{"癸"});
        yi.put("午", new String[]{"癸"});
        yi.put("未", new String[]{"癸"});
        yi.put("申", new String[]{"丙", "癸"});
        yi.put("酉", new String[]{"丙", "癸"});
        yi.put("戌", new String[]{"丙", "癸"});
        yi.put("亥", new String[]{"丙", "戊"});
        TIAO_HOU_TABLE.put("乙", yi);
        
        // 丙火
        Map<String, String[]> bing = new HashMap<>();
        bing.put("子", new String[]{"壬", "甲"});
        bing.put("丑", new String[]{"壬", "甲"});
        bing.put("寅", new String[]{"壬", "庚"});
        bing.put("卯", new String[]{"壬", "庚"});
        bing.put("辰", new String[]{"壬", "庚"});
        bing.put("巳", new String[]{"壬", "庚"});
        bing.put("午", new String[]{"壬", "庚"});
        bing.put("未", new String[]{"壬", "庚"});
        bing.put("申", new String[]{"壬", "甲"});
        bing.put("酉", new String[]{"壬", "甲"});
        bing.put("戌", new String[]{"甲", "壬"});
        bing.put("亥", new String[]{"甲", "壬"});
        TIAO_HOU_TABLE.put("丙", bing);
        
        // 丁火
        Map<String, String[]> ding = new HashMap<>();
        ding.put("子", new String[]{"甲", "庚"});
        ding.put("丑", new String[]{"甲", "庚"});
        ding.put("寅", new String[]{"甲", "庚"});
        ding.put("卯", new String[]{"甲", "庚"});
        ding.put("辰", new String[]{"甲", "庚"});
        ding.put("巳", new String[]{"甲", "壬"});
        ding.put("午", new String[]{"甲", "壬"});
        ding.put("未", new String[]{"甲", "壬"});
        ding.put("申", new String[]{"甲", "庚"});
        ding.put("酉", new String[]{"甲", "庚"});
        ding.put("戌", new String[]{"甲", "庚"});
        ding.put("亥", new String[]{"甲", "庚"});
        TIAO_HOU_TABLE.put("丁", ding);
        
        // 戊土
        Map<String, String[]> wu = new HashMap<>();
        wu.put("子", new String[]{"丙", "甲"});
        wu.put("丑", new String[]{"丙", "甲"});
        wu.put("寅", new String[]{"丙", "甲", "癸"});
        wu.put("卯", new String[]{"丙", "癸", "甲"});
        wu.put("辰", new String[]{"丙", "甲", "癸"});
        wu.put("巳", new String[]{"甲", "癸"});
        wu.put("午", new String[]{"甲", "癸"});
        wu.put("未", new String[]{"癸", "甲"});
        wu.put("申", new String[]{"丙", "癸"});
        wu.put("酉", new String[]{"丙", "癸"});
        wu.put("戌", new String[]{"甲", "癸", "丙"});
        wu.put("亥", new String[]{"甲", "丙"});
        TIAO_HOU_TABLE.put("戊", wu);
        
        // 己土
        Map<String, String[]> ji = new HashMap<>();
        ji.put("子", new String[]{"丙", "甲"});
        ji.put("丑", new String[]{"丙", "甲"});
        ji.put("寅", new String[]{"丙", "甲", "癸"});
        ji.put("卯", new String[]{"丙", "癸", "甲"});
        ji.put("辰", new String[]{"丙", "甲", "癸"});
        ji.put("巳", new String[]{"癸", "丙"});
        ji.put("午", new String[]{"癸", "丙"});
        ji.put("未", new String[]{"癸", "丙"});
        ji.put("申", new String[]{"丙", "癸"});
        ji.put("酉", new String[]{"丙", "癸"});
        ji.put("戌", new String[]{"丙", "癸"});
        ji.put("亥", new String[]{"丙", "甲"});
        TIAO_HOU_TABLE.put("己", ji);
        
        // 庚金
        Map<String, String[]> geng = new HashMap<>();
        geng.put("子", new String[]{"丁", "甲"});
        geng.put("丑", new String[]{"丁", "甲", "丙"});
        geng.put("寅", new String[]{"丁", "甲", "丙"});
        geng.put("卯", new String[]{"丁", "甲"});
        geng.put("辰", new String[]{"甲", "丁"});
        geng.put("巳", new String[]{"壬", "丁"});
        geng.put("午", new String[]{"壬", "癸"});
        geng.put("未", new String[]{"壬", "癸"});
        geng.put("申", new String[]{"丁", "甲"});
        geng.put("酉", new String[]{"丁", "甲", "壬"});
        geng.put("戌", new String[]{"甲", "壬"});
        geng.put("亥", new String[]{"丁", "甲"});
        TIAO_HOU_TABLE.put("庚", geng);
        
        // 辛金
        Map<String, String[]> xin = new HashMap<>();
        xin.put("子", new String[]{"丙", "壬"});
        xin.put("丑", new String[]{"丙", "壬"});
        xin.put("寅", new String[]{"壬", "己"});
        xin.put("卯", new String[]{"壬", "甲"});
        xin.put("辰", new String[]{"壬", "甲"});
        xin.put("巳", new String[]{"壬", "己", "癸"});
        xin.put("午", new String[]{"壬", "己", "癸"});
        xin.put("未", new String[]{"壬", "己", "癸"});
        xin.put("申", new String[]{"壬", "甲"});
        xin.put("酉", new String[]{"壬", "甲"});
        xin.put("戌", new String[]{"壬", "甲"});
        xin.put("亥", new String[]{"丙", "壬"});
        TIAO_HOU_TABLE.put("辛", xin);
        
        // 壬水
        Map<String, String[]> ren = new HashMap<>();
        ren.put("子", new String[]{"戊", "丙"});
        ren.put("丑", new String[]{"丙", "甲"});
        ren.put("寅", new String[]{"戊", "丙"});
        ren.put("卯", new String[]{"戊", "辛"});
        ren.put("辰", new String[]{"甲", "庚"});
        ren.put("巳", new String[]{"壬", "辛"});
        ren.put("午", new String[]{"壬", "癸", "庚"});
        ren.put("未", new String[]{"壬", "辛"});
        ren.put("申", new String[]{"戊", "丁"});
        ren.put("酉", new String[]{"甲", "戊"});
        ren.put("戌", new String[]{"甲", "丙"});
        ren.put("亥", new String[]{"戊", "丙"});
        TIAO_HOU_TABLE.put("壬", ren);
        
        // 癸水
        Map<String, String[]> gui = new HashMap<>();
        gui.put("子", new String[]{"丙", "辛"});
        gui.put("丑", new String[]{"丙", "辛"});
        gui.put("寅", new String[]{"辛", "丙"});
        gui.put("卯", new String[]{"辛", "庚"});
        gui.put("辰", new String[]{"丙", "辛", "甲"});
        gui.put("巳", new String[]{"辛", "壬"});
        gui.put("午", new String[]{"庚", "辛", "壬"});
        gui.put("未", new String[]{"庚", "辛"});
        gui.put("申", new String[]{"丁"});
        gui.put("酉", new String[]{"丙", "辛"});
        gui.put("戌", new String[]{"辛", "甲"});
        gui.put("亥", new String[]{"丙", "戊"});
        TIAO_HOU_TABLE.put("癸", gui);
    }

    /**
     * 分析调候信息
     */
    public Map<String, Object> analyze(BaZiChart chart) {
        Map<String, Object> result = new LinkedHashMap<>();
        
        String riGan = chart.getRiZhuTianGan();
        String yueZhi = chart.getYueZhu().getDiZhi();
        String jiJie = chart.getJiJie();
        
        // 获取调候用神
        String[] tiaoHouYongShen = getTiaoHouYongShen(riGan, yueZhi);
        result.put("调候用神", tiaoHouYongShen != null ? Arrays.asList(tiaoHouYongShen) : new ArrayList<>());
        
        // 检查当前八字的调候情况
        Map<String, List<String>> currentTiaoHou = new LinkedHashMap<>();
        currentTiaoHou.put("透干", new ArrayList<>());
        currentTiaoHou.put("支藏", new ArrayList<>());
        
        if (tiaoHouYongShen != null) {
            checkTiaoHou(chart, tiaoHouYongShen, currentTiaoHou);
        }
        result.put("当前八字调候情况", currentTiaoHou);
        
        // 判断调候湿燥等级
        String shiZaoLevel = determineShiZaoLevel(jiJie, currentTiaoHou);
        result.put("调候湿燥等级", shiZaoLevel);
        
        // 知识点
        List<String> zhiShiDian = new ArrayList<>();
        if (currentTiaoHou.get("透干").isEmpty() && currentTiaoHou.get("支藏").isEmpty()) {
            zhiShiDian.add("调候缺乏");
        }
        if ("寒湿".equals(shiZaoLevel)) {
            zhiShiDian.add("调候寒湿");
        } else if ("燥热".equals(shiZaoLevel)) {
            zhiShiDian.add("调候燥热");
        } else if ("中和".equals(shiZaoLevel)) {
            zhiShiDian.add("调候得宜");
        }
        result.put("知识点", zhiShiDian);
        
        return result;
    }

    /**
     * 获取调候用神
     */
    public String[] getTiaoHouYongShen(String riGan, String yueZhi) {
        Map<String, String[]> ganMap = TIAO_HOU_TABLE.get(riGan);
        if (ganMap != null) {
            return ganMap.get(yueZhi);
        }
        return null;
    }

    /**
     * 检查调候情况
     */
    private void checkTiaoHou(BaZiChart chart, String[] tiaoHouYongShen, 
                              Map<String, List<String>> currentTiaoHou) {
        Set<String> tiaoHouSet = new HashSet<>(Arrays.asList(tiaoHouYongShen));
        
        for (Pillar pillar : chart.getAllPillars()) {
            // 检查天干
            if (tiaoHouSet.contains(pillar.getTianGan())) {
                currentTiaoHou.get("透干").add(pillar.getTianGan());
            }
            
            // 检查藏干
            for (String cangGan : pillar.getDiZhiCangGan()) {
                if (tiaoHouSet.contains(cangGan)) {
                    currentTiaoHou.get("支藏").add(cangGan);
                }
            }
        }
    }

    /**
     * 判断湿燥等级
     */
    private String determineShiZaoLevel(String jiJie, Map<String, List<String>> currentTiaoHou) {
        boolean hasTouGan = !currentTiaoHou.get("透干").isEmpty();
        boolean hasZhiCang = !currentTiaoHou.get("支藏").isEmpty();
        
        // 冬季或秋季偏寒湿
        if ("冬".equals(jiJie) || "秋".equals(jiJie)) {
            if (!hasTouGan && !hasZhiCang) {
                return "寒湿";
            } else if (hasTouGan) {
                return "中和";
            } else {
                return "偏寒";
            }
        }
        
        // 夏季偏燥热
        if ("夏".equals(jiJie)) {
            if (!hasTouGan && !hasZhiCang) {
                return "燥热";
            } else if (hasTouGan) {
                return "中和";
            } else {
                return "偏燥";
            }
        }
        
        // 春季偏中和
        return hasTouGan || hasZhiCang ? "中和" : "偏寒";
    }
}

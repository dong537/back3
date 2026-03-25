package com.example.demo.yijing.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 六爻动变爻分析服务
 * 根据动爻数量和变化规则进行断卦
 */
@Slf4j
@Service
public class LiuYaoDongBianService {

    /**
     * 分析动变爻
     * @param changingLines 动爻位置列表（1-6）
     * @param originalHexagram 本卦
     * @param changedHexagram 变卦
     * @param yongShen 用神信息
     * @return 动变分析结果
     */
    public Map<String, Object> analyzeDongBian(List<Integer> changingLines, 
                                                String originalHexagram, 
                                                String changedHexagram,
                                                String yongShen) {
        Map<String, Object> result = new LinkedHashMap<>();
        
        if (changingLines == null || changingLines.isEmpty()) {
            // 静卦
            result.put("type", "静卦");
            result.put("priority", "本卦卦辞为主，世应生克为辅");
            result.put("auxiliary", "日月旺衰");
            result.put("interpretation", "状态稳定，无明显变化");
            return result;
        }
        
        int dongYaoCount = changingLines.size();
        result.put("动爻数量", dongYaoCount);
        result.put("动爻位置", changingLines);
        
        // 根据动爻数量确定断卦优先级
        switch (dongYaoCount) {
            case 1:
                result.put("type", "一爻动");
                result.put("priority", "本卦动爻爻辞为主，变卦卦辞为辅");
                result.put("auxiliary", "用神旺衰");
                result.put("interpretation", "变化明确，趋势单一");
                break;
            case 2:
                result.put("type", "两爻动");
                result.put("priority", "取阴爻爻辞为主（阳主过去，阴主未来）");
                result.put("auxiliary", "变卦卦辞");
                result.put("interpretation", "两个变化方向，需权衡");
                break;
            case 3:
                result.put("type", "三爻动");
                result.put("priority", "本卦卦辞+变卦卦辞结合");
                result.put("auxiliary", "世应与用神");
                result.put("interpretation", "变化复杂，多方影响");
                break;
            case 4:
                result.put("type", "四爻动");
                result.put("priority", "取不变两爻爻辞为主");
                result.put("auxiliary", "变卦卦辞");
                result.put("interpretation", "大趋势已定，细节微调");
                break;
            case 5:
                result.put("type", "五爻动");
                result.put("priority", "取不变一爻爻辞为主");
                result.put("auxiliary", "变卦卦辞");
                result.put("interpretation", "大局将变，少数因素稳定");
                break;
            case 6:
                result.put("type", "六爻全动");
                result.put("priority", "乾用九、坤用六，其余用变卦卦辞");
                result.put("auxiliary", "日月能量");
                result.put("interpretation", "彻底变革，全新局面");
                break;
            default:
                result.put("type", "未知");
                result.put("priority", "综合判断");
                break;
        }
        
        // 动变组合分析
        List<Map<String, Object>> dongBianAnalysis = new ArrayList<>();
        for (Integer pos : changingLines) {
            Map<String, Object> analysis = new LinkedHashMap<>();
            analysis.put("位置", pos);
            analysis.put("说明", getDongBianDescription(pos));
            dongBianAnalysis.add(analysis);
        }
        result.put("动变分析", dongBianAnalysis);
        
        // 吉凶倾向（简化判断）
        result.put("吉凶倾向", assessJiXiong(dongYaoCount, yongShen));
        
        return result;
    }

    /**
     * 获取动变描述
     */
    private String getDongBianDescription(int position) {
        String[] descriptions = {
            "初爻：基础层面变化",
            "二爻：内部环境变化",
            "三爻：过渡阶段变化",
            "四爻：外部环境变化",
            "五爻：领导层面变化",
            "上爻：最终结果变化"
        };
        if (position >= 1 && position <= 6) {
            return descriptions[position - 1];
        }
        return "未知位置";
    }

    /**
     * 评估吉凶倾向
     */
    private String assessJiXiong(int dongYaoCount, String yongShen) {
        // 简化判断：动爻数量适中为吉，过多或过少需谨慎
        if (dongYaoCount == 0) {
            return "中平 - 静卦，状态稳定";
        } else if (dongYaoCount == 1 || dongYaoCount == 2) {
            return "吉 - 变化明确，趋势向好";
        } else if (dongYaoCount == 3) {
            return "中平 - 变化复杂，需综合判断";
        } else {
            return "需谨慎 - 变化剧烈，需关注变卦";
        }
    }

    /**
     * 动变组合吉凶速查
     * @param relationship 动爻与用神关系（生、克、比和、泄）
     * @return 吉凶等级
     */
    public String getJiXiongLevel(String relationship) {
        Map<String, String> levelMap = Map.of(
            "生", "★★★★★ 大吉 - 助力用神，加速成功",
            "比和", "★★★★☆ 吉 - 和谐共振，进展平稳",
            "泄", "★☆☆☆☆ 小凶 - 消耗用神，付出增多",
            "克", "★★★★★ 大凶 - 阻碍用神，导致失败"
        );
        return levelMap.getOrDefault(relationship, "中平 - 需综合判断");
    }

    /**
     * 动爻化变爻回头作用
     * @param huaType 化变类型（回头生、回头克、进神、退神、空亡）
     * @param isYongShen 是否用神
     * @return 分析结果
     */
    public Map<String, Object> analyzeHuaBian(String huaType, boolean isYongShen) {
        Map<String, Object> result = new LinkedHashMap<>();
        
        switch (huaType) {
            case "回头生":
                result.put("用神", isYongShen ? "★★★★★ 大吉" : "力量大增");
                result.put("忌神", isYongShen ? "阻力倍增" : "★★★★★ 大凶");
                result.put("含义", "用神加速成功，忌神阻力倍增");
                break;
            case "回头克":
                result.put("用神", isYongShen ? "★★★★★ 大凶" : "力量大减");
                result.put("忌神", isYongShen ? "阻力减弱" : "★★★★★ 大吉");
                result.put("含义", "用神成功受阻，忌神阻力减弱");
                break;
            case "进神":
                result.put("用神", isYongShen ? "★★★★★ 大吉" : "力量递增");
                result.put("忌神", isYongShen ? "趋势向好加速" : "★★★★★ 大凶");
                result.put("含义", "趋势向好/向坏加速");
                break;
            case "退神":
                result.put("用神", isYongShen ? "★★★★★ 大凶" : "力量递减");
                result.put("忌神", isYongShen ? "趋势放缓" : "★★★★★ 大吉");
                result.put("含义", "趋势放缓/减弱");
                break;
            case "空亡":
                result.put("用神", "中平");
                result.put("忌神", "中平");
                result.put("含义", "变化暂时无效，需等待时机");
                break;
            default:
                result.put("用神", "需判断");
                result.put("忌神", "需判断");
                result.put("含义", "需综合判断");
        }
        
        return result;
    }
}

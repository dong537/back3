package com.example.demo.service;

import com.example.demo.entity.BaziInterpretation;
import com.example.demo.mapper.BaziInterpretationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 八字解释服务
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class BaziInterpretationService {

    private final BaziInterpretationMapper baziInterpretationMapper;

    /**
     * 根据十神类型和干支位置获取解释
     */
    public BaziInterpretation getInterpretation(String godType, String ganzhiPosition) {
        return baziInterpretationMapper.findByGodTypeAndPosition(godType, ganzhiPosition);
    }

    /**
     * 批量获取解释（根据多个十神和位置组合）
     */
    public List<BaziInterpretation> getInterpretations(List<Map<String, String>> requests) {
        List<BaziInterpretation> results = new ArrayList<>();
        for (Map<String, String> request : requests) {
            String godType = request.get("godType");
            String ganzhiPosition = request.get("ganzhiPosition");
            if (godType != null && ganzhiPosition != null) {
                BaziInterpretation interpretation = getInterpretation(godType, ganzhiPosition);
                if (interpretation != null) {
                    results.add(interpretation);
                }
            }
        }
        return results;
    }

    /**
     * 根据八字数据提取十神信息并获取对应的解释
     * @param baziData 八字数据（Map格式，包含详细各柱信息）
     * @return 解释列表
     */
    public List<Map<String, Object>> getInterpretationsFromBaziData(Map<String, Object> baziData) {
        List<Map<String, Object>> results = new ArrayList<>();
        
        if (baziData == null) {
            return results;
        }

        // 获取各柱信息
        @SuppressWarnings("unchecked")
        Map<String, Object> detailedPillars = (Map<String, Object>) baziData.get("详细各柱信息");
        if (detailedPillars == null) {
            @SuppressWarnings("unchecked")
            Map<String, Object> fallbackPillars = (Map<String, Object>) baziData.get("八字各柱信息");
            detailedPillars = fallbackPillars;
        }
        
        if (detailedPillars == null) {
            return results;
        }

        // 日支十神
        @SuppressWarnings("unchecked")
        Map<String, Object> riZhu = (Map<String, Object>) detailedPillars.get("日");
        if (riZhu != null) {
            @SuppressWarnings("unchecked")
            List<String> dizhiShiShen = (List<String>) riZhu.get("地支十神");
            if (dizhiShiShen != null && !dizhiShiShen.isEmpty()) {
                String mainShiShen = dizhiShiShen.get(0);
                if (mainShiShen != null && !"日主".equals(mainShiShen)) {
                    BaziInterpretation interpretation = getInterpretation(mainShiShen, "日支");
                    if (interpretation != null) {
                        results.add(buildInterpretationMap(interpretation, "日支", mainShiShen, "rizhi"));
                    }
                }
            }
        }

        // 年干十神
        @SuppressWarnings("unchecked")
        Map<String, Object> nianZhu = (Map<String, Object>) detailedPillars.get("年");
        if (nianZhu != null) {
            String tianganShiShen = (String) nianZhu.get("天干十神");
            if (tianganShiShen != null && !"日主".equals(tianganShiShen)) {
                BaziInterpretation interpretation = getInterpretation(tianganShiShen, "年干");
                if (interpretation != null) {
                    results.add(buildInterpretationMap(interpretation, "年干", tianganShiShen, "niangan"));
                }
            }
            
            // 年支十神
            @SuppressWarnings("unchecked")
            List<String> dizhiShiShen = (List<String>) nianZhu.get("地支十神");
            if (dizhiShiShen != null && !dizhiShiShen.isEmpty()) {
                String mainShiShen = dizhiShiShen.get(0);
                if (mainShiShen != null && !"日主".equals(mainShiShen)) {
                    BaziInterpretation interpretation = getInterpretation(mainShiShen, "年支");
                    if (interpretation != null) {
                        results.add(buildInterpretationMap(interpretation, "年支", mainShiShen, "nianzhi"));
                    }
                }
            }
        }

        // 月干十神
        @SuppressWarnings("unchecked")
        Map<String, Object> yueZhu = (Map<String, Object>) detailedPillars.get("月");
        if (yueZhu != null) {
            String tianganShiShen = (String) yueZhu.get("天干十神");
            if (tianganShiShen != null && !"日主".equals(tianganShiShen)) {
                BaziInterpretation interpretation = getInterpretation(tianganShiShen, "月干");
                if (interpretation != null) {
                    results.add(buildInterpretationMap(interpretation, "月干", tianganShiShen, "yuegan"));
                }
            }
        }

        // 时干十神
        @SuppressWarnings("unchecked")
        Map<String, Object> shiZhu = (Map<String, Object>) detailedPillars.get("时");
        if (shiZhu != null) {
            String tianganShiShen = (String) shiZhu.get("天干十神");
            if (tianganShiShen != null && !"日主".equals(tianganShiShen)) {
                BaziInterpretation interpretation = getInterpretation(tianganShiShen, "时干");
                if (interpretation != null) {
                    results.add(buildInterpretationMap(interpretation, "时干", tianganShiShen, "shigan"));
                }
            }
        }

        return results;
    }

    /**
     * 构建解释Map（用于前端显示）
     */
    public Map<String, Object> buildInterpretationDetailMap(BaziInterpretation interpretation) {
        return buildInterpretationMap(interpretation, interpretation.getGanzhiPosition(), interpretation.getGodType(), String.valueOf(interpretation.getId()));
    }

    private Map<String, Object> buildInterpretationMap(BaziInterpretation interpretation,
                                                       String position,
                                                       String shiShen,
                                                       String id) {
        Map<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("type", position);
        result.put("shiShen", shiShen);
        result.put("title", position + "·" + shiShen);
        result.put("basicDef", interpretation.getBasicDef());
        result.put("mainContent", interpretation.getMainContent());
        result.put("supportContent", interpretation.getSupportContent());
        result.put("restrictContent", interpretation.getRestrictContent());
        result.put("genderDiff", interpretation.getGenderDiff());
        result.put("tag", interpretation.getTag());
        result.put("helpCount", interpretation.getHelpCount() != null ? interpretation.getHelpCount() : 0);
        result.put("unhelpCount", interpretation.getUnhelpCount() != null ? interpretation.getUnhelpCount() : 0);
        result.put("commentCount", interpretation.getCommentCount() != null ? interpretation.getCommentCount() : 0);

        // 新增详细字段
        result.put("loveAdvice", interpretation.getLoveAdvice());
        result.put("careerAdvice", interpretation.getCareerAdvice());
        result.put("wealthAdvice", interpretation.getWealthAdvice());
        result.put("healthAdvice", interpretation.getHealthAdvice());
        result.put("suggestions", interpretation.getSuggestions());
        result.put("avoidances", interpretation.getAvoidances());

        // 评分数据 - 如果数据库没有则根据十神类型生成默认值
        result.put("overallScore", interpretation.getOverallScore() != null ? interpretation.getOverallScore() : generateDefaultScore(shiShen, "overall"));
        result.put("loveScore", interpretation.getLoveScore() != null ? interpretation.getLoveScore() : generateDefaultScore(shiShen, "love"));
        result.put("careerScore", interpretation.getCareerScore() != null ? interpretation.getCareerScore() : generateDefaultScore(shiShen, "career"));
        result.put("wealthScore", interpretation.getWealthScore() != null ? interpretation.getWealthScore() : generateDefaultScore(shiShen, "wealth"));
        result.put("healthScore", interpretation.getHealthScore() != null ? interpretation.getHealthScore() : generateDefaultScore(shiShen, "health"));
        result.put("socialScore", interpretation.getSocialScore() != null ? interpretation.getSocialScore() : generateDefaultScore(shiShen, "social"));

        return result;
    }
    
    /**
     * 根据十神类型生成默认评分
     */
    private int generateDefaultScore(String shiShen, String aspect) {
        // 基础分数
        int baseScore = 70;
        
        // 根据十神类型调整
        switch (shiShen) {
            case "正财":
                if ("wealth".equals(aspect)) return 85;
                if ("career".equals(aspect)) return 78;
                if ("love".equals(aspect)) return 72;
                break;
            case "偏财":
                if ("wealth".equals(aspect)) return 82;
                if ("social".equals(aspect)) return 80;
                if ("love".equals(aspect)) return 75;
                break;
            case "正官":
                if ("career".equals(aspect)) return 88;
                if ("social".equals(aspect)) return 82;
                if ("love".equals(aspect)) return 70;
                break;
            case "七杀":
                if ("career".equals(aspect)) return 80;
                if ("health".equals(aspect)) return 65;
                break;
            case "正印":
                if ("health".equals(aspect)) return 85;
                if ("career".equals(aspect)) return 78;
                break;
            case "偏印":
                if ("career".equals(aspect)) return 75;
                if ("health".equals(aspect)) return 70;
                break;
            case "比肩":
                if ("social".equals(aspect)) return 82;
                if ("career".equals(aspect)) return 75;
                break;
            case "劫财":
                if ("social".equals(aspect)) return 78;
                if ("wealth".equals(aspect)) return 65;
                break;
            case "食神":
                if ("health".equals(aspect)) return 88;
                if ("love".equals(aspect)) return 80;
                if ("social".equals(aspect)) return 82;
                break;
            case "伤官":
                if ("career".equals(aspect)) return 78;
                if ("love".equals(aspect)) return 68;
                break;
        }
        
        // 添加随机波动
        return baseScore + (int)(Math.random() * 15);
    }
}

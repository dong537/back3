package com.example.demo.service;

import com.example.demo.entity.BaziInterpretation;
import com.example.demo.mapper.BaziInterpretationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class BaziInterpretationService {

    private final BaziInterpretationMapper baziInterpretationMapper;

    public BaziInterpretation getInterpretation(String godType, String ganzhiPosition) {
        return baziInterpretationMapper.findByGodTypeAndPosition(godType, ganzhiPosition);
    }

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

    public List<Map<String, Object>> getInterpretationsFromBaziData(Map<String, Object> baziData) {
        List<Map<String, Object>> results = new ArrayList<>();
        if (baziData == null || baziData.isEmpty()) {
            return results;
        }

        Map<String, Object> detailedPillars = resolveDetailedPillars(baziData);
        if (detailedPillars.isEmpty()) {
            return results;
        }

        addRiZhiInterpretation(results, detailedPillars);
        addGanZhiInterpretation(results, detailedPillars, "year", "年干", "年支", "niangan", "nianzhi");
        addGanOnlyInterpretation(results, detailedPillars, "month", "月干", "yuegan");
        addGanOnlyInterpretation(results, detailedPillars, "hour", "时干", "shigan");

        return results;
    }

    public Map<String, Object> buildInterpretationDetailMap(BaziInterpretation interpretation) {
        return buildInterpretationMap(
                interpretation,
                interpretation.getGanzhiPosition(),
                interpretation.getGodType(),
                String.valueOf(interpretation.getId())
        );
    }

    private void addRiZhiInterpretation(List<Map<String, Object>> results, Map<String, Object> pillars) {
        Map<String, Object> dayPillar = asMap(pillars.get("day"));
        if (dayPillar.isEmpty()) {
            dayPillar = asMap(pillars.get("日"));
        }

        List<String> diZhiShiShen = asStringList(firstNonNull(dayPillar.get("diZhiShiShen"), dayPillar.get("地支十神")));
        if (diZhiShiShen.isEmpty()) {
            return;
        }

        String mainShiShen = diZhiShiShen.get(0);
        if (mainShiShen == null || "日主".equals(mainShiShen)) {
            return;
        }

        BaziInterpretation interpretation = getInterpretation(mainShiShen, "日支");
        if (interpretation != null) {
            results.add(buildInterpretationMap(interpretation, "日支", mainShiShen, "rizhi"));
        }
    }

    private void addGanZhiInterpretation(List<Map<String, Object>> results,
                                         Map<String, Object> pillars,
                                         String pillarKey,
                                         String ganPosition,
                                         String zhiPosition,
                                         String ganId,
                                         String zhiId) {
        Map<String, Object> pillar = asMap(pillars.get(pillarKey));
        if (pillar.isEmpty()) {
            pillar = asMap(pillars.get(mapLegacyPillarKey(pillarKey)));
        }
        if (pillar.isEmpty()) {
            return;
        }

        String ganShiShen = asString(firstNonNull(pillar.get("tianGanShiShen"), pillar.get("天干十神")));
        if (ganShiShen != null && !"日主".equals(ganShiShen)) {
            BaziInterpretation interpretation = getInterpretation(ganShiShen, ganPosition);
            if (interpretation != null) {
                results.add(buildInterpretationMap(interpretation, ganPosition, ganShiShen, ganId));
            }
        }

        List<String> diZhiShiShen = asStringList(firstNonNull(pillar.get("diZhiShiShen"), pillar.get("地支十神")));
        if (!diZhiShiShen.isEmpty()) {
            String mainShiShen = diZhiShiShen.get(0);
            if (mainShiShen != null && !"日主".equals(mainShiShen)) {
                BaziInterpretation interpretation = getInterpretation(mainShiShen, zhiPosition);
                if (interpretation != null) {
                    results.add(buildInterpretationMap(interpretation, zhiPosition, mainShiShen, zhiId));
                }
            }
        }
    }

    private void addGanOnlyInterpretation(List<Map<String, Object>> results,
                                          Map<String, Object> pillars,
                                          String pillarKey,
                                          String ganPosition,
                                          String ganId) {
        Map<String, Object> pillar = asMap(pillars.get(pillarKey));
        if (pillar.isEmpty()) {
            pillar = asMap(pillars.get(mapLegacyPillarKey(pillarKey)));
        }
        if (pillar.isEmpty()) {
            return;
        }

        String ganShiShen = asString(firstNonNull(pillar.get("tianGanShiShen"), pillar.get("天干十神")));
        if (ganShiShen == null || "日主".equals(ganShiShen)) {
            return;
        }

        BaziInterpretation interpretation = getInterpretation(ganShiShen, ganPosition);
        if (interpretation != null) {
            results.add(buildInterpretationMap(interpretation, ganPosition, ganShiShen, ganId));
        }
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
        result.put("loveAdvice", interpretation.getLoveAdvice());
        result.put("careerAdvice", interpretation.getCareerAdvice());
        result.put("wealthAdvice", interpretation.getWealthAdvice());
        result.put("healthAdvice", interpretation.getHealthAdvice());
        result.put("suggestions", interpretation.getSuggestions());
        result.put("avoidances", interpretation.getAvoidances());
        result.put("overallScore", interpretation.getOverallScore() != null ? interpretation.getOverallScore() : generateDefaultScore(shiShen, "overall"));
        result.put("loveScore", interpretation.getLoveScore() != null ? interpretation.getLoveScore() : generateDefaultScore(shiShen, "love"));
        result.put("careerScore", interpretation.getCareerScore() != null ? interpretation.getCareerScore() : generateDefaultScore(shiShen, "career"));
        result.put("wealthScore", interpretation.getWealthScore() != null ? interpretation.getWealthScore() : generateDefaultScore(shiShen, "wealth"));
        result.put("healthScore", interpretation.getHealthScore() != null ? interpretation.getHealthScore() : generateDefaultScore(shiShen, "health"));
        result.put("socialScore", interpretation.getSocialScore() != null ? interpretation.getSocialScore() : generateDefaultScore(shiShen, "social"));
        return result;
    }

    private int generateDefaultScore(String shiShen, String aspect) {
        int baseScore = 70;
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
            default:
                break;
        }
        return baseScore + (int) (Math.random() * 15);
    }

    private Map<String, Object> resolveDetailedPillars(Map<String, Object> baziData) {
        Map<String, Object> pillarDetails = asMap(baziData.get("pillarDetails"));
        if (!pillarDetails.isEmpty()) {
            return pillarDetails;
        }

        Map<String, Object> pillars = asMap(baziData.get("pillars"));
        if (!pillars.isEmpty()) {
            return pillars;
        }

        Map<String, Object> legacyDetails = asMap(baziData.get("详细各柱信息"));
        if (!legacyDetails.isEmpty()) {
            return legacyDetails;
        }

        return asMap(baziData.get("八字各柱信息"));
    }

    private String mapLegacyPillarKey(String key) {
        return switch (key) {
            case "year" -> "年";
            case "month" -> "月";
            case "day" -> "日";
            case "hour" -> "时";
            default -> key;
        };
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> asMap(Object value) {
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> result = new LinkedHashMap<>();
            map.forEach((key, item) -> result.put(Objects.toString(key, ""), item));
            return result;
        }
        return new LinkedHashMap<>();
    }

    private List<String> asStringList(Object value) {
        if (value instanceof List<?> list) {
            List<String> result = new ArrayList<>();
            for (Object item : list) {
                if (item != null) {
                    result.add(item.toString());
                }
            }
            return result;
        }
        return new ArrayList<>();
    }

    private String asString(Object value) {
        return value == null ? null : value.toString();
    }

    private Object firstNonNull(Object first, Object second) {
        return first != null ? first : second;
    }
}

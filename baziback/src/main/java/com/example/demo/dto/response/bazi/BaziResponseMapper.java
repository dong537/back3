package com.example.demo.dto.response.bazi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class BaziResponseMapper {

    private static final List<String> PILLAR_KEYS = Arrays.asList("年", "月", "日", "时");
    private static final Map<String, String> PILLAR_LABELS;
    private static final Map<String, String> WU_XING_KEY_MAP;

    static {
        Map<String, String> labels = new LinkedHashMap<>();
        labels.put("年", "year");
        labels.put("月", "month");
        labels.put("日", "day");
        labels.put("时", "hour");
        PILLAR_LABELS = Collections.unmodifiableMap(labels);

        Map<String, String> wuxing = new LinkedHashMap<>();
        wuxing.put("木", "wood");
        wuxing.put("火", "fire");
        wuxing.put("土", "earth");
        wuxing.put("金", "metal");
        wuxing.put("水", "water");
        WU_XING_KEY_MAP = Collections.unmodifiableMap(wuxing);
    }

    private BaziResponseMapper() {
    }

    public static BaziAnalysisResponse fromAnalysisMap(Map<String, Object> source) {
        if (source == null) {
            return null;
        }

        Map<String, BaziPillarResponse> pillars = buildPillars(source);
        Map<String, String> xingYun = normalizeStringMap(source.get("星运"));
        Map<String, String> ziZuo = normalizeStringMap(source.get("自坐"));
        Map<String, String> kongWang = normalizeStringMap(source.get("空亡"));
        Map<String, String> naYin = normalizeStringMap(source.get("纳音"));
        Map<String, List<String>> shenSha = normalizeListMap(source.get("神煞"));
        Map<String, Object> riZhuInfo = asMap(source.get("日柱等级信息"));
        Map<String, Object> xiYongShen = asMap(source.get("喜用神分析"));

        return BaziAnalysisResponse.builder()
                .id(asString(source.get("_id")))
                .baZi(asString(source.get("八字")))
                .birthDateTime(asString(source.get("出生时间")))
                .calculatedTime(asString(source.get("计算用时间")))
                .trueSolarTime(asString(source.get("真太阳时")))
                .trueSolarTimeOffsetMinutes(asInteger(source.get("真太阳时偏移分钟")))
                .longitude(asDouble(source.get("经度")))
                .gender(asString(source.get("性别")))
                .season(asString(source.get("季节")))
                .zodiac(asString(source.get("生肖")))
                .dayMaster(getPillarField(pillars, "day", BaziPillarResponse::getTianGan))
                .dayMasterElement(inferDayMasterElement(pillars, source))
                .bodyStrength(inferBodyStrength(xiYongShen))
                .geJu(asString(source.get("参考格局信息")))
                .renYuanSiLing(asString(source.get("人元司令")))
                .fiveElements(calculateFiveElements(pillars))
                .palaces(buildPalaces(source))
                .pillars(pillars)
                .xingYun(xingYun)
                .ziZuo(ziZuo)
                .kongWang(kongWang)
                .naYin(naYin)
                .shenSha(shenSha)
                .qiYun(buildQiYun(source.get("起运信息")))
                .daYun(buildDaYun(source.get("大运数据")))
                .liuNian(buildLiuNianList(source.get("流年数据")))
                .liuYue(buildLiuYueList(source.get("流月数据")))
                .riZhuInfo(riZhuInfo)
                .xiYongShen(xiYongShen)
                .xingChongHeHui(asMap(source.get("刑冲合会")))
                .yinYangAnalysis(asMap(source.get("阴阳情况分析")))
                .caiXing(asMap(source.get("财星信息")))
                .fuQi(asMap(source.get("夫妻星信息")))
                .fuMu(asMap(source.get("父母星信息")))
                .ziNv(asMap(source.get("子女星信息")))
                .tiaoHou(asMap(source.get("调候信息")))
                .build();
    }

    public static BaziInterpretationResponse fromInterpretationMap(Map<String, Object> source) {
        if (source == null) {
            return null;
        }

        Map<String, Integer> scores = new LinkedHashMap<>();
        scores.put("overall", asInteger(source.get("overallScore")));
        scores.put("love", asInteger(source.get("loveScore")));
        scores.put("career", asInteger(source.get("careerScore")));
        scores.put("wealth", asInteger(source.get("wealthScore")));
        scores.put("health", asInteger(source.get("healthScore")));
        scores.put("social", asInteger(source.get("socialScore")));

        Map<String, String> advices = new LinkedHashMap<>();
        advices.put("love", asString(source.get("loveAdvice")));
        advices.put("career", asString(source.get("careerAdvice")));
        advices.put("wealth", asString(source.get("wealthAdvice")));
        advices.put("health", asString(source.get("healthAdvice")));

        return BaziInterpretationResponse.builder()
                .id(asString(source.get("id")))
                .position(asString(source.get("type")))
                .shiShen(asString(source.get("shiShen")))
                .title(asString(source.get("title")))
                .basicDef(asString(source.get("basicDef")))
                .mainContent(asString(source.get("mainContent")))
                .supportContent(asString(source.get("supportContent")))
                .restrictContent(asString(source.get("restrictContent")))
                .genderDiff(asString(source.get("genderDiff")))
                .tags(splitTags(asString(source.get("tag"))))
                .helpCount(defaultInt(asInteger(source.get("helpCount"))))
                .unhelpCount(defaultInt(asInteger(source.get("unhelpCount"))))
                .commentCount(defaultInt(asInteger(source.get("commentCount"))))
                .scores(scores)
                .advices(advices)
                .suggestions(asString(source.get("suggestions")))
                .avoidances(asString(source.get("avoidances")))
                .build();
    }

    private static Map<String, BaziPillarResponse> buildPillars(Map<String, Object> source) {
        Map<String, Object> detailed = asMap(source.get("详细各柱信息"));
        if (detailed.isEmpty()) {
            detailed = asMap(source.get("八字各柱信息"));
        }

        Map<String, BaziPillarResponse> result = new LinkedHashMap<>();
        for (String pillarKey : PILLAR_KEYS) {
            Map<String, Object> pillar = asMap(detailed.get(pillarKey));
            if (pillar.isEmpty()) {
                continue;
            }
            result.put(PILLAR_LABELS.get(pillarKey), BaziPillarResponse.builder()
                    .name(PILLAR_LABELS.get(pillarKey))
                    .tianGan(asString(pillar.get("天干")))
                    .diZhi(asString(pillar.get("地支")))
                    .ganZhi(asString(pillar.get("天干")) + asString(pillar.get("地支")))
                    .tianGanShiShen(asString(pillar.get("天干十神")))
                    .diZhiCangGan(asStringList(pillar.get("地支藏干")))
                    .diZhiShiShen(asStringList(pillar.get("地支十神")))
                    .tianGanWuXing(asString(pillar.get("天干五行")))
                    .diZhiWuXing(asString(pillar.get("地支五行")))
                    .naYin(asString(pillar.get("纳音")))
                    .xingYun(asString(pillar.get("星运")))
                    .ziZuo(asString(pillar.get("自坐")))
                    .kongWang(asString(pillar.get("空亡")))
                    .xunShou(asString(pillar.get("旬首")))
                    .shenSha(asStringList(pillar.get("神煞")))
                    .build());
        }
        return result;
    }

    private static BaziPalaceResponse buildPalaces(Map<String, Object> source) {
        Map<String, Object> palaceMap = asMap(source.get("胎命身"));
        if (palaceMap.isEmpty()) {
            return null;
        }
        return BaziPalaceResponse.builder()
                .taiYuan(asString(palaceMap.get("胎元")))
                .mingGong(asString(palaceMap.get("命宫")))
                .shenGong(asString(palaceMap.get("身宫")))
                .build();
    }

    private static BaziQiYunResponse buildQiYun(Object source) {
        Map<String, Object> qiYun = asMap(source);
        if (qiYun.isEmpty()) {
            return null;
        }
        return BaziQiYunResponse.builder()
                .qiYunAge(extractLeadingInteger(asString(qiYun.get("起运年龄"))))
                .qiYunYear(asInteger(qiYun.get("起运年份")))
                .description(asString(qiYun.get("起运描述")))
                .build();
    }

    private static BaziDaYunResponse buildDaYun(Object source) {
        Map<String, Object> daYun = asMap(source);
        List<Map<String, Object>> cycles = asMapList(daYun.get("大运"));
        if (daYun.isEmpty() && cycles.isEmpty()) {
            return null;
        }

        List<BaziDaYunItemResponse> items = new ArrayList<>();
        for (Map<String, Object> cycle : cycles) {
            items.add(BaziDaYunItemResponse.builder()
                    .ganZhi(asString(cycle.get("干支")))
                    .startYear(asInteger(cycle.get("开始")))
                    .endYear(asInteger(cycle.get("结束")))
                    .tianGan(asString(cycle.get("天干")))
                    .diZhi(asString(cycle.get("地支")))
                    .tianGanShiShen(asString(cycle.get("天干十神")))
                    .diZhiShiShen(asString(cycle.get("地支十神")))
                    .tianGanWuXing(asString(cycle.get("天干五行")))
                    .diZhiWuXing(asString(cycle.get("地支五行")))
                    .naYin(asString(cycle.get("纳音")))
                    .diZhiCangGan(asStringList(cycle.get("地支藏干")))
                    .benQi(asMap(cycle.get("地支本气")))
                    .zhongQi(asMap(cycle.get("地支中气")))
                    .yuQi(asMap(cycle.get("地支余气")))
                    .build());
        }
        return BaziDaYunResponse.builder()
                .startYear(asInteger(daYun.get("起运日期")))
                .cycles(items)
                .build();
    }

    private static List<BaziLiuNianResponse> buildLiuNianList(Object source) {
        List<BaziLiuNianResponse> result = new ArrayList<>();
        for (Map<String, Object> item : asMapList(source)) {
            result.add(BaziLiuNianResponse.builder()
                    .year(asInteger(item.get("年份")))
                    .age(asString(item.get("年龄")))
                    .ganZhi(asString(item.get("干支")))
                    .tianGan(asString(item.get("天干")))
                    .diZhi(asString(item.get("地支")))
                    .tianGanShiShen(asString(item.get("天干十神")))
                    .diZhiShiShen(asString(item.get("地支十神")))
                    .tianGanWuXing(asString(item.get("天干五行")))
                    .diZhiWuXing(asString(item.get("地支五行")))
                    .naYin(asString(item.get("纳音")))
                    .diZhiCangGan(asStringList(item.get("地支藏干")))
                    .build());
        }
        return result;
    }

    private static List<BaziLiuYueResponse> buildLiuYueList(Object source) {
        List<BaziLiuYueResponse> result = new ArrayList<>();
        for (Map<String, Object> item : asMapList(source)) {
            result.add(BaziLiuYueResponse.builder()
                    .month(asInteger(item.get("月份")))
                    .solarTerm(asString(item.get("节气")))
                    .solarTermDate(asString(item.get("节气日期")))
                    .ganZhi(asString(item.get("干支")))
                    .tianGan(asString(item.get("天干")))
                    .diZhi(asString(item.get("地支")))
                    .tianGanShiShen(asString(item.get("天干十神")))
                    .diZhiShiShen(asString(item.get("地支十神")))
                    .tianGanWuXing(asString(item.get("天干五行")))
                    .diZhiWuXing(asString(item.get("地支五行")))
                    .diZhiCangGan(asStringList(item.get("地支藏干")))
                    .build());
        }
        return result;
    }

    private static Map<String, Integer> calculateFiveElements(Map<String, BaziPillarResponse> pillars) {
        Map<String, Integer> result = new LinkedHashMap<>();
        result.put("wood", 0);
        result.put("fire", 0);
        result.put("earth", 0);
        result.put("metal", 0);
        result.put("water", 0);

        for (BaziPillarResponse pillar : pillars.values()) {
            addWuxingScore(result, pillar.getTianGanWuXing(), 25);
            addWuxingScore(result, pillar.getDiZhiWuXing(), 25);
        }
        return result;
    }

    private static void addWuxingScore(Map<String, Integer> result, String wuxing, int score) {
        String key = WU_XING_KEY_MAP.get(wuxing);
        if (key != null) {
            result.put(key, result.getOrDefault(key, 0) + score);
        }
    }

    private static String inferDayMasterElement(Map<String, BaziPillarResponse> pillars, Map<String, Object> source) {
        String fromPillar = getPillarField(pillars, "day", BaziPillarResponse::getTianGanWuXing);
        if (fromPillar != null && !fromPillar.isBlank()) {
            return fromPillar;
        }
        return asString(source.get("日主五行"));
    }

    private static String inferBodyStrength(Map<String, Object> xiYongShen) {
        Object 综合推荐 = xiYongShen.get("综合推荐");
        if (综合推荐 instanceof Map<?, ?> map) {
            Object 判断 = map.get("判断");
            if (判断 != null) {
                return 判断.toString();
            }
        }
        Object 日主强弱推荐 = xiYongShen.get("日主强弱推荐");
        if (日主强弱推荐 instanceof Map<?, ?> map) {
            Object 判断 = map.get("判断");
            if (判断 != null) {
                return 判断.toString();
            }
        }
        return asString(xiYongShen.get("日主强弱"));
    }

    private static Integer extractLeadingInteger(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String digits = value.replaceAll("[^0-9-]", "");
        if (digits.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(digits);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static Map<String, String> normalizeStringMap(Object source) {
        Map<String, String> result = new LinkedHashMap<>();
        Map<String, Object> raw = asMap(source);
        for (String pillarKey : PILLAR_KEYS) {
            String mappedKey = PILLAR_LABELS.get(pillarKey);
            Object value = raw.get(pillarKey);
            if (value instanceof List<?> list) {
                result.put(mappedKey, list.isEmpty() ? "" : Objects.toString(list.get(0), ""));
            } else if (value != null) {
                result.put(mappedKey, value.toString());
            }
        }
        return result;
    }

    private static Map<String, List<String>> normalizeListMap(Object source) {
        Map<String, List<String>> result = new LinkedHashMap<>();
        Map<String, Object> raw = asMap(source);
        for (String pillarKey : PILLAR_KEYS) {
            result.put(PILLAR_LABELS.get(pillarKey), asStringList(raw.get(pillarKey)));
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> asMap(Object value) {
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> result = new LinkedHashMap<>();
            map.forEach((key, item) -> result.put(Objects.toString(key, ""), item));
            return result;
        }
        return new LinkedHashMap<>();
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> asMapList(Object value) {
        if (!(value instanceof List<?> list)) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object item : list) {
            result.add(asMap(item));
        }
        return result;
    }

    private static List<String> asStringList(Object value) {
        if (value instanceof List<?> list) {
            List<String> result = new ArrayList<>();
            for (Object item : list) {
                if (item != null) {
                    result.add(item.toString());
                }
            }
            return result;
        }
        if (value == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(Collections.singletonList(value.toString()));
    }

    private static List<String> splitTags(String tags) {
        if (tags == null || tags.isBlank()) {
            return new ArrayList<>();
        }
        List<String> result = new ArrayList<>();
        for (String tag : tags.split(";")) {
            if (!tag.isBlank()) {
                result.add(tag.trim());
            }
        }
        return result;
    }

    private static String asString(Object value) {
        return value == null ? null : value.toString();
    }

    private static Integer asInteger(Object value) {
        if (value instanceof Integer integer) {
            return integer;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value instanceof String text && !text.isBlank()) {
            try {
                return Integer.parseInt(text.trim());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private static Double asDouble(Object value) {
        if (value instanceof Double aDouble) {
            return aDouble;
        }
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        if (value instanceof String text && !text.isBlank()) {
            try {
                return Double.parseDouble(text.trim());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private static Integer defaultInt(Integer value) {
        return value == null ? 0 : value;
    }

    private static String getPillarField(Map<String, BaziPillarResponse> pillars,
                                         String key,
                                         java.util.function.Function<BaziPillarResponse, String> getter) {
        BaziPillarResponse pillar = pillars.get(key);
        return pillar == null ? null : getter.apply(pillar);
    }
}

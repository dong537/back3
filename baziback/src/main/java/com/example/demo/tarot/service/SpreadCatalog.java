package com.example.demo.tarot.service;

import com.example.demo.tarot.model.SpreadPosition;
import com.example.demo.tarot.model.SpreadType;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class SpreadCatalog {

    private final Map<SpreadType, List<SpreadPosition>> catalog = new EnumMap<>(SpreadType.class);

    public SpreadCatalog() {
        catalog.put(SpreadType.SINGLE, List.of(
                new SpreadPosition("CORE", "核心指引", "当前最关键主题/建议焦点")
        ));

        catalog.put(SpreadType.PAST_PRESENT_FUTURE, List.of(
                new SpreadPosition("PAST", "过去", "背景与已形成的影响"),
                new SpreadPosition("PRESENT", "现在", "当下状态与主要矛盾"),
                new SpreadPosition("FUTURE", "未来", "维持现路径下的趋势（可调整）")
        ));

        catalog.put(SpreadType.LOVE_TRIAD, List.of(
                new SpreadPosition("FOUNDATION", "缘分基础", "关系底层动力与吸引点"),
                new SpreadPosition("DYNAMIC", "当前状态", "互动现状与需求摩擦"),
                new SpreadPosition("ADVICE", "行动指引", "推进策略与边界/沟通建议")
        ));

        catalog.put(SpreadType.CELTIC_CROSS, List.of(
                new SpreadPosition("1_PRESENT", "现状", "问题核心处境"),
                new SpreadPosition("2_CHALLENGE", "阻碍/助力", "主要挑战或推动力"),
                new SpreadPosition("3_ROOT", "根源", "深层动机与潜因"),
                new SpreadPosition("4_PAST", "过去", "已发生影响"),
                new SpreadPosition("5_CROWN", "目标", "期待方向与计划"),
                new SpreadPosition("6_NEAR_FUTURE", "近期未来", "短期变化"),
                new SpreadPosition("7_SELF", "你自己", "心态与策略"),
                new SpreadPosition("8_ENV", "环境/他人", "外界条件"),
                new SpreadPosition("9_HOPE_FEAR", "希望与恐惧", "内心拉扯"),
                new SpreadPosition("10_OUTCOME", "结果趋势", "综合走向（非定论）")
        ));

        catalog.put(SpreadType.HORSESHOE, List.of(
                new SpreadPosition("1_PAST", "过去", "起因与背景"),
                new SpreadPosition("2_PRESENT", "现在", "当下局面"),
                new SpreadPosition("3_HIDDEN", "隐藏影响", "未被看见的变量"),
                new SpreadPosition("4_OBS", "障碍", "风险与阻力"),
                new SpreadPosition("5_ATT", "态度", "相关方倾向"),
                new SpreadPosition("6_ACT", "行动", "下一步建议"),
                new SpreadPosition("7_OUT", "趋势", "可能结果")
        ));

        catalog.put(SpreadType.RELATIONSHIP_CROSS, List.of(
                new SpreadPosition("YOU", "你", "你的立场与需求"),
                new SpreadPosition("OTHER", "对方", "对方的状态与需求"),
                new SpreadPosition("BOND", "纽带", "关系核心模式"),
                new SpreadPosition("CHALLENGE", "挑战", "冲突来源"),
                new SpreadPosition("ADVICE", "建议", "沟通与相处策略")
        ));

        catalog.put(SpreadType.CAREER_PATH, List.of(
                new SpreadPosition("CURRENT", "现状", "当前职业状态"),
                new SpreadPosition("STRENGTH", "优势", "可用资源与能力"),
                new SpreadPosition("BLOCK", "阻碍", "限制与短板"),
                new SpreadPosition("OPP", "机会", "窗口与方向"),
                new SpreadPosition("ACTION", "行动", "推进策略"),
                new SpreadPosition("OUTCOME", "趋势", "中短期走向")
        ));

        catalog.put(SpreadType.DECISION_MAKING, List.of(
                new SpreadPosition("SIT", "现状", "决策背景"),
                new SpreadPosition("A", "方案A", "A路径代价与收益"),
                new SpreadPosition("B", "方案B", "B路径代价与收益"),
                new SpreadPosition("KEY", "关键因素", "成败变量"),
                new SpreadPosition("ADVICE", "建议", "更稳健的选择原则")
        ));

        catalog.put(SpreadType.SPIRITUAL_GUIDANCE, List.of(
                new SpreadPosition("THEME", "主题", "成长课题"),
                new SpreadPosition("BLOCK", "阻碍", "信念/恐惧"),
                new SpreadPosition("PRACTICE", "练习", "可执行练习"),
                new SpreadPosition("GIFT", "礼物", "可能收获")
        ));

        List<SpreadPosition> months = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            months.add(new SpreadPosition("M" + i, i + "月", "该月主旋律与建议"));
        }
        catalog.put(SpreadType.YEAR_AHEAD, Collections.unmodifiableList(months));

        catalog.put(SpreadType.CHAKRA_ALIGNMENT, List.of(
                new SpreadPosition("ROOT", "海底轮", "安全感/生存"),
                new SpreadPosition("SACRAL", "脐轮", "情绪/创造"),
                new SpreadPosition("SOLAR", "太阳神经丛", "意志/边界"),
                new SpreadPosition("HEART", "心轮", "爱/接纳"),
                new SpreadPosition("THROAT", "喉轮", "表达/真实"),
                new SpreadPosition("THIRD_EYE", "眉心轮", "直觉/洞察"),
                new SpreadPosition("CROWN", "顶轮", "意义/整合")
        ));

        catalog.put(SpreadType.SHADOW_WORK, List.of(
                new SpreadPosition("TRIGGER", "触发点", "易被点燃的情境"),
                new SpreadPosition("NEED", "隐藏需求", "真正想要的"),
                new SpreadPosition("DEF", "防御模式", "自我保护方式"),
                new SpreadPosition("TRUTH", "真相", "更客观的结构"),
                new SpreadPosition("INT", "整合", "更成熟的回应"),
                new SpreadPosition("NEXT", "下一步", "可执行微行动")
        ));
    }

    public List<SpreadPosition> positions(SpreadType type) {
        return catalog.getOrDefault(type, List.of());
    }

    public Map<SpreadType, List<SpreadPosition>> all() {
        return Collections.unmodifiableMap(catalog);
    }
}

package com.example.demo.yijing.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 梅花易数体用生克分析服务
 */
@Slf4j
@Service
public class PlumBlossomTiYongService {

    /**
     * 八卦五行对应表
     */
    private static final Map<String, GuaInfo> GUA_WU_XING_MAP = new HashMap<>();

    static {
        GUA_WU_XING_MAP.put("乾", new GuaInfo("金", "西北", "父亲、权威、金属"));
        GUA_WU_XING_MAP.put("坤", new GuaInfo("土", "西南", "母亲、土地、包容"));
        GUA_WU_XING_MAP.put("震", new GuaInfo("木", "东方", "长男、行动、树木"));
        GUA_WU_XING_MAP.put("巽", new GuaInfo("木", "东南", "长女、思维、风"));
        GUA_WU_XING_MAP.put("坎", new GuaInfo("水", "北方", "中男、智慧、水"));
        GUA_WU_XING_MAP.put("离", new GuaInfo("火", "南方", "中女、光明、火"));
        GUA_WU_XING_MAP.put("艮", new GuaInfo("土", "东北", "少男、静止、山"));
        GUA_WU_XING_MAP.put("兑", new GuaInfo("金", "西方", "少女、口舌、金属"));
    }

    /**
     * 五行生克关系
     */
    private static final Map<String, List<String>> WU_XING_SHENG = Map.of(
        "木", Arrays.asList("火"),
        "火", Arrays.asList("土"),
        "土", Arrays.asList("金"),
        "金", Arrays.asList("水"),
        "水", Arrays.asList("木")
    );

    private static final Map<String, List<String>> WU_XING_KE = Map.of(
        "木", Arrays.asList("土"),
        "火", Arrays.asList("金"),
        "土", Arrays.asList("水"),
        "金", Arrays.asList("木"),
        "水", Arrays.asList("火")
    );

    /**
     * 分析体用生克
     * @param upperGua 上卦名称（如：乾、坤）
     * @param lowerGua 下卦名称（如：震、艮）
     * @param changingLinePos 动爻位置（1-6，null表示无动爻）
     * @return 体用生克分析结果
     */
    public Map<String, Object> analyzeTiYong(String upperGua, String lowerGua, Integer changingLinePos) {
        Map<String, Object> result = new LinkedHashMap<>();
        
        GuaInfo upperInfo = GUA_WU_XING_MAP.getOrDefault(upperGua, new GuaInfo("", "", ""));
        GuaInfo lowerInfo = GUA_WU_XING_MAP.getOrDefault(lowerGua, new GuaInfo("", "", ""));
        
        // 确定体用：静卦为体（自己），动爻所在卦为用（事物）
        String tiGua, yongGua;
        String tiWuXing, yongWuXing;
        
        if (changingLinePos == null || changingLinePos == 0) {
            // 无动爻，下卦为体，上卦为用
            tiGua = lowerGua;
            yongGua = upperGua;
            tiWuXing = lowerInfo.getWuXing();
            yongWuXing = upperInfo.getWuXing();
        } else if (changingLinePos <= 3) {
            // 动爻在下卦，下卦为用，上卦为体
            tiGua = upperGua;
            yongGua = lowerGua;
            tiWuXing = upperInfo.getWuXing();
            yongWuXing = lowerInfo.getWuXing();
        } else {
            // 动爻在上卦，上卦为用，下卦为体
            tiGua = lowerGua;
            yongGua = upperGua;
            tiWuXing = lowerInfo.getWuXing();
            yongWuXing = upperInfo.getWuXing();
        }
        
        result.put("体卦", tiGua);
        result.put("用卦", yongGua);
        result.put("体卦五行", tiWuXing);
        result.put("用卦五行", yongWuXing);
        result.put("体卦信息", upperGua.equals(tiGua) ? upperInfo : lowerInfo);
        result.put("用卦信息", upperGua.equals(yongGua) ? upperInfo : lowerInfo);
        
        // 判断生克关系
        String relationship = determineShengKe(tiWuXing, yongWuXing);
        result.put("体用关系", relationship);
        
        // 获取吉凶等级和含义
        Map<String, Object> jiXiong = getJiXiongInfo(relationship);
        result.put("吉凶等级", jiXiong.get("level"));
        result.put("核心含义", jiXiong.get("meaning"));
        result.put("典型场景", jiXiong.get("scenario"));
        
        return result;
    }

    /**
     * 判断五行生克关系
     */
    private String determineShengKe(String tiWuXing, String yongWuXing) {
        if (tiWuXing.equals(yongWuXing)) {
            return "比和";
        }
        
        // 用生体
        if (WU_XING_SHENG.getOrDefault(yongWuXing, Collections.emptyList()).contains(tiWuXing)) {
            return "用生体";
        }
        
        // 体生用
        if (WU_XING_SHENG.getOrDefault(tiWuXing, Collections.emptyList()).contains(yongWuXing)) {
            return "体生用";
        }
        
        // 用克体
        if (WU_XING_KE.getOrDefault(yongWuXing, Collections.emptyList()).contains(tiWuXing)) {
            return "用克体";
        }
        
        // 体克用
        if (WU_XING_KE.getOrDefault(tiWuXing, Collections.emptyList()).contains(yongWuXing)) {
            return "体克用";
        }
        
        return "未知";
    }

    /**
     * 获取吉凶信息
     */
    private Map<String, Object> getJiXiongInfo(String relationship) {
        Map<String, Object> result = new LinkedHashMap<>();
        
        switch (relationship) {
            case "用生体":
                result.put("level", "★★★★★ 大吉");
                result.put("meaning", "外界助力、贵人扶持");
                result.put("scenario", "事业遇贵人、财运广进、感情顺利");
                break;
            case "体克用":
                result.put("level", "★★★☆☆ 小吉");
                result.put("meaning", "主动掌控、克服困难");
                result.put("scenario", "求职顺利、竞争取胜、失物可寻");
                break;
            case "比和":
                result.put("level", "★★★★☆ 吉");
                result.put("meaning", "和谐平衡、合作愉快");
                result.put("scenario", "合作顺畅、感情和睦、团队协作良好");
                break;
            case "体生用":
                result.put("level", "★☆☆☆☆ 小凶");
                result.put("meaning", "消耗精力、付出多回报少");
                result.put("scenario", "投资损耗、工作劳累、感情付出无回应");
                break;
            case "用克体":
                result.put("level", "★★★★★ 大凶");
                result.put("meaning", "外界压制、阻力重重");
                result.put("scenario", "事业遇阻、财运亏损、健康恶化");
                break;
            default:
                result.put("level", "中平");
                result.put("meaning", "需综合判断");
                result.put("scenario", "需结合具体情况分析");
        }
        
        return result;
    }

    /**
     * 获取八卦信息
     */
    public GuaInfo getGuaInfo(String guaName) {
        return GUA_WU_XING_MAP.getOrDefault(guaName, new GuaInfo("", "", ""));
    }

    /**
     * 八卦信息类
     */
    public static class GuaInfo {
        private String wuXing;      // 五行
        private String direction;   // 方位
        private String represents; // 代表事物

        public GuaInfo(String wuXing, String direction, String represents) {
            this.wuXing = wuXing;
            this.direction = direction;
            this.represents = represents;
        }

        public String getWuXing() { return wuXing; }
        public String getDirection() { return direction; }
        public String getRepresents() { return represents; }
    }
}

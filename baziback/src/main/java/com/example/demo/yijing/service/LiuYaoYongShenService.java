package com.example.demo.yijing.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 六爻用神判断服务
 * 根据预测类别确定首选用神和辅助参考
 */
@Slf4j
@Service
public class LiuYaoYongShenService {

    /**
     * 用神对应表
     */
    private static final Map<String, YongShenInfo> YONG_SHEN_MAP = new HashMap<>();

    static {
        // 财运
        YONG_SHEN_MAP.put("财运", new YongShenInfo("妻财爻", Arrays.asList("兄弟爻(忌)", "子孙爻(生财)"), 
            "妻财爻旺相、子孙生财、无兄弟劫财则吉"));
        
        // 事业/求职
        YONG_SHEN_MAP.put("事业", new YongShenInfo("官鬼爻", Arrays.asList("父母爻(生官)", "子孙爻(克官)"), 
            "官鬼爻旺相、父母生官、无子孙克官则吉"));
        YONG_SHEN_MAP.put("求职", new YongShenInfo("官鬼爻", Arrays.asList("父母爻(生官)", "子孙爻(克官)"), 
            "官鬼爻旺相、父母生官、无子孙克官则吉"));
        
        // 学业/考试
        YONG_SHEN_MAP.put("学业", new YongShenInfo("父母爻", Arrays.asList("官鬼爻(成果)", "子孙爻(智慧)"), 
            "父母爻旺相、文书清晰、无冲克则吉"));
        YONG_SHEN_MAP.put("考试", new YongShenInfo("父母爻", Arrays.asList("官鬼爻(成果)", "子孙爻(智慧)"), 
            "父母爻旺相、文书清晰、无冲克则吉"));
        
        // 感情(男测)
        YONG_SHEN_MAP.put("感情_男", new YongShenInfo("妻财爻", Arrays.asList("官鬼爻(竞争对手)"), 
            "妻财爻旺相、与世爻相生相合则吉"));
        
        // 感情(女测)
        YONG_SHEN_MAP.put("感情_女", new YongShenInfo("官鬼爻", Arrays.asList("父母爻(长辈意见)"), 
            "官鬼爻旺相、与世爻相生相合则吉"));
        YONG_SHEN_MAP.put("感情", new YongShenInfo("妻财爻", Arrays.asList("官鬼爻(竞争对手)"), 
            "妻财爻旺相、与世爻相生相合则吉"));
        
        // 健康
        YONG_SHEN_MAP.put("健康", new YongShenInfo("官鬼爻(病症)", Arrays.asList("子孙爻(医药)", "妻财爻(营养)"), 
            "官鬼爻衰弱、子孙爻旺相则吉"));
        
        // 出行/安全
        YONG_SHEN_MAP.put("出行", new YongShenInfo("父母爻(车船)", Arrays.asList("官鬼爻(风险)", "兄弟爻(阻碍)"), 
            "父母爻安稳、官鬼爻衰弱则吉"));
        YONG_SHEN_MAP.put("安全", new YongShenInfo("父母爻(车船)", Arrays.asList("官鬼爻(风险)", "兄弟爻(阻碍)"), 
            "父母爻安稳、官鬼爻衰弱则吉"));
        
        // 官司/纠纷
        YONG_SHEN_MAP.put("官司", new YongShenInfo("官鬼爻(官府)", Arrays.asList("世应爻(双方)", "父母爻(证据)"), 
            "世爻旺相、官鬼爻生世则吉"));
        YONG_SHEN_MAP.put("纠纷", new YongShenInfo("官鬼爻(官府)", Arrays.asList("世应爻(双方)", "父母爻(证据)"), 
            "世爻旺相、官鬼爻生世则吉"));
        
        // 子女/晚辈
        YONG_SHEN_MAP.put("子女", new YongShenInfo("子孙爻", Arrays.asList("妻财爻(生子孙)"), 
            "子孙爻旺相、无官鬼克则吉"));
        YONG_SHEN_MAP.put("晚辈", new YongShenInfo("子孙爻", Arrays.asList("妻财爻(生子孙)"), 
            "子孙爻旺相、无官鬼克则吉"));
        
        // 自身状况
        YONG_SHEN_MAP.put("自身", new YongShenInfo("世爻", Arrays.asList("应爻(环境)"), 
            "世爻旺相、无冲克则吉"));
    }

    /**
     * 根据预测类别获取用神信息
     * @param category 预测类别（如：财运、事业、感情等）
     * @param isMale 是否男性（用于感情类别的判断）
     * @return 用神信息
     */
    public YongShenInfo getYongShen(String category, Boolean isMale) {
        String key = category;
        
        // 处理感情类别的性别区分
        if ("感情".equals(category) || "婚姻".equals(category) || "恋爱".equals(category)) {
            key = isMale != null && isMale ? "感情_男" : "感情_女";
        }
        
        YongShenInfo info = YONG_SHEN_MAP.get(key);
        if (info == null) {
            // 默认用神
            log.warn("未找到类别 {} 的用神配置，使用默认用神", category);
            return new YongShenInfo("世爻", Arrays.asList("应爻"), "世爻旺相、无冲克则吉");
        }
        
        return info;
    }

    /**
     * 获取所有用神类别
     */
    public List<String> getAllCategories() {
        Set<String> categories = new LinkedHashSet<>();
        for (String key : YONG_SHEN_MAP.keySet()) {
            if (!key.contains("_")) {
                categories.add(key);
            }
        }
        return new ArrayList<>(categories);
    }

    /**
     * 用神信息类
     */
    public static class YongShenInfo {
        private String primaryYongShen;      // 首选用神
        private List<String> auxiliaryRefs;  // 辅助参考
        private String judgmentPoints;       // 核心判断要点

        public YongShenInfo(String primaryYongShen, List<String> auxiliaryRefs, String judgmentPoints) {
            this.primaryYongShen = primaryYongShen;
            this.auxiliaryRefs = auxiliaryRefs;
            this.judgmentPoints = judgmentPoints;
        }

        public String getPrimaryYongShen() { return primaryYongShen; }
        public List<String> getAuxiliaryRefs() { return auxiliaryRefs; }
        public String getJudgmentPoints() { return judgmentPoints; }
    }
}

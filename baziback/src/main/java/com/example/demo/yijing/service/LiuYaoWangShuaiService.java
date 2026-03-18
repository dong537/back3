package com.example.demo.yijing.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 六爻旺衰分析服务
 * 根据日月、动爻、变爻分析用神和世爻的旺衰
 */
@Slf4j
@Service
public class LiuYaoWangShuaiService {

    // 五行相生关系
    private static final Map<String, String> WU_XING_SHENG = new HashMap<>();
    static {
        WU_XING_SHENG.put("木", "火");
        WU_XING_SHENG.put("火", "土");
        WU_XING_SHENG.put("土", "金");
        WU_XING_SHENG.put("金", "水");
        WU_XING_SHENG.put("水", "木");
    }
    
    // 五行相克关系
    private static final Map<String, String> WU_XING_KE = new HashMap<>();
    static {
        WU_XING_KE.put("木", "土");
        WU_XING_KE.put("土", "水");
        WU_XING_KE.put("水", "火");
        WU_XING_KE.put("火", "金");
        WU_XING_KE.put("金", "木");
    }
    
    // 地支五行
    private static final Map<String, String> DI_ZHI_WU_XING = new HashMap<>();
    static {
        DI_ZHI_WU_XING.put("子", "水");
        DI_ZHI_WU_XING.put("丑", "土");
        DI_ZHI_WU_XING.put("寅", "木");
        DI_ZHI_WU_XING.put("卯", "木");
        DI_ZHI_WU_XING.put("辰", "土");
        DI_ZHI_WU_XING.put("巳", "火");
        DI_ZHI_WU_XING.put("午", "火");
        DI_ZHI_WU_XING.put("未", "土");
        DI_ZHI_WU_XING.put("申", "金");
        DI_ZHI_WU_XING.put("酉", "金");
        DI_ZHI_WU_XING.put("戌", "土");
        DI_ZHI_WU_XING.put("亥", "水");
    }

    /**
     * 分析用神旺衰
     * @param yongShenBranch 用神地支
     * @param yueJian 月建
     * @param riChen 日辰
     * @param changingLines 动爻位置列表
     * @param yongShenLiuQin 用神六亲
     * @param allYaos 所有爻信息（包含六亲、地支等）
     * @return 旺衰分析结果
     */
    public WangShuaiResult analyzeYongShen(String yongShenBranch, String yueJian, String riChen,
                                           List<Integer> changingLines, String yongShenLiuQin,
                                           List<YaoInfo> allYaos) {
        WangShuaiResult result = new WangShuaiResult();
        result.setYongShenBranch(yongShenBranch);
        result.setYongShenLiuQin(yongShenLiuQin);
        
        // 1. 月建旺衰（最重要）
        String yueJianWuXing = DI_ZHI_WU_XING.get(yueJian);
        String yongShenWuXing = DI_ZHI_WU_XING.get(yongShenBranch);
        
        String yueJianStatus = "平";
        if (yueJian.equals(yongShenBranch)) {
            yueJianStatus = "★★★★★ 临月建，极旺";
        } else if (WU_XING_SHENG.get(yueJianWuXing).equals(yongShenWuXing)) {
            yueJianStatus = "★★★★☆ 得月建生，旺";
        } else if (WU_XING_KE.get(yueJianWuXing).equals(yongShenWuXing)) {
            yueJianStatus = "★☆☆☆☆ 被月建克，衰";
        }
        result.setYueJianStatus(yueJianStatus);
        
        // 2. 日辰旺衰
        String riChenWuXing = DI_ZHI_WU_XING.get(riChen);
        String riChenStatus = "平";
        if (riChen.equals(yongShenBranch)) {
            riChenStatus = "★★★★★ 临日辰，极旺";
        } else if (WU_XING_SHENG.get(riChenWuXing).equals(yongShenWuXing)) {
            riChenStatus = "★★★☆☆ 得日辰生，较旺";
        } else if (WU_XING_KE.get(riChenWuXing).equals(yongShenWuXing)) {
            riChenStatus = "★★☆☆☆ 被日辰克，较衰";
        }
        result.setRiChenStatus(riChenStatus);
        
        // 3. 动爻影响
        List<String> dongYaoEffects = new ArrayList<>();
        if (changingLines != null && !changingLines.isEmpty()) {
            for (Integer pos : changingLines) {
                if (pos > 0 && pos <= allYaos.size()) {
                    YaoInfo yao = allYaos.get(pos - 1);
                    String effect = analyzeDongYaoEffect(yao, yongShenLiuQin, yongShenWuXing);
                    dongYaoEffects.add("第" + pos + "爻：" + effect);
                }
            }
        }
        result.setDongYaoEffects(dongYaoEffects);
        
        // 4. 综合旺衰判断
        String overallStatus = calculateOverallStatus(yueJianStatus, riChenStatus, dongYaoEffects);
        result.setOverallStatus(overallStatus);
        
        return result;
    }

    /**
     * 分析动爻对用神的影响
     */
    private String analyzeDongYaoEffect(YaoInfo yao, String yongShenLiuQin, String yongShenWuXing) {
        String yaoLiuQin = yao.getLiuQin();
        
        // 动爻六亲与用神的关系
        if (yaoLiuQin.equals(yongShenLiuQin)) {
            return "动爻与用神同类，比和助力";
        } else if (isShengYongShen(yaoLiuQin, yongShenLiuQin)) {
            return "动爻生用神，助力增强";
        } else if (isKeYongShen(yaoLiuQin, yongShenLiuQin)) {
            return "动爻克用神，阻力增大";
        }
        
        return "中性影响";
    }

    /**
     * 判断是否生用神
     */
    private boolean isShengYongShen(String yaoLiuQin, String yongShenLiuQin) {
        // 父母生官鬼，官鬼生父母（简化判断）
        return (yaoLiuQin.equals("父母") && yongShenLiuQin.equals("官鬼")) ||
               (yaoLiuQin.equals("子孙") && yongShenLiuQin.equals("妻财"));
    }

    /**
     * 判断是否克用神
     */
    private boolean isKeYongShen(String yaoLiuQin, String yongShenLiuQin) {
        // 子孙克官鬼，兄弟克妻财
        return (yaoLiuQin.equals("子孙") && yongShenLiuQin.equals("官鬼")) ||
               (yaoLiuQin.equals("兄弟") && yongShenLiuQin.equals("妻财"));
    }

    /**
     * 计算综合旺衰
     */
    private String calculateOverallStatus(String yueJianStatus, String riChenStatus, List<String> dongYaoEffects) {
        // 简化判断：根据月建和日辰状态
        if (yueJianStatus.contains("极旺") || riChenStatus.contains("极旺")) {
            return "★★★★★ 极旺 - 用神力量强大，成功概率高";
        } else if (yueJianStatus.contains("旺") && riChenStatus.contains("旺")) {
            return "★★★★☆ 旺 - 用神状态良好，利于成功";
        } else if (yueJianStatus.contains("衰") || riChenStatus.contains("衰")) {
            return "★☆☆☆☆ 衰 - 用神力量不足，需谨慎";
        }
        return "★★★☆☆ 平 - 用神状态一般，需综合判断";
    }

    /**
     * 旺衰分析结果
     */
    public static class WangShuaiResult {
        private String yongShenBranch;
        private String yongShenLiuQin;
        private String yueJianStatus;
        private String riChenStatus;
        private List<String> dongYaoEffects;
        private String overallStatus;

        // Getters and Setters
        public String getYongShenBranch() { return yongShenBranch; }
        public void setYongShenBranch(String yongShenBranch) { this.yongShenBranch = yongShenBranch; }
        public String getYongShenLiuQin() { return yongShenLiuQin; }
        public void setYongShenLiuQin(String yongShenLiuQin) { this.yongShenLiuQin = yongShenLiuQin; }
        public String getYueJianStatus() { return yueJianStatus; }
        public void setYueJianStatus(String yueJianStatus) { this.yueJianStatus = yueJianStatus; }
        public String getRiChenStatus() { return riChenStatus; }
        public void setRiChenStatus(String riChenStatus) { this.riChenStatus = riChenStatus; }
        public List<String> getDongYaoEffects() { return dongYaoEffects; }
        public void setDongYaoEffects(List<String> dongYaoEffects) { this.dongYaoEffects = dongYaoEffects; }
        public String getOverallStatus() { return overallStatus; }
        public void setOverallStatus(String overallStatus) { this.overallStatus = overallStatus; }
    }

    /**
     * 爻信息（用于旺衰分析）
     */
    public static class YaoInfo {
        private Integer position;
        private String branch;
        private String liuQin;
        private Boolean isDongYao;

        public YaoInfo(Integer position, String branch, String liuQin, Boolean isDongYao) {
            this.position = position;
            this.branch = branch;
            this.liuQin = liuQin;
            this.isDongYao = isDongYao;
        }

        public Integer getPosition() { return position; }
        public String getBranch() { return branch; }
        public String getLiuQin() { return liuQin; }
        public Boolean getIsDongYao() { return isDongYao; }
    }
}

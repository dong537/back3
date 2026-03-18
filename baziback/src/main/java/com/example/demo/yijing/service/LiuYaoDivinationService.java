package com.example.demo.yijing.service;

import com.example.demo.exception.BusinessException;
import com.example.demo.yijing.model.Hexagram;
import com.example.demo.yijing.repository.HexagramRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 六爻占卜服务
 * 整合摇卦、装卦、析卦的完整流程
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LiuYaoDivinationService {

    private final HexagramRepository hexagramRepository;
    private final LiuYaoZhuangGuaService zhuangGuaService;
    private final LiuYaoYongShenService yongShenService;
    private final LiuYaoWangShuaiService wangShuaiService;
    private final LiuYaoDongBianService dongBianService;

    /**
     * 完整六爻占卜流程
     * @param question 占问问题
     * @param category 预测类别（工作/财运/感情等）
     * @param isMale 是否男性（用于感情类判断）
     * @param coinResults 摇卦结果（6次摇卦的硬币状态）
     * @param divinationDate 占卜日期
     * @return 完整的占卜结果
     */
    public Map<String, Object> performDivination(String question, String category, Boolean isMale,
                                                 List<CoinResult> coinResults, LocalDate divinationDate) {
        log.info("开始六爻占卜 - 问题: {}, 类别: {}", question, category);

        // 1. 根据摇卦结果确定本卦
        String binary = buildBinaryFromCoins(coinResults);
        List<Integer> changingLines = findChangingLines(coinResults);
        
        Hexagram original = hexagramRepository.findByBinary(binary)
                .orElse(hexagramRepository.findById(1).orElse(null));
        
        if (original == null) {
            throw new BusinessException("无法确定卦象");
        }

        // 2. 计算变卦
        Hexagram changed = null;
        if (!changingLines.isEmpty()) {
            String changedBinary = buildChangedBinary(binary, changingLines);
            changed = hexagramRepository.findByBinary(changedBinary).orElse(null);
        }

        // 3. 装卦计算
        LiuYaoZhuangGuaService.ZhuangGuaResult zhuangGuaResult = 
                zhuangGuaService.calculateZhuangGua(original.getId(), divinationDate);

        // 4. 用神选取
        LiuYaoYongShenService.YongShenInfo yongShenInfo = yongShenService.getYongShen(category, isMale);
        
        // 找到用神爻
        LiuYaoZhuangGuaService.YaoZhuangGuaInfo yongShenYaoInfo = findYongShenYaoInfo(
                zhuangGuaResult.getYaos(), yongShenInfo.getPrimaryYongShen());

        // 5. 旺衰分析
        List<LiuYaoWangShuaiService.YaoInfo> yaoInfos = convertToYaoInfos(zhuangGuaResult.getYaos(), changingLines);
        LiuYaoWangShuaiService.WangShuaiResult wangShuaiResult = null;
        if (yongShenYaoInfo != null) {
            wangShuaiResult = wangShuaiService.analyzeYongShen(
                    yongShenYaoInfo.getBranch(),
                    zhuangGuaResult.getYueJian(),
                    zhuangGuaResult.getRiChen(),
                    changingLines,
                    yongShenYaoInfo.getLiuQin(),
                    yaoInfos
            );
        }

        // 6. 动变分析
        Map<String, Object> dongBianAnalysis = dongBianService.analyzeDongBian(
                changingLines,
                original.getChinese(),
                changed != null ? changed.getChinese() : null,
                yongShenInfo.getPrimaryYongShen()
        );

        // 7. 构建完整结果
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("question", question);
        result.put("category", category);
        result.put("divination_date", divinationDate.toString());
        
        // 本卦信息
        Map<String, Object> originalMap = new HashMap<>();
        originalMap.put("id", original.getId());
        originalMap.put("name", original.getName());
        originalMap.put("chinese", original.getChinese());
        originalMap.put("symbol", original.getSymbol());
        originalMap.put("binary", binary);
        result.put("original", originalMap);
        
        // 变卦信息
        if (changed != null) {
            Map<String, Object> changedMap = new HashMap<>();
            changedMap.put("id", changed.getId());
            changedMap.put("name", changed.getName());
            changedMap.put("chinese", changed.getChinese());
            changedMap.put("symbol", changed.getSymbol());
            result.put("changed", changedMap);
        }
        
        result.put("changing_lines", changingLines);
        result.put("zhuang_gua", convertZhuangGuaToMap(zhuangGuaResult));
        result.put("yong_shen", convertYongShenToMap(yongShenInfo, yongShenYaoInfo));
        result.put("wang_shuai", convertWangShuaiToMap(wangShuaiResult));
        result.put("dong_bian", dongBianAnalysis);
        
        // 综合判断
        result.put("overall_judgment", generateOverallJudgment(
                yongShenInfo, wangShuaiResult, dongBianAnalysis, zhuangGuaResult
        ));

        return result;
    }

    /**
     * 根据摇卦结果构建二进制字符串
     */
    private String buildBinaryFromCoins(List<CoinResult> coinResults) {
        StringBuilder binary = new StringBuilder();
        for (CoinResult result : coinResults) {
            binary.append(result.getBinary());
        }
        return binary.toString();
    }

    /**
     * 找出动爻位置
     */
    private List<Integer> findChangingLines(List<CoinResult> coinResults) {
        List<Integer> changingLines = new ArrayList<>();
        for (int i = 0; i < coinResults.size(); i++) {
            if (coinResults.get(i).isDongYao()) {
                changingLines.add(i + 1);
            }
        }
        return changingLines;
    }

    /**
     * 构建变卦二进制
     */
    private String buildChangedBinary(String originalBinary, List<Integer> changingLines) {
        char[] binary = originalBinary.toCharArray();
        for (Integer pos : changingLines) {
            int index = pos - 1;
            binary[index] = (binary[index] == '0') ? '1' : '0';
        }
        return new String(binary);
    }

    /**
     * 找到用神爻信息
     */
    private LiuYaoZhuangGuaService.YaoZhuangGuaInfo findYongShenYaoInfo(
            List<LiuYaoZhuangGuaService.YaoZhuangGuaInfo> yaos, String yongShenLiuQin) {
        // 从用神名称中提取六亲（如"妻财爻" -> "妻财"）
        String liuQin = yongShenLiuQin.replace("爻", "").replace("用神", "");
        
        for (LiuYaoZhuangGuaService.YaoZhuangGuaInfo yao : yaos) {
            if (yao.getLiuQin().contains(liuQin) || liuQin.contains(yao.getLiuQin())) {
                return yao;
            }
        }
        // 如果找不到，返回世爻
        return yaos.stream().filter(y -> y.getIsShi()).findFirst().orElse(yaos.get(0));
    }

    /**
     * 转换为YaoInfo列表
     */
    private List<LiuYaoWangShuaiService.YaoInfo> convertToYaoInfos(
            List<LiuYaoZhuangGuaService.YaoZhuangGuaInfo> yaos, List<Integer> changingLines) {
        return yaos.stream()
                .map(yao -> new LiuYaoWangShuaiService.YaoInfo(
                        yao.getYaoPosition(),
                        yao.getBranch(),
                        yao.getLiuQin(),
                        changingLines.contains(yao.getYaoPosition())
                ))
                .collect(Collectors.toList());
    }

    /**
     * 转换装卦结果为Map
     */
    private Map<String, Object> convertZhuangGuaToMap(LiuYaoZhuangGuaService.ZhuangGuaResult zhuangGua) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("hexagram_name", zhuangGua.getHexagramName());
        map.put("palace_nature", zhuangGua.getPalaceNature());
        map.put("yue_jian", zhuangGua.getYueJian());
        map.put("ri_chen", zhuangGua.getRiChen());
        map.put("date_gan_zhi", zhuangGua.getDateGanZhi());
        map.put("kong_wang", zhuangGua.getKongWang());
        
        List<Map<String, Object>> yaos = zhuangGua.getYaos().stream()
                .map(yao -> {
                    Map<String, Object> yaoMap = new LinkedHashMap<>();
                    yaoMap.put("yao_position", yao.getYaoPosition());
                    yaoMap.put("yao_type", yao.getYaoType());
                    yaoMap.put("stem", yao.getStem());
                    yaoMap.put("branch", yao.getBranch());
                    yaoMap.put("liu_qin", yao.getLiuQin());
                    yaoMap.put("is_shi", yao.getIsShi());
                    yaoMap.put("is_ying", yao.getIsYing());
                    yaoMap.put("liu_shen", yao.getLiuShen());
                    yaoMap.put("is_kong_wang", yao.getIsKongWang());
                    yaoMap.put("wang_shuai", yao.getWangShuai());
                    return yaoMap;
                })
                .collect(Collectors.toList());
        map.put("yaos", yaos);
        
        return map;
    }

    /**
     * 转换用神信息为Map
     */
    private Map<String, Object> convertYongShenToMap(LiuYaoYongShenService.YongShenInfo yongShenInfo, 
                                                      LiuYaoZhuangGuaService.YaoZhuangGuaInfo yao) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("primary", yongShenInfo.getPrimaryYongShen());
        map.put("auxiliary", yongShenInfo.getAuxiliaryRefs());
        map.put("judgment_points", yongShenInfo.getJudgmentPoints());
        if (yao != null) {
            map.put("yao_position", yao.getYaoPosition());
            map.put("branch", yao.getBranch());
            map.put("stem", yao.getStem());
            map.put("liu_qin", yao.getLiuQin());
        }
        return map;
    }

    /**
     * 转换旺衰结果为Map
     */
    private Map<String, Object> convertWangShuaiToMap(LiuYaoWangShuaiService.WangShuaiResult wangShuai) {
        if (wangShuai == null) return null;
        
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("yue_jian_status", wangShuai.getYueJianStatus());
        map.put("ri_chen_status", wangShuai.getRiChenStatus());
        map.put("dong_yao_effects", wangShuai.getDongYaoEffects());
        map.put("overall_status", wangShuai.getOverallStatus());
        return map;
    }

    /**
     * 生成综合判断
     */
    private Map<String, Object> generateOverallJudgment(
            LiuYaoYongShenService.YongShenInfo yongShenInfo,
            LiuYaoWangShuaiService.WangShuaiResult wangShuaiResult,
            Map<String, Object> dongBianAnalysis,
            LiuYaoZhuangGuaService.ZhuangGuaResult zhuangGuaResult) {
        
        Map<String, Object> judgment = new LinkedHashMap<>();
        
        // 综合评估
        String overall = "中平";
        if (wangShuaiResult != null && wangShuaiResult.getOverallStatus().contains("极旺")) {
            overall = "大吉";
        } else if (wangShuaiResult != null && wangShuaiResult.getOverallStatus().contains("旺")) {
            overall = "吉";
        } else if (wangShuaiResult != null && wangShuaiResult.getOverallStatus().contains("衰")) {
            overall = "凶";
        }
        
        judgment.put("overall", overall);
        judgment.put("summary", String.format("用神%s，%s", 
                yongShenInfo.getPrimaryYongShen(),
                wangShuaiResult != null ? wangShuaiResult.getOverallStatus() : "需综合判断"));
        judgment.put("suggestion", generateSuggestion(overall, yongShenInfo));
        
        return judgment;
    }

    /**
     * 生成建议
     */
    private String generateSuggestion(String overall, LiuYaoYongShenService.YongShenInfo yongShenInfo) {
        switch (overall) {
            case "大吉":
                return "用神极旺，成功概率很高，可以积极行动，把握机会。";
            case "吉":
                return "用神状态良好，利于成功，建议顺势而为，稳步推进。";
            case "中平":
                return "用神状态一般，需要谨慎行事，综合各方面因素再做决定。";
            case "凶":
                return "用神力量不足，阻力较大，建议暂缓行动，等待更好时机。";
            default:
                return "需要综合判断，建议咨询专业人士。";
        }
    }

    /**
     * 摇卦结果
     */
    public static class CoinResult {
        private int round;
        private List<Integer> coins; // 0=字(阴), 1=背(阳)
        private String yaoType; // 少阳、少阴、老阳、老阴
        private boolean dongYao;
        private int binary; // 0或1

        public CoinResult(int round, List<Integer> coins, String yaoType, boolean dongYao, int binary) {
            this.round = round;
            this.coins = coins;
            this.yaoType = yaoType;
            this.dongYao = dongYao;
            this.binary = binary;
        }

        public int getRound() { return round; }
        public List<Integer> getCoins() { return coins; }
        public String getYaoType() { return yaoType; }
        public boolean isDongYao() { return dongYao; }
        public int getBinary() { return binary; }
    }
}

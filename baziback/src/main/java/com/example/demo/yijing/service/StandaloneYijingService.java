package com.example.demo.yijing.service;

import com.example.demo.dto.request.yijing.YijingGenerateHexagramRequest;
import com.example.demo.dto.request.yijing.YijingInterpretRequest;
import com.example.demo.yijing.model.Hexagram;
import com.example.demo.yijing.model.HexagramResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StandaloneYijingService {

    private final HexagramGeneratorService hexagramGeneratorService;
    private final LiuYaoYongShenService liuYaoYongShenService;
    private final LiuYaoDongBianService liuYaoDongBianService;
    private final PlumBlossomTiYongService plumBlossomTiYongService;

    public Map<String, Object> generateHexagram(YijingGenerateHexagramRequest request) {
        log.info("独立生成卦象 - 问题: {}, 方法: {}", request.getQuestion(), request.getMethod());

        HexagramResult result = hexagramGeneratorService.generateHexagram(
                request.getQuestion(),
                request.getMethod(),
                request.getSeed()
        );

        // 添加六爻和梅花易数分析
        enrichWithAnalysis(result, request.getQuestion(), null);

        return buildGenerateResponse(result);
    }

    /**
     * 丰富卦象结果，添加六爻和梅花易数分析
     */
    private void enrichWithAnalysis(HexagramResult result, String question, Boolean isMale) {
        // 六爻分析
        if (result.getChangingLines() != null && !result.getChangingLines().isEmpty()) {
            // 根据问题推断预测类别
            String category = inferCategory(question);
            LiuYaoYongShenService.YongShenInfo yongShenInfo = liuYaoYongShenService.getYongShen(category, isMale);
            
            Map<String, Object> liuYaoAnalysis = new LinkedHashMap<>();
            liuYaoAnalysis.put("用神信息", Map.of(
                "首选用神", yongShenInfo.getPrimaryYongShen(),
                "辅助参考", yongShenInfo.getAuxiliaryRefs(),
                "核心判断要点", yongShenInfo.getJudgmentPoints()
            ));
            
            Map<String, Object> dongBianAnalysis = liuYaoDongBianService.analyzeDongBian(
                result.getChangingLines(),
                result.getOriginal() != null ? result.getOriginal().getChinese() : "",
                result.getChanged() != null ? result.getChanged().getChinese() : "",
                yongShenInfo.getPrimaryYongShen()
            );
            liuYaoAnalysis.put("动变分析", dongBianAnalysis);
            
            result.setLiuYaoAnalysis(liuYaoAnalysis);
        }
        
        // 梅花易数分析（仅当使用梅花易数方法时）
        if ("plum_blossom".equals(result.getMethod()) && result.getOriginal() != null) {
            String upperGua = result.getOriginal().getUpper();
            String lowerGua = result.getOriginal().getLower();
            Integer changingLinePos = (result.getChangingLines() != null && !result.getChangingLines().isEmpty()) 
                ? result.getChangingLines().get(0) : null;
            
            Map<String, Object> plumBlossomAnalysis = plumBlossomTiYongService.analyzeTiYong(
                upperGua, lowerGua, changingLinePos
            );
            result.setPlumBlossomAnalysis(plumBlossomAnalysis);
        }
    }

    /**
     * 根据问题推断预测类别
     */
    private String inferCategory(String question) {
        if (question == null) return "自身";
        
        String q = question.toLowerCase();
        if (q.contains("财") || q.contains("钱") || q.contains("投资") || q.contains("生意")) {
            return "财运";
        } else if (q.contains("事业") || q.contains("工作") || q.contains("升职") || q.contains("求职")) {
            return "事业";
        } else if (q.contains("考试") || q.contains("学习") || q.contains("学业")) {
            return "考试";
        } else if (q.contains("感情") || q.contains("恋爱") || q.contains("婚姻") || q.contains("复合")) {
            return "感情";
        } else if (q.contains("健康") || q.contains("病") || q.contains("身体")) {
            return "健康";
        } else if (q.contains("出行") || q.contains("旅行") || q.contains("安全")) {
            return "出行";
        } else if (q.contains("官司") || q.contains("纠纷") || q.contains("诉讼")) {
            return "官司";
        } else if (q.contains("子女") || q.contains("孩子") || q.contains("晚辈")) {
            return "子女";
        }
        return "自身";
    }

    public Map<String, Object> interpretHexagram(YijingInterpretRequest request) {
        log.info("解读卦象 - 问题: {}", request.getQuestion());

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", request.getTimestamp());
        response.put("question", request.getQuestion());
        response.put("method", request.getMethod());

        Map<String, Object> interpretation = new HashMap<>();
        
        Hexagram original = convertToHexagram(request.getOriginal());
        interpretation.put("original_hexagram", buildHexagramInterpretation(original, request.getFocus()));

        if (request.getChanged() != null) {
            Hexagram changed = convertToHexagram(request.getChanged());
            interpretation.put("changed_hexagram", buildHexagramInterpretation(changed, request.getFocus()));
        }

        if (request.getChangingLines() != null && !request.getChangingLines().isEmpty()) {
            interpretation.put("changing_lines_interpretation", 
                    buildChangingLinesInterpretation(original, request.getChangingLines()));
        }

        interpretation.put("overall_guidance", buildOverallGuidance(
                original, 
                request.getChanged() != null ? convertToHexagram(request.getChanged()) : null,
                request.getQuestion(),
                request.getContext()
        ));

        response.put("interpretation", interpretation);
        response.put("success", true);

        return response;
    }

    private Map<String, Object> buildGenerateResponse(HexagramResult result) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", result.getTimestamp());
        response.put("method", result.getMethod());
        response.put("question", result.getQuestion());
        response.put("original", convertHexagramToMap(result.getOriginal()));
        
        if (result.getChangingLines() != null && !result.getChangingLines().isEmpty()) {
            response.put("changing_lines", result.getChangingLines());
        }
        
        if (result.getChanged() != null) {
            response.put("changed", convertHexagramToMap(result.getChanged()));
        }
        
        response.put("interpretation_hint", result.getInterpretationHint());
        
        // 添加本卦的爻详细信息
        if (result.getOriginalYaos() != null && !result.getOriginalYaos().isEmpty()) {
            response.put("original_yaos", convertYaosToMapList(result.getOriginalYaos()));
        }
        
        // 添加变卦的爻详细信息
        if (result.getChangedYaos() != null && !result.getChangedYaos().isEmpty()) {
            response.put("changed_yaos", convertYaosToMapList(result.getChangedYaos()));
        }
        
        // 添加六爻分析结果
        if (result.getLiuYaoAnalysis() != null) {
            response.put("liu_yao_analysis", result.getLiuYaoAnalysis());
        }
        
        // 添加梅花易数分析结果
        if (result.getPlumBlossomAnalysis() != null) {
            response.put("plum_blossom_analysis", result.getPlumBlossomAnalysis());
        }
        
        response.put("success", true);

        return response;
    }
    
    /**
     * 将爻列表转换为Map列表
     */
    private List<Map<String, Object>> convertYaosToMapList(List<com.example.demo.entity.TbHexagramYao> yaos) {
        return yaos.stream()
                .map(yao -> {
                    Map<String, Object> yaoMap = new HashMap<>();
                    yaoMap.put("id", yao.getId());
                    yaoMap.put("hexagram_id", yao.getHexagramId());
                    yaoMap.put("yao_position", yao.getYaoPosition());
                    yaoMap.put("yao_type", yao.getYaoType());
                    yaoMap.put("stem", yao.getStem());
                    yaoMap.put("branch", yao.getBranch());
                    yaoMap.put("liu_qin", yao.getLiuQin());
                    yaoMap.put("is_shi", yao.getIsShi());
                    yaoMap.put("is_ying", yao.getIsYing());
                    return yaoMap;
                })
                .collect(Collectors.toList());
    }

    private Map<String, Object> convertHexagramToMap(Hexagram hexagram) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", hexagram.getId());
        map.put("name", hexagram.getName());
        map.put("chinese", hexagram.getChinese());
        map.put("binary", hexagram.getBinary());
        map.put("upper", hexagram.getUpper());
        map.put("lower", hexagram.getLower());
        map.put("symbol", hexagram.getSymbol());
        map.put("judgment", hexagram.getJudgment());
        map.put("judgmentExplanation", hexagram.getJudgmentExplanation());
        map.put("image", hexagram.getImage());
        map.put("imageExplanation", hexagram.getImageExplanation());
        map.put("meaning", hexagram.getMeaning());
        map.put("keywords", hexagram.getKeywords());
        map.put("element", hexagram.getElement());
        map.put("season", hexagram.getSeason());
        map.put("direction", hexagram.getDirection());
        map.put("applications", hexagram.getApplications());
        
        if (hexagram.getLines() != null && !hexagram.getLines().isEmpty()) {
            List<Map<String, Object>> lines = hexagram.getLines().stream()
                    .map(line -> {
                        Map<String, Object> lineMap = new HashMap<>();
                        lineMap.put("position", line.getPosition());
                        lineMap.put("type", line.getType());
                        lineMap.put("text", line.getText());
                        lineMap.put("textExplanation", line.getTextExplanation());
                        lineMap.put("meaning", line.getMeaning());
                        lineMap.put("stem", line.getStem());
                        lineMap.put("branch", line.getBranch());
                        lineMap.put("liuQin", line.getLiuQin());
                        lineMap.put("isShi", line.getIsShi());
                        lineMap.put("isYing", line.getIsYing());
                        return lineMap;
                    })
                    .collect(Collectors.toList());
            map.put("lines", lines);
        }
        
        return map;
    }

    private Hexagram convertToHexagram(YijingInterpretRequest.HexagramData data) {
        Hexagram.HexagramBuilder builder = Hexagram.builder()
                .id(data.getId())
                .name(data.getName())
                .chinese(data.getChinese())
                .binary(data.getBinary())
                .upper(data.getUpper())
                .lower(data.getLower())
                .symbol(data.getSymbol())
                .judgment(data.getJudgment())
                .image(data.getImage())
                .meaning(data.getMeaning())
                .keywords(data.getKeywords())
                .element(data.getElement())
                .season(data.getSeason())
                .direction(data.getDirection())
                .applications(data.getApplications());

        if (data.getLines() != null) {
            List<Hexagram.Line> lines = data.getLines().stream()
                    .map(lineData -> Hexagram.Line.builder()
                            .position(lineData.getPosition())
                            .type(lineData.getType())
                            .text(lineData.getText())
                            .meaning(lineData.getMeaning())
                            .build())
                    .collect(Collectors.toList());
            builder.lines(lines);
        }

        return builder.build();
    }

    private Map<String, Object> buildHexagramInterpretation(Hexagram hexagram, String focus) {
        Map<String, Object> interpretation = new HashMap<>();
        interpretation.put("name", hexagram.getChinese());
        interpretation.put("judgment", hexagram.getJudgment());
        interpretation.put("image", hexagram.getImage());
        interpretation.put("meaning", hexagram.getMeaning());
        interpretation.put("keywords", hexagram.getKeywords());
        interpretation.put("applications", hexagram.getApplications());
        
        return interpretation;
    }

    private List<Map<String, Object>> buildChangingLinesInterpretation(Hexagram hexagram, List<Integer> changingLines) {
        List<Map<String, Object>> interpretations = new ArrayList<>();
        
        for (Integer linePos : changingLines) {
            if (hexagram.getLines() != null && linePos > 0 && linePos <= hexagram.getLines().size()) {
                Hexagram.Line line = hexagram.getLines().get(linePos - 1);
                Map<String, Object> lineInterpretation = new HashMap<>();
                lineInterpretation.put("position", linePos);
                lineInterpretation.put("text", line.getText());
                lineInterpretation.put("meaning", line.getMeaning());
                lineInterpretation.put("significance", "第" + linePos + "爻发动，表示此爻所代表的情况正在发生变化");
                interpretations.add(lineInterpretation);
            }
        }
        
        return interpretations;
    }

    private String buildOverallGuidance(Hexagram original, Hexagram changed, String question, String context) {
        StringBuilder guidance = new StringBuilder();
        
        guidance.append("【卦象总论】\n");
        guidance.append("您问：").append(question).append("\n\n");
        guidance.append("得卦：").append(original.getChinese()).append("卦\n");
        guidance.append("卦辞：").append(original.getJudgment()).append("\n");
        guidance.append("象辞：").append(original.getImage()).append("\n\n");
        
        guidance.append("【卦象解析】\n");
        guidance.append(original.getMeaning()).append("\n\n");
        
        if (original.getApplications() != null) {
            guidance.append("【具体应用】\n");
            original.getApplications().forEach((key, value) -> 
                    guidance.append(key).append("：").append(value).append("\n"));
            guidance.append("\n");
        }
        
        if (changed != null) {
            guidance.append("【变卦分析】\n");
            guidance.append("变卦为：").append(changed.getChinese()).append("卦\n");
            guidance.append("变卦含义：").append(changed.getMeaning()).append("\n\n");
            guidance.append("【发展趋势】\n");
            guidance.append("从").append(original.getChinese()).append("卦变为").append(changed.getChinese())
                    .append("卦，表示事态正在发生转变。");
            guidance.append("需要关注变化的方向，顺势而为。\n\n");
        }
        
        guidance.append("【建议】\n");
        guidance.append("根据卦象显示，建议您");
        if (original.getKeywords() != null && !original.getKeywords().isEmpty()) {
            guidance.append("以").append(String.join("、", original.getKeywords())).append("的态度来处理此事。");
        }
        
        if (context != null && !context.isEmpty()) {
            guidance.append("\n结合您的具体情况：").append(context);
        }
        
        return guidance.toString();
    }

    public List<Map<String, Object>> listAllHexagrams() {
        return hexagramGeneratorService.getAllHexagrams().stream()
                .map(this::convertHexagramToMap)
                .collect(Collectors.toList());
    }

    public Map<String, Object> getHexagramInfo(Integer id) {
        return hexagramGeneratorService.getHexagramById(id)
                .map(this::convertHexagramToMap)
                .orElse(null);
    }
}

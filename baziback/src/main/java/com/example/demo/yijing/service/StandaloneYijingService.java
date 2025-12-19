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

    public Map<String, Object> generateHexagram(YijingGenerateHexagramRequest request) {
        log.info("独立生成卦象 - 问题: {}, 方法: {}", request.getQuestion(), request.getMethod());

        HexagramResult result = hexagramGeneratorService.generateHexagram(
                request.getQuestion(),
                request.getMethod(),
                request.getSeed()
        );

        return buildGenerateResponse(result);
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
        response.put("success", true);

        return response;
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
        map.put("image", hexagram.getImage());
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
                        lineMap.put("meaning", line.getMeaning());
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

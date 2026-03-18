package com.example.demo.yijing.model;

import com.example.demo.entity.TbHexagramYao;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HexagramResult {
    private String timestamp;
    private String method;
    private String question;
    private Hexagram original;
    private List<Integer> changingLines;
    private Hexagram changed;
    private String interpretationHint;
    
    // 本卦的爻详细信息（纳甲、六亲、世应等）
    private List<TbHexagramYao> originalYaos;
    
    // 变卦的爻详细信息
    private List<TbHexagramYao> changedYaos;
    
    // 六爻分析结果
    private Map<String, Object> liuYaoAnalysis;
    
    // 梅花易数分析结果
    private Map<String, Object> plumBlossomAnalysis;
}

package com.example.demo.dto.request.yijing;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class YijingInterpretRequest {

    /**
     * 由 yijing_generate_hexagram 返回的完整卦象对象
     */
    @NotEmpty(message = "hexagram 不能为空")
    private Map<String, Object> hexagram;

    /**
     * 解讀焦點：overall / specific_line / changing
     */
    private String focus;

    /**
     * 當 focus 為 specific_line 時指定爻位
     */
    private Integer lineNumber;

    /**
     * 應用場景：general / career / relationship / health / finance
     */
    private String context;

    /**
     * 解讀詳盡度：brief / standard / detailed
     */
    private String detailLevel;
}


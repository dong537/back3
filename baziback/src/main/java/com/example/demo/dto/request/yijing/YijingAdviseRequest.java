package com.example.demo.dto.request.yijing;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class YijingAdviseRequest {

    /**
     * 由 yijing_generate_hexagram 返回的完整卦象
     */
    @NotEmpty(message = "hexagram 不能为空")
    private Map<String, Object> hexagram;

    @NotBlank(message = "question 不能为空")
    private String question;

    /**
     * 可选的方案列表，用于比较建议
     */
    private List<String> options;

    /**
     * 建议的时间范围：immediate / short_term / long_term
     */
    private String timeFrame;

    /**
     * 可选的附加上下文
     */
    private Map<String, Object> context;
}


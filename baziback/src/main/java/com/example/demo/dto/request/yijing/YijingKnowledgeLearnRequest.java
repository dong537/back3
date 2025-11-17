package com.example.demo.dto.request.yijing;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class YijingKnowledgeLearnRequest {

    @NotBlank(message = "topic 不能为空")
    private String topic;

    /**
     * yijing / bazi / both
     */
    @NotBlank(message = "system 不能为空")
    private String system;

    /**
     * beginner / intermediate / advanced
     */
    @NotBlank(message = "level 不能为空")
    private String level;

    /**
     * text / interactive / visual
     */
    private String format;
}


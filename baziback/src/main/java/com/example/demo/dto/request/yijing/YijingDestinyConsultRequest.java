package com.example.demo.dto.request.yijing;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
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
public class YijingDestinyConsultRequest {

    @NotEmpty(message = "user_profile 不能为空")
    @JsonProperty("user_profile")
    private Map<String, Object> userProfile;

    @NotBlank(message = "question 不能为空")
    private String question;

    @NotBlank(message = "consultation_type 不能为空")
    @JsonProperty("consultation_type")
    private String consultationType;

    /**
     * 历史咨询上下文（可选）
     */
    private List<Map<String, Object>> context;
}


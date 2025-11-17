package com.example.demo.dto.request.yijing;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
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
public class YijingDestinyConsultRequest {

    @NotBlank(message = "question 不能为空")
    private String question;

    @NotBlank(message = "consultation_type 不能为空")
    @JsonProperty("consultation_type")
    private String consultationType;

    private Map<String, Object> context;
}


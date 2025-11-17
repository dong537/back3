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
public class YijingBaziForecastRequest {

    @NotEmpty(message = "chart 不能为空")
    private Map<String, Object> chart;

    @NotBlank(message = "start_date 不能为空")
    @JsonProperty("start_date")
    private String startDate;

    @NotBlank(message = "end_date 不能为空")
    @JsonProperty("end_date")
    private String endDate;

    private List<String> aspects;

    private String resolution;
}


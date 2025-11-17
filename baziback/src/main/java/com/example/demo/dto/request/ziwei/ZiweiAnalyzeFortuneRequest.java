package com.example.demo.dto.request.ziwei;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ZiweiAnalyzeFortuneRequest {

    @NotBlank(message = "chartId 不能为空")
    private String chartId;

    @NotBlank(message = "周期类型不能为空")
    private String period;  // current_year/next_year/decade/custom

    @NotEmpty(message = "分析方面不能为空")
    private List<String> aspects;

    private String startDate;  // 当period=custom时必填，格式：YYYY-MM-DD

    private String endDate;    // 当period=custom时必填，格式：YYYY-MM-DD
}
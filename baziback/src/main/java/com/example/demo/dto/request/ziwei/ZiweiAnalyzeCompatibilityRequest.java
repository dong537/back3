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
public class ZiweiAnalyzeCompatibilityRequest {

    @NotBlank(message = "第一个命盘ID不能为空")
    private String chart1Id;  // 第一人命盘ID

    @NotBlank(message = "第二个命盘ID不能为空")
    private String chart2Id;  // 第二人命盘ID

    @NotBlank(message = "分析类型不能为空")
    private String analysisType;  // marriage/business/friendship

    private List<String> aspects;  // 自定义分析维度（可选）
}
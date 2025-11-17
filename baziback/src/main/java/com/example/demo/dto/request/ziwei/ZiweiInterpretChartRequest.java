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
public class ZiweiInterpretChartRequest {

    @NotBlank(message = "chartId 不能为空")
    private String chartId;  // 工具1生成的命盘ID

    @NotEmpty(message = "解读方面不能为空")
    private List<String> aspects;  // personality/career/wealth/relationships/health/family

    private String detailLevel;  // basic/detailed/comprehensive（默认detailed）
}
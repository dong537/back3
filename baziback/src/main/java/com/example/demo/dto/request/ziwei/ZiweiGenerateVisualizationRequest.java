package com.example.demo.dto.request.ziwei;


import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
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
public class ZiweiGenerateVisualizationRequest {

    @NotBlank(message = "chartId 不能为空")
    private String chartId;

    @NotBlank(message = "可视化类型不能为空")
    private String visualizationType;  // traditional_chart/modern_wheel/palace_grid/star_map

    private List<String> includeElements;  // stars/palaces/four_transformations等

    private String colorscheme;  // classic/modern/minimalist

    private String outputFormat;  // svg/png/html（默认png）
}
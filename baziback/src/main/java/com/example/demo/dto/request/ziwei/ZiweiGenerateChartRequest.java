package com.example.demo.dto.request.ziwei;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ZiweiGenerateChartRequest {

    @NotBlank(message = "出生日期不能为空")
    private String birthDate;  // 格式：YYYY-MM-DD

    @NotBlank(message = "出生时间不能为空")
    private String birthTime;  // 格式：HH:mm

    @NotBlank(message = "性别不能为空")
    private String gender;     // male / female

    private String birthLocation;  // 出生地点（可选，用于真太阳时）

    private String name;      // 姓名（可选）
}
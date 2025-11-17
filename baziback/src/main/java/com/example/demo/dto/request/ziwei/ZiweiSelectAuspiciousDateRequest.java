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
public class ZiweiSelectAuspiciousDateRequest {

    @NotBlank(message = "chartId 不能为空")
    private String chartId;

    @NotBlank(message = "事件类型不能为空")
    private String eventType;  // marriage/business_opening/moving/travel等

    @NotNull(message = "日期范围不能为空")
    private DateRange dateRange;

    private Preferences preferences;  // 偏好设置（可选）

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class DateRange {
        private String start;  // YYYY-MM-DD
        private String end;    // YYYY-MM-DD
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Preferences {
        private Boolean weekendsOnly;    // 仅周末
        private Boolean avoidHolidays;   // 避开节假日
    }
}
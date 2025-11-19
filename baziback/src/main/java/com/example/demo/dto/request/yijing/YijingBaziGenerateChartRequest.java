package com.example.demo.dto.request.yijing;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
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
public class YijingBaziGenerateChartRequest {

    @NotBlank(message = "birth_time 不能为空")
    @JsonProperty("birth_time")
    private String birthTime;

    @JsonProperty("is_lunar")
    private Boolean isLunar;

    /**
     * male / female
     */
    @NotBlank(message = "gender 不能为空")
    private String gender;

    @JsonProperty("birth_location")
    private BirthLocation birthLocation;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class BirthLocation {
        private Double longitude;
        private Double latitude;
    }
}


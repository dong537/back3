package com.example.demo.dto.response.yijing;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class YijingLiuYaoAnalysisResponse {

    @JsonProperty("yong_shen")
    private YijingYongShenResponse yongShen;

    @JsonProperty("dong_bian")
    private YijingDongBianResponse dongBian;
}

package com.example.demo.dto.response.yijing;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
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
public class YijingYongShenResponse {

    @JsonProperty("primary")
    private String primary;

    @JsonProperty("auxiliary")
    private List<String> auxiliary;

    @JsonProperty("judgment_points")
    private String judgmentPoints;
}

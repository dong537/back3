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
public class YijingDongBianResponse {

    @JsonProperty("changing_line_count")
    private Integer changingLineCount;

    @JsonProperty("changing_line_positions")
    private List<Integer> changingLinePositions;

    @JsonProperty("type")
    private String type;

    @JsonProperty("priority")
    private String priority;

    @JsonProperty("auxiliary")
    private String auxiliary;

    @JsonProperty("interpretation")
    private String interpretation;

    @JsonProperty("details")
    private List<YijingDongBianDetailResponse> details;

    @JsonProperty("fortune_tendency")
    private String fortuneTendency;
}

package com.example.demo.dto.response.bazi;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BaziDaYunResponse {
    private Integer startYear;
    @Builder.Default
    private List<BaziDaYunItemResponse> cycles = new ArrayList<>();
}

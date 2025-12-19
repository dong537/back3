package com.example.demo.yijing.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HexagramResult {
    private String timestamp;
    private String method;
    private String question;
    private Hexagram original;
    private List<Integer> changingLines;
    private Hexagram changed;
    private String interpretationHint;
}

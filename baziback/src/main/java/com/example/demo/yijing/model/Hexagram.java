package com.example.demo.yijing.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Hexagram {
    private Integer id;
    private String name;
    private String chinese;
    private String pinyin;
    private String binary;
    private String upper;
    private String lower;
    private String symbol;
    private String judgment;
    private String image;
    private String meaning;
    private List<String> keywords;
    private String element;
    private String season;
    private String direction;
    private Map<String, String> applications;
    private List<Line> lines;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Line {
        private Integer position;
        private String type;
        private String text;
        private String meaning;
    }
}

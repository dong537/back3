package com.example.demo.dto.request.tarot;

import lombok.Data;

@Data
public class SearchCardsRequest {
    private String keyword;
    private String suit;
    private String arcana;
    private String element;
    private String orientation;
    private Integer limit;
}

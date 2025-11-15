package com.example.demo.dto.request.tarot;

import lombok.Data;

@Data
public class GetRandomCardsRequest {
    private Integer count;
    private String suit;
    private String arcana;
    private String element;
}

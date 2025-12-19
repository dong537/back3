package com.example.demo.tarot.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TarotCard {
    private Integer cardId;
    private String cardNameCn;
    private TarotCardType cardType;
    private TarotSuit suit;
    private String meaningUp;
    private String meaningRev;
    private String keyword;
}

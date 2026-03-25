package com.example.demo.entity;

import com.example.demo.tarot.model.TarotCardType;
import com.example.demo.tarot.model.TarotSuit;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonInclude;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TbTarotCard {
    private Integer id;
    private Integer cardId;
    private String cardNameCn;
    private String cardNameEn;
    private TarotCardType cardType;
    private TarotSuit suit;
    
    private Integer number;
    private String symbol;
    private String keywordUp;
    private String keywordRev;
    private String meaningUp;
    private String meaningRev;
    private String description;
    private String interpretationUp;
    private String interpretationRev;
    private String loveUp;
    private String loveRev;
    private String careerUp;
    private String careerRev;
    private String wealthUp;
    private String wealthRev;
    private String healthUp;
    private String healthRev;
    private String adviceUp;
    private String adviceRev;
    private String imageUrl;
    private Integer sortOrder;
}

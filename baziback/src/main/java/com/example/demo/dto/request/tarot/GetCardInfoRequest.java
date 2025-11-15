package com.example.demo.dto.request.tarot;

import lombok.Data;

@Data
public class GetCardInfoRequest {
    private String cardName;
    private String orientation; // upright/reversed
}

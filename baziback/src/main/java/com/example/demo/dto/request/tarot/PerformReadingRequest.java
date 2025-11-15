package com.example.demo.dto.request.tarot;

import lombok.Data;

@Data
public class PerformReadingRequest {
    private String spreadType; // single_card/three_card等
    private String question;
    private String sessionId; // 可选
}

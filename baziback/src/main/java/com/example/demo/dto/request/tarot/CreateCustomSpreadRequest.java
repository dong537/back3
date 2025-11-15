package com.example.demo.dto.request.tarot;

import lombok.Data;

import java.util.List;

@Data
public class CreateCustomSpreadRequest {
    private String spreadName;
    private String description;
    private List<Position> positions;
    private String question;
    private String sessionId; // 可选

    @Data
    public static class Position {
        private String name;
        private String meaning;
    }
}

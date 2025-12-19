package com.example.demo.dto.response.tarot;

import com.example.demo.tarot.model.SpreadPosition;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class TarotSpreadsResponse {
    private Map<String, List<SpreadPosition>> spreads;
}

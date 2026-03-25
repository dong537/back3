package com.example.demo.tarot.model;

import lombok.Data;

@Data
public class SpreadPosition {
    private String key;
    private String name;
    private String businessMeaning;
    
    public SpreadPosition() {
    }
    
    public SpreadPosition(String key, String name, String businessMeaning) {
        this.key = key;
        this.name = name;
        this.businessMeaning = businessMeaning;
    }
}

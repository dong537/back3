package com.example.demo.tarot.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SpreadPosition {
    private String key;
    private String name;
    private String businessMeaning;
}

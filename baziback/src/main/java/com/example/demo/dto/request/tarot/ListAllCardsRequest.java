package com.example.demo.dto.request.tarot;

import lombok.Data;

@Data
public class ListAllCardsRequest {
    private String category; // major_arcanal/minor_arcana/wands等，可选
}

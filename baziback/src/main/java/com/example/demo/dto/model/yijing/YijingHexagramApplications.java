package com.example.demo.dto.model.yijing;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class YijingHexagramApplications {

    private String career;
    private String relationship;
    private String health;
    private String wealth;
    private String study;
}


package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 卦象表实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TbHexagram {
    private Integer id;
    private String name;
    private String nameShort;
    private String upperGua;
    private String lowerGua;
    private String palaceNature;
    private String description;
}

package com.example.demo.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SolarChartRequest {

    @NotNull(message = "阳历时间不能为空")
    private String solarDatetime;

    @NotNull(message = "性别不能为空")
    private Integer gender;

    private Integer ziweiSchool = 1; // 默认南派

    private String name;

    private String birthLocation;
}
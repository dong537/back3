package com.example.demo.dto.response.bazi;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BaziPillarResponse {
    private String name;
    private String tianGan;
    private String diZhi;
    private String ganZhi;
    private String tianGanShiShen;
    @Builder.Default
    private List<String> diZhiCangGan = new ArrayList<>();
    @Builder.Default
    private List<String> diZhiShiShen = new ArrayList<>();
    private String tianGanWuXing;
    private String diZhiWuXing;
    private String naYin;
    private String xingYun;
    private String ziZuo;
    private String kongWang;
    private String xunShou;
    @Builder.Default
    private List<String> shenSha = new ArrayList<>();
}

package com.example.demo.service;

import com.example.demo.client.McpStarClient;
import com.example.demo.dto.request.star.CompatibilityRequest;
import com.example.demo.dto.request.star.DailyHoroscopeRequest;
import com.example.demo.dto.request.star.ZodiacByDateRequest;
import com.example.demo.dto.request.star.ZodiacInfoRequest;
import com.example.demo.dto.response.star.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 星座星象服务实现类
 * 提供星座信息查询、运势分析、配对等功能
 */
@Service
@RequiredArgsConstructor  // 自动生成构造函数注入
@Slf4j
public class ZodiacService {

    // ✅ 保留您的变量名 zodiacClient，注入 McpStarClient
    private final McpStarClient zodiacClient;

    /**
     * 获取星座基础信息
     */
    public ZodiacInfoResponse getZodiacInfo(ZodiacInfoRequest request) {
        log.info("获取星座信息: {}", request);
        return zodiacClient.getZodiacInfo(request);
    }

    /**
     * 获取今日运势
     */
    public DailyHoroscopeResponse getDailyHoroscope(DailyHoroscopeRequest request) {
        log.info("获取今日运势: {}", request);
        return zodiacClient.getDailyHoroscope(request);
    }

    /**
     * 星座配对分析
     */
    public CompatibilityResponse getCompatibility(CompatibilityRequest request) {
        log.info("星座配对分析: {} 与 {}", request.getZodiac1(), request.getZodiac2());
        return zodiacClient.getCompatibility(request);
    }

    /**
     * 根据生日查询星座
     */
    public ZodiacByDateResponse getZodiacByDate(ZodiacByDateRequest request) {
        log.info("根据生日查询星座: {}月{}日", request.getMonth(), request.getDay());
        return zodiacClient.getZodiacByDate(request);
    }

    /**
     * 获取所有星座列表
     */
    public AllZodiacsResponse getAllZodiacs() {
        log.info("获取所有星座列表");
        return zodiacClient.getAllZodiacs();
    }
}
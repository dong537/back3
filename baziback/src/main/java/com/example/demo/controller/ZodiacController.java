package com.example.demo.controller;

import com.example.demo.dto.request.star.CompatibilityRequest;
import com.example.demo.dto.request.star.DailyHoroscopeRequest;
import com.example.demo.dto.request.star.ZodiacByDateRequest;
import com.example.demo.dto.request.star.ZodiacInfoRequest;
import com.example.demo.dto.response.star.*;
import com.example.demo.service.ZodiacService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 星座运势服务控制器
 * 提供星座信息查询、每日运势、星座配对等RESTful API接口
 */
@RestController
@RequestMapping("/api/zodiac")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ZodiacController {

    private final ZodiacService zodiacService;

    /**
     * 获取星座基本信息
     * 查询指定星座的性格特点、幸运元素等基础信息
     *
     * 请求参数：
     * - zodiac: 星座名称 (aries/taurus/gemini/cancer/leo/virgo/libra/scorpio/sagittarius/capricorn/aquarius/pisces)
     *
     * 示例请求：
     * {
     *   "zodiac": "aries"
     * }
     *
     * 测试数据说明：
     * 支持所有12星座查询，当传入无效星座时返回默认信息
     *
     * @param request 星座信息请求参数
     * @return 星座详细信息
     */
    @PostMapping("/info")
    public ResponseEntity<ZodiacInfoResponse> getZodiacInfo(@RequestBody ZodiacInfoRequest request) {
        try {
            log.info("查询星座信息: {}", request.getZodiac());
            ZodiacInfoResponse response = zodiacService.getZodiacInfo(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("查询星座信息失败", e);
            throw e;
        }
    }

    /**
     * 获取每日星座运势
     * 查询指定星座的当日运势分析
     *
     * 请求参数：
     * - zodiac: 星座名称
     * - date: 日期（ISO格式，可选，默认当天）
     * - category: 运势类别 (overall/love/career/health/finance，可选，默认overall)
     *
     * 示例请求：
     * {
     *   "zodiac": "taurus",
     *   "date": "2024-05-20",
     *   "category": "career"
     * }
     *
     * 测试数据说明：
     * 日期范围支持近30天，超出范围返回最近的可用数据
     *
     * @param request 每日运势请求参数
     * @return 每日运势分析结果
     */
    @PostMapping("/daily-horoscope")
    public ResponseEntity<DailyHoroscopeResponse> getDailyHoroscope(@RequestBody DailyHoroscopeRequest request) {
        try {
            DailyHoroscopeResponse response = zodiacService.getDailyHoroscope(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("查询每日运势失败", e);
            throw e;
        }
    }

    /**
     * 星座配对兼容性分析
     * 分析两个星座之间的配对指数和关系建议
     *
     * 请求参数：
     * - zodiac1: 第一个星座
     * - zodiac2: 第二个星座
     * - aspect: 分析方面 (love/friendship/work，可选，默认love)
     *
     * 示例请求：
     * {
     *   "zodiac1": "gemini",
     *   "zodiac2": "libra",
     *   "aspect": "love"
     * }
     *
     * 测试数据说明：
     * 包含所有12x12星座组合的预设配对数据
     *
     * @param request 配对请求参数
     * @return 配对分析结果
     */
    @PostMapping("/compatibility")
    public ResponseEntity<CompatibilityResponse> getCompatibility(@RequestBody CompatibilityRequest request) {
        try {
            log.info("星座配对分析: {} vs {}", request.getZodiac1(), request.getZodiac2());
            CompatibilityResponse response = zodiacService.getCompatibility(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("星座配对分析失败", e);
            throw e;
        }
    }

    /**
     * 根据日期查询星座
     * 根据生日日期确定对应的星座
     *
     * 请求参数：
     * - month: 月份(1-12)
     * - day: 日期(1-31)
     *
     * 示例请求：
     * {
     *   "month": 6,
     *   "day": 15
     * }
     *
     * 测试数据说明：
     * 包含所有日期边界情况的测试数据，如2月29日、12月31日等
     *
     * @param request 日期请求参数
     * @return 对应的星座信息
     */
    @PostMapping("/by-date")
    public ResponseEntity<ZodiacByDateResponse> getZodiacByDate(@RequestBody ZodiacByDateRequest request) {
        try {
            log.info("根据日期查询星座: {}/{}", request.getMonth(), request.getDay());
            ZodiacByDateResponse response = zodiacService.getZodiacByDate(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("根据日期查询星座失败", e);
            throw e;
        }
    }
    /**
     * 获取所有星座信息
     * 返回12个星座的基本信息列表
     *
     * 示例请求：
     * {}
     *
     * 测试数据说明：
     * 固定返回所有12星座的完整信息集合
     *
     * @return 所有星座信息列表
     */
    @PostMapping("/all")
    public ResponseEntity<AllZodiacsResponse> getAllZodiacs() {
        try {
            log.info("查询所有星座信息");
            AllZodiacsResponse response = zodiacService.getAllZodiacs();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("查询所有星座信息失败", e);
            throw e;
        }
    }
}
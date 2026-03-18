package com.example.demo.controller;

import com.example.demo.common.Result;
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

@RestController
@RequestMapping("/api/zodiac")
@RequiredArgsConstructor
@Slf4j
@Validated
public class StarController {

    private final ZodiacService zodiacService;

    @PostMapping("/info")
    public ResponseEntity<Result<ZodiacInfoResponse>> getZodiacInfo(@RequestBody ZodiacInfoRequest request) {
        log.info("查询星座信息: {}", request.getZodiac());
        ZodiacInfoResponse response = zodiacService.getZodiacInfo(request);
        return ResponseEntity.ok(Result.success(response));
    }

    @PostMapping("/daily-horoscope")
    public ResponseEntity<Result<DailyHoroscopeResponse>> getDailyHoroscope(@RequestBody DailyHoroscopeRequest request) {
        log.info("收到请求: zodiac={}, date={}, category={}",
                request.getZodiac(), request.getDate(), request.getCategory());
        DailyHoroscopeResponse response = zodiacService.getDailyHoroscope(request);
        return ResponseEntity.ok(Result.success(response));
    }

    @PostMapping("/compatibility")
    public ResponseEntity<Result<CompatibilityResponse>> getCompatibility(@RequestBody CompatibilityRequest request) {
        log.info("星座配对分析: {} vs {}", request.getZodiac1(), request.getZodiac2());
        CompatibilityResponse response = zodiacService.getCompatibility(request);
        return ResponseEntity.ok(Result.success(response));
    }

    @PostMapping("/by-date")
    public ResponseEntity<Result<ZodiacByDateResponse>> getZodiacByDate(@RequestBody ZodiacByDateRequest request) {
        log.info("根据日期查询星座: {}/{}", request.getMonth(), request.getDay());
        ZodiacByDateResponse response = zodiacService.getZodiacByDate(request);
        return ResponseEntity.ok(Result.success(response));
    }

    @PostMapping("/all")
    public ResponseEntity<Result<AllZodiacsResponse>> getAllZodiacs() {
        log.info("查询所有星座信息");
        AllZodiacsResponse response = zodiacService.getAllZodiacs();
        return ResponseEntity.ok(Result.success(response));
    }
}

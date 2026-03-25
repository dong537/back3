package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.dto.request.yijing.YijingGenerateHexagramRequest;
import com.example.demo.dto.request.yijing.YijingInterpretRequest;
import com.example.demo.service.YijingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 易经MCP服务控制器
 */
@RestController
@Slf4j
@Validated
@RequestMapping("/api/yijing")
@RequiredArgsConstructor
public class YijingController {

    private final YijingService yijingService;

    @PostMapping("/hexagram/generate")
    public Result<Map<String, Object>> generateHexagram(@RequestBody @Validated YijingGenerateHexagramRequest request) {
        Map<String, Object> data = yijingService.generateHexagram(request);
        return Result.success(data);
    }

    @PostMapping("/hexagram/interpret")
    public Result<Map<String, Object>> interpretHexagram(@RequestBody @Validated YijingInterpretRequest request) {
        Map<String, Object> data = yijingService.interpretHexagram(request);
        return Result.success(data);
    }

    @GetMapping("/hexagrams")
    public Result<Map<String, Object>> listAllHexagrams() {
        Map<String, Object> data = yijingService.listAllHexagrams();
        return Result.success(data);
    }

    @GetMapping("/hexagram/{id}")
    public Result<Map<String, Object>> getHexagramById(@PathVariable Integer id) {
        Map<String, Object> data = yijingService.getHexagramInfo(id);
        return Result.success(data);
    }
}

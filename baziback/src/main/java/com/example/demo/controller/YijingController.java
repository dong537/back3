package com.example.demo.controller;

import com.example.demo.dto.request.yijing.*;
import com.example.demo.service.YijingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Map;
import java.util.function.Supplier;

/**
 * 易经八字MCP服务控制器
 * 提供卦象生成、八字分析、命理咨询等RESTful API接口
 */
@RestController
@Slf4j
@Validated
@RequestMapping("/api/yijing")
@RequiredArgsConstructor
public class YijingController {

    private final YijingService yijingService;
    /**
     * 生成六爻卦象
     * 根据指定方法生成易经卦象
     *
     * 请求参数：
     * - method: 起卦方式 (number/time/plum_blossom/random/coin)
     * - question: 求卦意图/问题描述
     * - seed: 起卦种子信息（可选）
     *
     * 示例请求：
     * {
     *   "method": "time",
     *   "question": "今天的运势如何？"
     * }
     *
     * @param request 起卦请求参数
     * @return 生成的卦象数据
     */
    @PostMapping("/hexagram/generate")
    public Mono<ResponseEntity<Map<String, Object>>> generateHexagram(@RequestBody @Validated YijingGenerateHexagramRequest request) {
        return asyncResponse("生成卦象", () -> yijingService.generateHexagram(request));
    }

    /**
     * 解读卦象
     * 对已生成的卦象进行详细解读
     *
     * @param request 解读请求参数
     * @return 卦象解读结果
     */
    @PostMapping("/hexagram/interpret")
    public Mono<ResponseEntity<Map<String, Object>>> interpretHexagram(@RequestBody @Validated YijingInterpretRequest request) {
        return asyncResponse("解读卦象", () -> yijingService.interpretHexagram(request));
    }

    /**
     * 获取所有卦象列表
     *
     * @return 64卦列表
     */
    @GetMapping("/hexagrams")
    public Mono<ResponseEntity<Map<String, Object>>> listAllHexagrams() {
        return asyncResponse("获取卦象列表", yijingService::listAllHexagrams);
    }

    /**
     * 获取指定卦象详情
     *
     * @param id 卦象ID (1-64)
     * @return 卦象详细信息
     */
    @GetMapping("/hexagram/{id}")
    public Mono<ResponseEntity<Map<String, Object>>> getHexagramById(@PathVariable Integer id) {
        return asyncResponse("获取卦象详情", () -> yijingService.getHexagramInfo(id));
    }

    private <T> Mono<ResponseEntity<T>> asyncResponse(String action, Supplier<T> supplier) {
        return Mono.fromCallable(() -> ResponseEntity.ok(supplier.get()))
                .subscribeOn(Schedulers.boundedElastic())
                .doOnError(e -> log.error("{}失败", action, e));
    }
}
package com.example.demo.controller;

import com.example.demo.client.McpBaziClient;
import com.example.demo.dto.request.bazi.McpBaziRequest;
import com.example.demo.dto.response.FormattedBaziResponse;
import com.example.demo.dto.response.McpBaziResponse;
import com.example.demo.service.DeepSeekService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RestController
@Slf4j
@RequestMapping("/api/bazi")
@RequiredArgsConstructor
public class BaziController {

    private final McpBaziClient mcpBaziClient;
    private final DeepSeekService deepSeekService;

    /**
     * 查询可用工具列表
     * GET http://localhost:8080/api/bazi/tools
     */
    @GetMapping("/tools")
    public Mono<String> listTools() {
        return Mono.fromCallable(mcpBaziClient::listAvailableTools)
                .subscribeOn(Schedulers.boundedElastic());
    }
    /**
     * 获取八字详情（格式化的响应，前端友好）
     * POST http://localhost:8080/api/bazi/formatted
     */
    @PostMapping("/formatted")
    public Mono<FormattedBaziResponse> getBaziFormatted(@RequestBody McpBaziRequest request) {
        return Mono.fromCallable(() -> {
                    log.info("收到八字查询请求: gender={}, lunarDatetime={}, solarDatetime={}",
                            request.getGender(), request.getLunarDatetime(), request.getSolarDatetime());

                    McpBaziResponse resp = mcpBaziClient.getBaziDetail(request);

                    if (resp == null || resp.getBaziData() == null) {
                        log.error("MCP响应为空或baziData为空");
                        throw new RuntimeException("获取八字数据失败：响应为空");
                    }

                    FormattedBaziResponse formatted = FormattedBaziResponse.fromMcpResponse(resp);

                    if (formatted == null) {
                        log.error("格式化响应失败");
                        throw new RuntimeException("格式化八字数据失败");
                    }

                    log.info("成功返回格式化八字数据: gender={}, bazi={}",
                            formatted.getGender(), formatted.getBazi());

                    return formatted;
                })
                .subscribeOn(Schedulers.boundedElastic())
                .doOnError(e -> log.error("获取格式化八字数据失败", e));
    }
}
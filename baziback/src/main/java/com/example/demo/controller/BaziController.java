package com.example.demo.controller;

import com.example.demo.client.McpBaziClient;
import com.example.demo.dto.request.McpBaziRequest;
import com.example.demo.dto.response.FormattedBaziResponse;
import com.example.demo.dto.response.McpBaziResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/api/bazi")
@RequiredArgsConstructor
public class BaziController {

    private final McpBaziClient mcpBaziClient;

    /**
     * 查询可用工具列表
     * GET http://localhost:8080/api/bazi/tools
     */
    @GetMapping("/tools")
    public String listTools() {
        return mcpBaziClient.listAvailableTools();
    }

    /**
     * 获取八字详情（原始格式）
     * POST http://localhost:8080/api/bazi/detail
     */
    @PostMapping("/detail")
    public McpBaziResponse getBaziDetail(@RequestBody McpBaziRequest request) {
        return mcpBaziClient.getBaziDetail(request);
    }

    /**
     * 获取八字详情（仅返回结构化数据，不含原始响应）
     * POST http://localhost:8080/api/bazi/structured
     */
    @PostMapping("/structured")
    public Map<String, Object> getBaziStructured(@RequestBody McpBaziRequest request) {
        McpBaziResponse response = mcpBaziClient.getBaziDetail(request);
        return response.getBaziData();
    }

    /**
     * 获取八字详情（格式化的响应，前端友好）
     * POST http://localhost:8080/api/bazi/formatted
     */
    @PostMapping("/formatted")
    public FormattedBaziResponse getBaziFormatted(@RequestBody McpBaziRequest request) {
        McpBaziResponse resp = mcpBaziClient.getBaziDetail(request);
        log.warn(">>> 返回给前端的对象：{}", resp);
        return FormattedBaziResponse.fromMcpResponse(resp);
    }

}
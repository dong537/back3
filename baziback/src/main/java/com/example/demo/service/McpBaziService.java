package com.example.demo.service;

import com.example.demo.client.McpBaziClient;
import com.example.demo.dto.request.bazi.McpBaziRequest;
import com.example.demo.dto.response.McpBaziResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class McpBaziService {

    private final McpBaziClient mcpBaziClient;

    public McpBaziResponse getBaziInfo(McpBaziRequest request) {
        if (request.getSolarDatetime() == null && request.getLunarDatetime() == null) {
            throw new IllegalArgumentException("solarDatetime 与 lunarDatetime 必须二选一");
        }
        return mcpBaziClient.getBaziDetail(request);
    }
    /**
     * 获取八字工具信息
     */
    
    }
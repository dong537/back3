package com.example.demo.service;

import com.example.demo.client.McpBaziClient;
import com.example.demo.dto.request.McpBaziRequest;
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

        // 移除对 eightCharProviderSect 的强制设置，测试是否为参数问题
        // System.out.println("接收到原始 eightCharProviderSect 值: " + request.getEightCharProviderSect() + "，强制修正为 1");
        // request.setEightCharProviderSect(1);
        // 调用修正后的 McpBaziClient
        return mcpBaziClient.getBaziDetail(request);
    }
    /**
     * 获取八字工具信息
     */
    }
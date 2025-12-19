package com.example.demo.service;

import com.example.demo.dto.request.yijing.*;
import com.example.demo.yijing.service.StandaloneYijingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class YijingService {

    private final StandaloneYijingService standaloneYijingService;

    public Map<String, Object> generateHexagram(YijingGenerateHexagramRequest request) {
        return standaloneYijingService.generateHexagram(request);
    }

    public Map<String, Object> interpretHexagram(YijingInterpretRequest request) {
        return standaloneYijingService.interpretHexagram(request);
    }

    public Map<String, Object> listAllHexagrams() {
        return Map.of(
                "success", true,
                "message", "获取成功",
                "data", Map.of(
                        "hexagrams", standaloneYijingService.listAllHexagrams(),
                        "total", 64
                )
        );
    }

    public Map<String, Object> getHexagramInfo(Integer id) {
        Map<String, Object> hexagram = standaloneYijingService.getHexagramInfo(id);
        if (hexagram != null) {
            return Map.of(
                    "success", true,
                    "message", "获取成功",
                    "data", hexagram
            );
        } else {
            return Map.of(
                    "success", false,
                    "message", "未找到该卦象"
            );
        }
    }
}
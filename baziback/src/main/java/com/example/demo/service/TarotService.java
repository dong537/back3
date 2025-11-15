package com.example.demo.service;

import com.example.demo.client.McpTarotClient;
import com.example.demo.dto.request.tarot.*;
import com.example.demo.dto.response.tarot.*;
import com.example.demo.exception.McpApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class TarotService {

    private final McpTarotClient mcpTarotClient;

    // 1. 获取单张牌信息
    public GetCardInfoResponse getCardInfo(GetCardInfoRequest request) {
        validateCardName(request.getCardName());
        return mcpTarotClient.getCardInfo(request);
    }

    // 2. 列出所有塔罗牌
    public ListAllCardsResponse listAllCards(ListAllCardsRequest request) {
        return mcpTarotClient.listAllCards(request);
    }

    // 3. 执行塔罗解读
    public PerformReadingResponse performReading(PerformReadingRequest request) {
        if (!request.getSpreadType().matches("single_card|three_card|celtic_cross|horseshoe|relationship_cross|career_path|decision_making|spiritual_guidance|year_ahead|chakra_alignment|shadow_work")) {
            throw new McpApiException("无效的牌阵类型: " + request.getSpreadType());
        }
        return mcpTarotClient.performReading(request);
    }

    // 4. 搜索塔罗牌
    public SearchCardsResponse searchCards(SearchCardsRequest request) {
        return mcpTarotClient.searchCards(request);
    }

    // 5. 查找相似牌
    public FindSimilarCardsResponse findSimilarCards(FindSimilarCardsRequest request) {
        validateCardName(request.getCardName());
        return mcpTarotClient.findSimilarCards(request);
    }

    // 6. 获取数据库分析
    public GetDatabaseAnalyticsResponse getDatabaseAnalytics(GetDatabaseAnalyticsRequest request) {
        return mcpTarotClient.getDatabaseAnalytics(request);
    }

    // 7. 创建自定义牌阵
    public CreateCustomSpreadResponse createCustomSpread(CreateCustomSpreadRequest request) {
        if (request.getPositions().size() < 1 || request.getPositions().size() > 15) {
            throw new McpApiException("自定义牌阵位置数量必须在1-15之间");
        }
        return mcpTarotClient.createCustomSpread(request);
    }

    // 8. 获取随机牌
    public GetRandomCardsResponse getRandomCards(GetRandomCardsRequest request) {
        if (request.getCount() < 1 || request.getCount() > 20) {
            throw new McpApiException("随机牌数量必须在1-20之间");
        }
        return mcpTarotClient.getRandomCards(request);
    }

    // 公共校验方法
    private void validateCardName(String cardName) {
        if (cardName == null || cardName.trim().isEmpty()) {
            throw new McpApiException("牌名不能为空");
        }
    }
}
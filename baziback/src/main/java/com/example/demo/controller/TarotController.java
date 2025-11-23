package com.example.demo.controller;

import com.example.demo.annotation.RequireAuth;
import com.example.demo.dto.request.tarot.*;
import com.example.demo.dto.response.tarot.*;
import com.example.demo.service.TarotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tarot")
@Slf4j
@RequiredArgsConstructor
@RequireAuth  // 需要登录
public class TarotController {

    private final TarotService tarotService;

    // 1. 获取单张牌信息
    @PostMapping("/card/info")
    public ResponseEntity<GetCardInfoResponse> getCardInfo(@RequestBody GetCardInfoRequest request) {
        return ResponseEntity.ok(tarotService.getCardInfo(request));
    }

    // 2. 列出所有塔罗牌
    @PostMapping("/card/list")
    public ResponseEntity<ListAllCardsResponse> listAllCards(@RequestBody ListAllCardsRequest request) {
        return ResponseEntity.ok(tarotService.listAllCards(request));
    }

    // 3. 执行塔罗解读
    @PostMapping("/reading/perform")
    public ResponseEntity<PerformReadingResponse> performReading(@RequestBody PerformReadingRequest request) {
        return ResponseEntity.ok(tarotService.performReading(request));
    }

    // 4. 搜索塔罗牌
    @PostMapping("/card/search")
    public ResponseEntity<SearchCardsResponse> searchCards(@RequestBody SearchCardsRequest request) {
        return ResponseEntity.ok(tarotService.searchCards(request));
    }

    // 5. 查找相似牌
    @PostMapping("/card/similar")
    public ResponseEntity<FindSimilarCardsResponse> findSimilarCards(@RequestBody FindSimilarCardsRequest request) {
        return ResponseEntity.ok(tarotService.findSimilarCards(request));
    }

    // 6. 获取数据库分析
    @PostMapping("/analytics")
    public ResponseEntity<GetDatabaseAnalyticsResponse> getDatabaseAnalytics(@RequestBody GetDatabaseAnalyticsRequest request) {
        return ResponseEntity.ok(tarotService.getDatabaseAnalytics(request));
    }

    // 7. 创建自定义牌阵
    @PostMapping("/spread/custom")
    public ResponseEntity<CreateCustomSpreadResponse> createCustomSpread(@RequestBody CreateCustomSpreadRequest request) {
        return ResponseEntity.ok(tarotService.createCustomSpread(request));
    }

    // 8. 获取随机牌
    @PostMapping("/card/random")
    public ResponseEntity<GetRandomCardsResponse> getRandomCards(@RequestBody GetRandomCardsRequest request) {
        return ResponseEntity.ok(tarotService.getRandomCards(request));
    }
}
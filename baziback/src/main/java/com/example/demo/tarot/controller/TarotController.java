package com.example.demo.tarot.controller;

import com.example.demo.common.Result;
import com.example.demo.entity.TbTarotCard;
import com.example.demo.mapper.TarotCardMapper;
import com.example.demo.tarot.service.TarotService;
import com.example.demo.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/tarot")
@RequiredArgsConstructor
@Slf4j
public class TarotController {

    private final TarotService tarotService;
    private final TarotCardMapper tarotCardMapper;
    private final JwtUtil jwtUtil;

    /**
     * 获取牌阵列表
     */
    @GetMapping("/spreads")
    public Result<Map<String, Object>> getSpreads() {
        try {
            Map<String, Object> spreads = tarotService.getSpreads();
            return Result.success(spreads);
        } catch (Exception e) {
            log.error("Error getting spreads", e);
            return Result.error("获取牌阵列表失败: " + e.getMessage());
        }
    }

    /**
     * 抽取塔罗牌
     */
    @PostMapping("/draw")
    public Result<TarotService.DrawResult> drawCards(
            @RequestBody Map<String, String> request) {
        try {
            String spreadType = request.getOrDefault("spreadType", "SINGLE");
            String question = request.getOrDefault("question", "");
            
            log.info("Drawing tarot cards - spreadType: {}, question: {}", spreadType, question);
            
            TarotService.DrawResult result = tarotService.drawCards(spreadType, question);
            return Result.success(result);
        } catch (Exception e) {
            log.error("Error drawing tarot cards", e);
            return Result.error("抽取塔罗牌失败: " + e.getMessage());
        }
    }

    /**
     * 快速单牌占卜
     */
    @PostMapping("/quick-draw")
    public Result<TarotService.DrawResult> quickDraw(
            @RequestBody Map<String, String> request) {
        try {
            String question = request.getOrDefault("question", "");
            TarotService.DrawResult result = tarotService.drawCards("SINGLE", question);
            return Result.success(result);
        } catch (Exception e) {
            log.error("Error quick drawing tarot card", e);
            return Result.error("快速占卜失败: " + e.getMessage());
        }
    }

    /**
     * 获取塔罗牌详细信息
     */
    @GetMapping("/card/{cardId}")
    public Result<Map<String, Object>> getCardDetail(@PathVariable Integer cardId) {
        try {
            TbTarotCard card = tarotCardMapper.findByCardId(cardId);
            if (card == null) {
                return Result.error("未找到该塔罗牌");
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("id", card.getId());
            result.put("cardId", card.getCardId());
            result.put("cardNameCn", card.getCardNameCn());
            result.put("cardNameEn", card.getCardNameEn());
            result.put("cardType", card.getCardType());
            result.put("suit", card.getSuit());
            result.put("number", card.getNumber());
            result.put("symbol", card.getSymbol());
            result.put("keywordUp", card.getKeywordUp());
            result.put("keywordRev", card.getKeywordRev());
            result.put("meaningUp", card.getMeaningUp());
            result.put("meaningRev", card.getMeaningRev());
            result.put("description", card.getDescription());
            result.put("interpretationUp", card.getInterpretationUp());
            result.put("interpretationRev", card.getInterpretationRev());
            result.put("loveUp", card.getLoveUp());
            result.put("loveRev", card.getLoveRev());
            result.put("careerUp", card.getCareerUp());
            result.put("careerRev", card.getCareerRev());
            result.put("wealthUp", card.getWealthUp());
            result.put("wealthRev", card.getWealthRev());
            result.put("healthUp", card.getHealthUp());
            result.put("healthRev", card.getHealthRev());
            result.put("adviceUp", card.getAdviceUp());
            result.put("adviceRev", card.getAdviceRev());
            result.put("imageUrl", card.getImageUrl());
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("Error getting card detail", e);
            return Result.error("获取塔罗牌详情失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有塔罗牌列表
     */
    @GetMapping("/cards")
    public Result<java.util.List<Map<String, Object>>> getAllCards() {
        try {
            java.util.List<TbTarotCard> cards = tarotCardMapper.findAll();
            java.util.List<Map<String, Object>> result = new java.util.ArrayList<>();
            for (TbTarotCard card : cards) {
                Map<String, Object> cardMap = new HashMap<>();
                cardMap.put("id", card.getId());
                cardMap.put("cardId", card.getCardId());
                cardMap.put("cardNameCn", card.getCardNameCn());
                cardMap.put("cardNameEn", card.getCardNameEn());
                cardMap.put("cardType", card.getCardType());
                cardMap.put("suit", card.getSuit());
                cardMap.put("number", card.getNumber());
                cardMap.put("symbol", card.getSymbol());
                cardMap.put("imageUrl", card.getImageUrl());
                result.add(cardMap);
            }
            return Result.success(result);
        } catch (Exception e) {
            log.error("Error getting all cards", e);
            return Result.error("获取塔罗牌列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取今日运势
     */
    @GetMapping("/daily-fortune/{cardId}")
    public Result<Map<String, Object>> getDailyFortune(@PathVariable Integer cardId) {
        try {
            TbTarotCard card = tarotCardMapper.findByCardId(cardId);
            if (card == null) {
                return Result.error("未找到该塔罗牌");
            }
            
            // 根据日期生成随机但稳定的正逆位（同一天同一张牌结果相同）
            java.time.LocalDate today = java.time.LocalDate.now();
            long seed = today.toEpochDay() + cardId;
            boolean isReversed = (seed % 2) == 0;
            
            Map<String, Object> result = new HashMap<>();
            result.put("date", today.toString());
            result.put("cardId", card.getCardId());
            result.put("cardName", card.getCardNameCn());
            result.put("isReversed", isReversed);
            result.put("interpretation", isReversed ? card.getInterpretationRev() : card.getInterpretationUp());
            result.put("meaning", isReversed ? card.getMeaningRev() : card.getMeaningUp());
            result.put("advice", isReversed ? card.getAdviceRev() : card.getAdviceUp());
            result.put("keyword", isReversed ? card.getKeywordRev() : card.getKeywordUp());
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("Error getting daily fortune", e);
            return Result.error("获取今日运势失败: " + e.getMessage());
        }
    }

    /**
     * 获取今日抽牌记录（如果已抽过）
     * GET /api/tarot/daily-draw
     */
    @GetMapping("/daily-draw")
    public Result<TarotService.DailyDrawResult> getTodayDraw(
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            Long userId = getUserIdFromToken(token);
            if (userId == null) {
                return Result.error("未登录或token无效");
            }
            
            TarotService.DailyDrawResult result = tarotService.getTodayDraw(userId);
            if (result == null) {
                return Result.error("今天还没有抽牌");
            }
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("Error getting today draw", e);
            return Result.error("获取今日抽牌记录失败: " + e.getMessage());
        }
    }
    
    /**
     * 执行每日抽牌（如果今天已抽过则返回错误）
     * POST /api/tarot/daily-draw
     */
    @PostMapping("/daily-draw")
    public Result<TarotService.DailyDrawResult> drawDailyCard(
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            Long userId = getUserIdFromToken(token);
            if (userId == null) {
                return Result.error("未登录或token无效");
            }
            
            TarotService.DailyDrawResult result = tarotService.drawDailyCard(userId);
            return Result.success(result);
        } catch (RuntimeException e) {
            // 业务异常（如今天已抽过）
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("Error drawing daily card", e);
            return Result.error("每日抽牌失败: " + e.getMessage());
        }
    }
    
    /**
     * 从token中获取用户ID
     */
    private Long getUserIdFromToken(String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }
        try {
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            if (jwtUtil.validateToken(token)) {
                return jwtUtil.extractUserId(token);
            }
        } catch (Exception e) {
            log.warn("Token解析失败: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 根据牌名获取塔罗牌详细信息
     */
    @GetMapping("/card/name/{name}")
    public Result<Map<String, Object>> getCardDetailByName(@PathVariable String name) {
        try {
            // URL解码
            String decodedName;
            try {
                decodedName = java.net.URLDecoder.decode(name, "UTF-8");
            } catch (Exception e) {
                decodedName = name; // 如果解码失败，使用原始名称
                log.warn("URL解码失败，使用原始名称: {}", name);
            }
            log.info("查找塔罗牌: 原始名称={}, 解码后={}", name, decodedName);
            
            // 先尝试精确匹配
            TbTarotCard card = tarotCardMapper.findByName(decodedName);
            
            if (card == null) {
                log.warn("未找到塔罗牌: {}", decodedName);
                return Result.error("未找到该塔罗牌: " + decodedName);
            }
            
            log.info("找到塔罗牌: cardId={}, cardNameCn={}", card.getCardId(), card.getCardNameCn());
            
            Map<String, Object> result = new HashMap<>();
            result.put("id", card.getId());
            result.put("cardId", card.getCardId());
            result.put("cardNameCn", card.getCardNameCn());
            result.put("cardNameEn", card.getCardNameEn());
            result.put("cardType", card.getCardType());
            result.put("suit", card.getSuit());
            result.put("number", card.getNumber());
            result.put("symbol", card.getSymbol());
            result.put("keywordUp", card.getKeywordUp());
            result.put("keywordRev", card.getKeywordRev());
            result.put("meaningUp", card.getMeaningUp());
            result.put("meaningRev", card.getMeaningRev());
            result.put("description", card.getDescription());
            result.put("interpretationUp", card.getInterpretationUp());
            result.put("interpretationRev", card.getInterpretationRev());
            result.put("loveUp", card.getLoveUp());
            result.put("loveRev", card.getLoveRev());
            result.put("careerUp", card.getCareerUp());
            result.put("careerRev", card.getCareerRev());
            result.put("wealthUp", card.getWealthUp());
            result.put("wealthRev", card.getWealthRev());
            result.put("healthUp", card.getHealthUp());
            result.put("healthRev", card.getHealthRev());
            result.put("adviceUp", card.getAdviceUp());
            result.put("adviceRev", card.getAdviceRev());
            result.put("imageUrl", card.getImageUrl());
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("Error getting card detail by name", e);
            return Result.error("获取塔罗牌详情失败: " + e.getMessage());
        }
    }
}

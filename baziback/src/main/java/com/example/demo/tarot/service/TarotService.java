package com.example.demo.tarot.service;

import com.example.demo.entity.TbTarotCard;
import com.example.demo.entity.TbTarotDailyDraw;
import com.example.demo.mapper.TarotCardMapper;
import com.example.demo.mapper.TarotDailyDrawMapper;
import com.example.demo.tarot.model.SpreadPosition;
import com.example.demo.tarot.model.SpreadType;
import com.example.demo.tarot.model.TarotCard;
import com.example.demo.tarot.repository.TarotDeckRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class TarotService {

    private final TarotDeckRepository tarotDeckRepository;
    private final SpreadCatalog spreadCatalog;
    private final TarotCardMapper tarotCardMapper;
    private final TarotDailyDrawMapper tarotDailyDrawMapper;

    /**
     * 抽取塔罗牌
     */
    public DrawResult drawCards(String spreadTypeStr, String question) {
        try {
            // 解析牌阵类型
            SpreadType spreadType;
            try {
                spreadType = SpreadType.valueOf(spreadTypeStr);
            } catch (IllegalArgumentException e) {
                log.warn("Invalid spread type: {}, using SINGLE", spreadTypeStr);
                spreadType = SpreadType.SINGLE;
            }

            // 获取牌阵位置信息
            List<SpreadPosition> positions = spreadCatalog.positions(spreadType);
            if (positions.isEmpty()) {
                positions = spreadCatalog.positions(SpreadType.SINGLE);
            }

            // 获取所有牌
            List<TarotCard> allCards = tarotDeckRepository.findAll();
            if (allCards == null || allCards.isEmpty()) {
                log.error("塔罗牌数据库为空，无法抽牌");
                throw new RuntimeException("塔罗牌数据库为空，请联系管理员");
            }
            List<TarotCard> deck = new ArrayList<>(allCards);
            
            // 洗牌
            Collections.shuffle(deck, new Random());

            // 抽取需要的牌数
            int cardCount = positions.size();
            List<TarotCard> drawnCards = deck.stream()
                    .limit(cardCount)
                    .collect(Collectors.toList());

            // 构建结果
            List<DrawResult.Card> resultCards = new ArrayList<>();
            for (int i = 0; i < drawnCards.size(); i++) {
                TarotCard card = drawnCards.get(i);
                SpreadPosition position = i < positions.size() ? positions.get(i) : null;
                
                // 随机决定正逆位（30%概率逆位）
                boolean reversed = new Random().nextInt(100) < 30;

                DrawResult.Card resultCard = new DrawResult.Card();
                resultCard.setIndex(card.getCardId());
                resultCard.setName(card.getCardNameCn());
                resultCard.setReversed(reversed);
                resultCard.setSymbol(getCardSymbol(card));
                if (position != null) {
                    resultCard.setPosition(position.getKey());
                    resultCard.setPositionMeaning(position.getBusinessMeaning());
                }
                resultCards.add(resultCard);
            }

            DrawResult result = new DrawResult();
            result.setSpreadType(spreadTypeStr);
            result.setQuestion(question);
            result.setCards(resultCards);

            return result;
        } catch (Exception e) {
            log.error("Error drawing tarot cards", e);
            throw new RuntimeException("抽取塔罗牌失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取牌阵列表
     */
    public Map<String, Object> getSpreads() {
        Map<String, Object> result = new HashMap<>();
        Map<SpreadType, List<SpreadPosition>> allSpreads = spreadCatalog.all();
        
        Map<String, Object> spreads = new HashMap<>();
        for (Map.Entry<SpreadType, List<SpreadPosition>> entry : allSpreads.entrySet()) {
            Map<String, Object> spreadInfo = new HashMap<>();
            spreadInfo.put("type", entry.getKey().name());
            spreadInfo.put("cardCount", entry.getValue().size());
            spreadInfo.put("positions", entry.getValue().stream()
                    .map(p -> {
                        Map<String, String> pos = new HashMap<>();
                        pos.put("code", p.getKey());
                        pos.put("label", p.getName());
                        pos.put("meaning", p.getBusinessMeaning());
                        return pos;
                    })
                    .collect(Collectors.toList()));
            spreads.put(entry.getKey().name(), spreadInfo);
        }
        
        result.put("spreads", spreads);
        return result;
    }

    /**
     * 每日抽牌 - 获取今日抽牌记录（如果已抽过）
     */
    public DailyDrawResult getTodayDraw(Long userId) {
        try {
            LocalDate today = LocalDate.now();
            TbTarotDailyDraw dailyDraw = tarotDailyDrawMapper.findByUserIdAndDate(userId, today);
            
            if (dailyDraw == null) {
                return null; // 今天还没抽牌
            }
            
            // 获取牌的信息
            TbTarotCard card = tarotCardMapper.findByCardId(dailyDraw.getCardId());
            if (card == null) {
                log.error("抽牌记录中的牌ID不存在: {}", dailyDraw.getCardId());
                return null;
            }
            
            return buildDailyDrawResult(dailyDraw, card);
        } catch (Exception e) {
            log.error("Error getting today draw for user: {}", userId, e);
            throw new RuntimeException("获取今日抽牌记录失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 每日抽牌 - 执行抽牌（如果今天已抽过则抛出异常）
     */
    @Transactional
    public DailyDrawResult drawDailyCard(Long userId) {
        if (userId == null) {
            log.error("用户ID为空，无法抽牌");
            throw new RuntimeException("用户ID无效");
        }
        
        try {
            LocalDate today = LocalDate.now();
            log.info("用户 {} 开始每日抽牌，日期: {}", userId, today);
            
            // 检查今天是否已抽过
            int count = tarotDailyDrawMapper.countByUserIdAndDate(userId, today);
            if (count > 0) {
                log.warn("用户 {} 今天已经抽过牌了", userId);
                throw new RuntimeException("今天已经抽过牌了，请明天再来");
            }
            
            // 获取所有牌
            List<TarotCard> allCards = tarotDeckRepository.findAll();
            if (allCards == null || allCards.isEmpty()) {
                log.error("塔罗牌数据库为空，无法抽牌");
                throw new RuntimeException("塔罗牌数据库为空，请联系管理员");
            }
            
            log.debug("从 {} 张牌中随机抽取", allCards.size());
            
            // 随机抽取一张牌
            List<TarotCard> deck = new ArrayList<>(allCards);
            Collections.shuffle(deck, new Random());
            TarotCard drawnCard = deck.get(0);
            
            if (drawnCard == null || drawnCard.getCardId() == null) {
                log.error("抽取的牌无效: {}", drawnCard);
                throw new RuntimeException("抽牌失败：牌数据无效");
            }
            
            log.debug("抽到牌: cardId={}", drawnCard.getCardId());
            
            // 随机决定正逆位（30%概率逆位）
            boolean isReversed = new Random().nextInt(100) < 30;
            
            // 保存抽牌记录
            TbTarotDailyDraw dailyDraw = TbTarotDailyDraw.builder()
                    .userId(userId)
                    .drawDate(today)
                    .cardId(drawnCard.getCardId())
                    .isReversed(isReversed)
                    .build();
            
            int insertResult = tarotDailyDrawMapper.insert(dailyDraw);
            if (insertResult <= 0) {
                log.error("插入抽牌记录失败: userId={}, cardId={}", userId, drawnCard.getCardId());
                throw new RuntimeException("保存抽牌记录失败");
            }
            
            log.debug("抽牌记录已保存: id={}", dailyDraw.getId());
            
            // 获取牌的详细信息
            TbTarotCard card = tarotCardMapper.findByCardId(drawnCard.getCardId());
            if (card == null) {
                log.error("抽到的牌ID不存在: {}", drawnCard.getCardId());
                throw new RuntimeException("获取牌信息失败：牌ID " + drawnCard.getCardId() + " 不存在");
            }
            
            log.info("用户 {} 抽牌成功: cardId={}, cardName={}, isReversed={}", 
                    userId, card.getCardId(), card.getCardNameCn(), isReversed);
            
            return buildDailyDrawResult(dailyDraw, card);
        } catch (RuntimeException e) {
            // 业务异常直接抛出
            log.warn("每日抽牌业务异常: userId={}, error={}", userId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("每日抽牌系统异常: userId={}", userId, e);
            throw new RuntimeException("每日抽牌失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 构建每日抽牌结果
     * 注意：逆位时只翻转图片，内容数据使用正位数据
     */
    private DailyDrawResult buildDailyDrawResult(TbTarotDailyDraw dailyDraw, TbTarotCard card) {
        if (dailyDraw == null || card == null) {
            log.error("构建每日抽牌结果时参数为空: dailyDraw={}, card={}", dailyDraw, card);
            throw new RuntimeException("构建抽牌结果失败：数据不完整");
        }
        
        boolean isReversed = dailyDraw.getIsReversed() != null && dailyDraw.getIsReversed();
        
        DailyDrawResult result = new DailyDrawResult();
        result.setDate(dailyDraw.getDrawDate() != null ? dailyDraw.getDrawDate().toString() : LocalDate.now().toString());
        result.setCardId(card.getCardId() != null ? card.getCardId() : 0);
        result.setCardNameCn(card.getCardNameCn() != null ? card.getCardNameCn() : "未知");
        result.setCardNameEn(card.getCardNameEn() != null ? card.getCardNameEn() : "Unknown");
        result.setIsReversed(isReversed); // 保留逆位标志，用于前端翻转图片
        result.setImageUrl(card.getImageUrl());
        result.setSymbol(card.getSymbol() != null ? card.getSymbol() : "🎴");
        
        // 无论正逆位，都使用正位的内容数据（逆位时只翻转图片）
        // 添加空值检查，避免空指针异常
        result.setInterpretation(card.getInterpretationUp() != null ? card.getInterpretationUp() : "");
        result.setMeaning(card.getMeaningUp() != null ? card.getMeaningUp() : "");
        result.setLove(card.getLoveUp() != null ? card.getLoveUp() : "");
        result.setCareer(card.getCareerUp() != null ? card.getCareerUp() : "");
        result.setWealth(card.getWealthUp() != null ? card.getWealthUp() : "");
        result.setHealth(card.getHealthUp() != null ? card.getHealthUp() : "");
        result.setAdvice(card.getAdviceUp() != null ? card.getAdviceUp() : "");
        result.setKeyword(card.getKeywordUp() != null ? card.getKeywordUp() : "");
        
        log.debug("构建每日抽牌结果成功: cardId={}, cardName={}, isReversed={}", 
                result.getCardId(), result.getCardNameCn(), result.getIsReversed());
        
        return result;
    }
    
    /**
     * 获取牌的符号（用于显示）
     */
    private String getCardSymbol(TarotCard card) {
        // 可以根据牌的类型返回不同的符号
        if (card.getCardType() == com.example.demo.tarot.model.TarotCardType.MAJOR_ARCANA) {
            return "🎴";
        } else {
            return "🃏";
        }
    }

    /**
     * 抽牌结果
     */
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class DrawResult {
        private String spreadType;
        private String question;
        private List<Card> cards;

        @lombok.Data
        @lombok.NoArgsConstructor
        @lombok.AllArgsConstructor
        public static class Card {
            private Integer index;
            private String name;
            private Boolean reversed;
            private String symbol;
            private String position;
            private String positionMeaning;
        }
    }
    
    /**
     * 每日抽牌结果
     */
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class DailyDrawResult {
        private String date;
        private Integer cardId;
        private String cardNameCn;
        private String cardNameEn;
        private Boolean isReversed;
        private String imageUrl;
        private String symbol;
        private String interpretation;  // 综合运势解读
        private String meaning;         // 基本含义
        private String love;            // 感情/人际
        private String career;          // 事业
        private String wealth;          // 财运
        private String health;          // 健康
        private String advice;         // 建议
        private String keyword;        // 关键词
    }
}

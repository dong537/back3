package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.dto.response.CreditBalanceResponse;
import com.example.demo.entity.ExchangeProduct;
import com.example.demo.entity.ExchangeRecord;
import com.example.demo.exception.BusinessException;
import com.example.demo.service.AchievementService;
import com.example.demo.service.CreditExchangeService;
import com.example.demo.service.CreditService;
import com.example.demo.service.SseEmitterService;
import com.example.demo.util.AuthUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/credit")
@RequiredArgsConstructor
@Slf4j
public class CreditController {

    @Value("${app.credit.watch-ad-points:10}")
    private int watchAdPoints;

    @Value("${app.credit.watch-ad-daily-limit:20}")
    private int watchAdDailyLimit;

    private final CreditService creditService;
    private final CreditExchangeService creditExchangeService;
    private final AchievementService achievementService;
    private final SseEmitterService sseEmitterService;
    private final AuthUtil authUtil;

    @GetMapping("/balance")
    public Result<CreditBalanceResponse> getBalance(
            @RequestHeader(value = "Authorization", required = false) String token) {
        Long userId = authUtil.requireUserId(token);
        Integer balance = creditService.getCurrentPoints(userId);
        log.info("查询用户积分: userId={}, balance={}", userId, balance);
        return Result.success(new CreditBalanceResponse(balance));
    }

    @GetMapping("/products")
    public Result<List<ExchangeProduct>> getProducts() {
        List<ExchangeProduct> products = creditExchangeService.getAvailableProducts();
        log.info("查询可兑换商品列表: count={}", products.size());
        return Result.success(products);
    }

    @PostMapping("/exchange")
    public Result<Map<String, Object>> exchangeProduct(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestBody Map<String, String> request) {
        Long userId = authUtil.requireUserId(token);
        if (request == null) {
            throw new BusinessException("请求参数不能为空");
        }

        String productCode = request.get("productCode");
        if (productCode == null || productCode.isEmpty()) {
            throw new BusinessException("商品代码不能为空");
        }

        log.info("用户兑换商品: userId={}, productCode={}", userId, productCode);
        Map<String, Object> result = creditExchangeService.exchangeProduct(userId, productCode);
        if (!Boolean.TRUE.equals(result.get("success"))) {
            throw new BusinessException(String.valueOf(result.getOrDefault("message", "兑换失败")));
        }
        return Result.success(result);
    }

    @GetMapping("/exchange/records")
    public Result<List<ExchangeRecord>> getExchangeRecords(
            @RequestHeader(value = "Authorization", required = false) String token) {
        Long userId = authUtil.requireUserId(token);
        List<ExchangeRecord> records = creditExchangeService.getUserExchangeRecords(userId);
        log.info("查询用户兑换记录: userId={}, count={}", userId, records.size());
        return Result.success(records);
    }

    @PostMapping("/spend")
    public Result<Map<String, Object>> spendCredits(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestBody Map<String, Object> request) {
        Long userId = authUtil.requireUserId(token);
        if (request == null) {
            throw new BusinessException("请求参数不能为空");
        }

        Integer amount = parseAmount(request.get("amount"));
        if (amount <= 0) {
            throw new BusinessException("积分数量必须大于0");
        }

        String reason = String.valueOf(request.getOrDefault("reason", "功能消费"));
        boolean success = creditService.deductPoints(userId, amount, reason);
        if (!success) {
            Integer currentBalance = creditService.getCurrentPoints(userId);
            Map<String, Object> data = new HashMap<>();
            data.put("currentBalance", currentBalance);
            data.put("required", amount);
            throw new BusinessException(
                    "积分不足，当前余额：" + currentBalance + "，需要：" + amount,
                    HttpStatus.BAD_REQUEST,
                    data
            );
        }

        Integer newBalance = creditService.getCurrentPoints(userId);
        log.info("用户消费积分: userId={}, amount={}, reason={}, newBalance={}", userId, amount, reason, newBalance);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("amount", amount);
        result.put("newBalance", newBalance);
        result.put("reason", reason);
        return Result.success(result);
    }

    @PostMapping("/earn/watch-ad")
    public Result<Map<String, Object>> earnPointsByWatchingAd(
            @RequestHeader(value = "Authorization", required = false) String token) {
        Long userId = authUtil.requireUserId(token);

        int earnedCountToday = creditService.getTodayWatchAdEarnCount(userId);
        if (earnedCountToday >= watchAdDailyLimit) {
            Map<String, Object> data = new HashMap<>();
            data.put("dailyLimit", watchAdDailyLimit);
            data.put("earnedCountToday", earnedCountToday);
            throw new BusinessException(
                    "今日观看广告积分已达上限",
                    HttpStatus.TOO_MANY_REQUESTS,
                    data
            );
        }

        String reason = "观看广告";
        Integer newBalance = creditService.addPointsAndGetBalance(userId, watchAdPoints, reason, null);
        if (newBalance == null) {
            throw new BusinessException("积分添加失败");
        }

        try {
            achievementService.checkPointsAchievements(userId);
        } catch (Exception e) {
            log.error("检查广告积分成就失败: userId={}", userId, e);
        }

        int remaining = Math.max(0, watchAdDailyLimit - earnedCountToday - 1);
        log.info("用户观看广告获得积分: userId={}, points={}, newBalance={}, remaining={}",
                userId, watchAdPoints, newBalance, remaining);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("points", watchAdPoints);
        result.put("newBalance", newBalance);
        result.put("reason", reason);
        result.put("remainingToday", remaining);
        return Result.success(result);
    }

    @GetMapping(value = "/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<Map<String, Object>>> subscribeCreditSse(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestParam(value = "token", required = false) String tokenParam) {
        String authToken = token != null ? token : (tokenParam != null ? "Bearer " + tokenParam : null);
        Long userId = authUtil.requireUserId(authToken);
        log.info("用户 {} 订阅积分 SSE 流", userId);
        return sseEmitterService.subscribe(userId, "credit");
    }

    private Integer parseAmount(Object amountObj) {
        if (amountObj == null) {
            throw new BusinessException("积分数量不能为空");
        }
        if (amountObj instanceof Integer integer) {
            return integer;
        }
        if (amountObj instanceof Number number) {
            return number.intValue();
        }
        throw new BusinessException("积分数量格式错误");
    }
}

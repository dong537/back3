package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.dto.response.CreditBalanceResponse;
import com.example.demo.entity.ExchangeProduct;
import com.example.demo.entity.ExchangeRecord;
import com.example.demo.service.CreditExchangeService;
import com.example.demo.exception.BusinessException;
import com.example.demo.service.CreditService;
import com.example.demo.service.SseEmitterService;
import com.example.demo.util.AuthUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 积分控制器
 */
@RestController
@RequestMapping("/api/credit")
@RequiredArgsConstructor
@Slf4j
public class CreditController {

    private final CreditService creditService;
    private final CreditExchangeService creditExchangeService;
    private final SseEmitterService sseEmitterService;
    private final AuthUtil authUtil;

    /**
     * 获取当前积分
     */
    @GetMapping("/balance")
    public Result<CreditBalanceResponse> getBalance(
            @RequestHeader(value = "Authorization", required = false) String token) {
        Long userId = authUtil.requireUserId(token);
        Integer balance = creditService.getCurrentPoints(userId);
        log.info("查询用户积分: userId={}, balance={}", userId, balance);
        return Result.success(new CreditBalanceResponse(balance));
    }

    /**
     * 获取可兑换商品列表
     */
    @GetMapping("/products")
    public Result<List<ExchangeProduct>> getProducts() {
        List<ExchangeProduct> products = creditExchangeService.getAvailableProducts();
        log.info("查询可兑换商品列表: 商品数量={}", products.size());
        return Result.success(products);
    }

    /**
     * 兑换商品
     */
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
        log.info("兑换成功: userId={}, productCode={}", userId, productCode);
        return Result.success(result);
    }

    /**
     * 获取兑换记录
     */
    @GetMapping("/exchange/records")
    public Result<List<ExchangeRecord>> getExchangeRecords(
            @RequestHeader(value = "Authorization", required = false) String token) {
        Long userId = authUtil.requireUserId(token);
        List<ExchangeRecord> records = creditExchangeService.getUserExchangeRecords(userId);
        log.info("查询用户兑换记录: userId={}, 记录数量={}", userId, records.size());
        return Result.success(records);
    }

    /**
     * 消费积分（用于AI解读、进阶牌阵等功能）
     */
    @PostMapping("/spend")
    public Result<Map<String, Object>> spendCredits(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestBody Map<String, Object> request) {
        Long userId = authUtil.requireUserId(token);
        if (request == null) {
            throw new BusinessException("请求参数不能为空");
        }
        
        Object amountObj = request.get("amount");
        if (amountObj == null) {
            throw new BusinessException("积分数量不能为空");
        }
        Integer amount;
        if (amountObj instanceof Integer) {
            amount = (Integer) amountObj;
        } else if (amountObj instanceof Number) {
            amount = ((Number) amountObj).intValue();
        } else {
            throw new BusinessException("积分数量格式错误");
        }
        
        if (amount <= 0) {
            throw new BusinessException("积分数量必须大于0");
        }
        
        String reason = (String) request.getOrDefault("reason", "功能消费");
        
        // 先检查余额是否足够
        Integer currentBalance = creditService.getCurrentPoints(userId);
        if (currentBalance < amount) {
            throw new BusinessException("积分不足，当前余额：" + currentBalance + "，需要：" + amount);
        }
        
        // 扣除积分
        boolean success = creditService.deductPoints(userId, amount, reason);
        if (!success) {
            throw new BusinessException("积分扣除失败");
        }
        
        // 获取扣除后的余额
        Integer newBalance = creditService.getCurrentPoints(userId);
        
        log.info("用户消费积分: userId={}, amount={}, reason={}, newBalance={}", userId, amount, reason, newBalance);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("amount", amount);
        result.put("newBalance", newBalance);
        result.put("reason", reason);
        
        return Result.success(result);
    }

    /**
     * 观看广告获得积分
     */
    @PostMapping("/earn/watch-ad")
    public Result<Map<String, Object>> earnPointsByWatchingAd(
            @RequestHeader(value = "Authorization", required = false) String token) {
        Long userId = authUtil.requireUserId(token);
        Integer points = 10; // 观看广告获得10积分
        String reason = "观看广告";
        
        // 添加积分
        boolean success = creditService.addPoints(userId, points, reason, null);
        if (!success) {
            throw new BusinessException("积分添加失败");
        }
        
        // 获取添加后的余额
        Integer newBalance = creditService.getCurrentPoints(userId);
        
        log.info("用户观看广告获得积分: userId={}, points={}, newBalance={}", userId, points, newBalance);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("points", points);
        result.put("newBalance", newBalance);
        result.put("reason", reason);
        
        return Result.success(result);
    }

    /**
     * 积分相关的 SSE 订阅（WebFlux）
     * 前端可以使用 EventSource 连接：/api/credit/sse
     */
    @GetMapping(value = "/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<Map<String, Object>>> subscribeCreditSse(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestParam(value = "token", required = false) String tokenParam) {
        String authToken = token != null ? token : (tokenParam != null ? "Bearer " + tokenParam : null);
        Long userId = authUtil.requireUserId(authToken);
        log.info("用户 {} 订阅积分 SSE 流", userId);
        return sseEmitterService.subscribe(userId);
    }
}

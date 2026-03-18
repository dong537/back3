package com.example.demo.service;

import com.example.demo.entity.ExchangeProduct;
import com.example.demo.entity.ExchangeRecord;
import com.example.demo.mapper.CreditMapper;
import com.example.demo.mapper.UserFeatureUnlockMapper;
import com.example.demo.mapper.UserVipMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 积分兑换服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CreditExchangeService {
    
    private final CreditMapper creditMapper;
    private final CreditService creditService;
    private final UserVipMapper userVipMapper;
    private final UserFeatureUnlockMapper userFeatureUnlockMapper;
    
    /**
     * 获取所有可兑换商品
     */
    public List<ExchangeProduct> getAvailableProducts() {
        return creditMapper.findAllActiveProducts();
    }
    
    /**
     * 兑换商品
     */
    @Transactional
    public Map<String, Object> exchangeProduct(Long userId, String productCode) {
        Map<String, Object> result = new HashMap<>();
        
        // 参数验证
        if (userId == null) {
            result.put("success", false);
            result.put("message", "用户ID不能为空");
            return result;
        }
        if (productCode == null || productCode.isEmpty()) {
            result.put("success", false);
            result.put("message", "商品代码不能为空");
            return result;
        }
        
        // 1. 查询商品信息
        ExchangeProduct product = creditMapper.findByProductCode(productCode);
        if (product == null) {
            result.put("success", false);
            result.put("message", "商品不存在或已下架");
            return result;
        }
        
        // 2. 检查用户积分
        Integer currentPoints = creditService.getCurrentPoints(userId);
        if (currentPoints < product.getPointsCost()) {
            result.put("success", false);
            result.put("message", "积分不足");
            return result;
        }
        
        // 3. 检查兑换限制
        int todayCount = creditMapper.countTodayExchange(userId, productCode);
        if (product.getDailyLimit() != null && todayCount >= product.getDailyLimit()) {
            result.put("success", false);
            result.put("message", "今日兑换次数已达上限");
            return result;
        }
        
        int totalCount = creditMapper.countTotalExchange(userId, productCode);
        if (product.getUserLimit() != null && totalCount >= product.getUserLimit()) {
            result.put("success", false);
            result.put("message", "兑换次数已达上限");
            return result;
        }
        
        // 4. 扣除积分
        boolean deducted = creditService.deductPoints(userId, product.getPointsCost(), 
                "兑换商品：" + product.getProductName());
        if (!deducted) {
            result.put("success", false);
            result.put("message", "积分扣除失败");
            return result;
        }
        
        // 5. 创建兑换记录
        LocalDateTime expireTime = null;
        if ("vip_days".equals(product.getProductType()) || "feature".equals(product.getProductType())) {
            // VIP和功能类商品设置过期时间
            expireTime = LocalDateTime.now().plusDays(product.getProductValue());
        }
        
        ExchangeRecord record = ExchangeRecord.builder()
                .userId(userId)
                .productId(product.getId())
                .productCode(productCode)
                .productName(product.getProductName())
                .pointsCost(product.getPointsCost())
                .productValue(product.getProductValue())
                .status(1) // 已发放
                .expireTime(expireTime)
                .build();
        
        creditMapper.insertExchangeRecord(record);
        
        // 6. 发放商品（根据商品类型处理）
        distributeProduct(userId, product, record);
        
        result.put("success", true);
        result.put("message", "兑换成功");
        result.put("record", record);
        return result;
    }
    
    /**
     * 发放商品
     */
    private void distributeProduct(Long userId, ExchangeProduct product, ExchangeRecord record) {
        LocalDateTime now = LocalDateTime.now();
        switch (product.getProductType()) {
            case "divination_count":
                // 当前项目未实现“占卜次数额度表”，先保留兑换记录作为凭证
                log.info("用户 {} 兑换了 {} 次占卜（尚未落库额度，仅记录兑换流水）", userId, product.getProductValue());
                break;
            case "vip_days": {
                // 写入 tb_user_vip
                LocalDateTime endTime = now.plusDays(product.getProductValue());
                userVipMapper.insert(com.example.demo.entity.UserVip.builder()
                        .userId(userId)
                        .vipType("VIP")
                        .startTime(now)
                        .endTime(endTime)
                        .source("exchange")
                        .sourceId(record.getId())
                        .isActive(1)
                        .build());
                log.info("用户 {} 兑换了 {} 天VIP，生效至 {}", userId, product.getProductValue(), endTime);
                break;
            }
            case "feature": {
                // 写入 tb_user_feature_unlock
                LocalDateTime endTime = null;
                if (record.getExpireTime() != null) {
                    endTime = record.getExpireTime();
                }
                String featureCode = product.getProductCode();
                userFeatureUnlockMapper.insert(com.example.demo.entity.UserFeatureUnlock.builder()
                        .userId(userId)
                        .featureCode(featureCode)
                        .featureName(product.getProductName())
                        .unlockType("exchange")
                        .startTime(now)
                        .endTime(endTime)
                        .source("exchange")
                        .sourceId(record.getId())
                        .isActive(1)
                        .build());
                log.info("用户 {} 解锁功能 {}，有效期至 {}", userId, featureCode, endTime);
                break;
            }
            default:
                log.warn("未知商品类型：{}，productCode={}", product.getProductType(), product.getProductCode());
        }
    }
    
    /**
     * 获取用户的兑换记录
     */
    public List<ExchangeRecord> getUserExchangeRecords(Long userId) {
        return creditMapper.findExchangeRecordsByUserId(userId);
    }
}

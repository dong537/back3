package com.example.demo.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 成就统计缓存服务
 * 用于缓存用户统计数据，减少数据库查询
 */
@Service
@Slf4j
public class AchievementCacheService {

    /**
     * 用户占卜次数缓存
     * Key: userId, Value: 占卜次数
     */
    private final Map<Long, AtomicInteger> divinationCountCache = new ConcurrentHashMap<>();

    /**
     * 缓存过期时间（毫秒）
     * 默认5分钟
     */
    private static final long CACHE_EXPIRE_TIME = 5 * 60 * 1000;

    /**
     * 缓存时间戳
     * Key: userId, Value: 缓存时间戳
     */
    private final Map<Long, Long> cacheTimestamp = new ConcurrentHashMap<>();

    /**
     * 获取用户占卜次数（带缓存）
     * 
     * @param userId 用户ID
     * @param actualCount 实际查询的占卜次数（如果缓存不存在或过期）
     * @return 占卜次数
     */
    public int getDivinationCount(Long userId, int actualCount) {
        if (userId == null) {
            return actualCount;
        }

        // 检查缓存是否存在且未过期
        Long timestamp = cacheTimestamp.get(userId);
        AtomicInteger cachedCount = divinationCountCache.get(userId);

        if (cachedCount != null && timestamp != null) {
            long now = System.currentTimeMillis();
            if (now - timestamp < CACHE_EXPIRE_TIME) {
                // 缓存有效，使用缓存值
                int count = cachedCount.get();
                log.debug("使用缓存的占卜次数: userId={}, count={}", userId, count);
                return count;
            } else {
                // 缓存过期，清除
                divinationCountCache.remove(userId);
                cacheTimestamp.remove(userId);
            }
        }

        // 缓存不存在或过期，使用实际值并更新缓存
        divinationCountCache.put(userId, new AtomicInteger(actualCount));
        cacheTimestamp.put(userId, System.currentTimeMillis());
        log.debug("更新占卜次数缓存: userId={}, count={}", userId, actualCount);
        return actualCount;
    }

    /**
     * 增加用户占卜次数（更新缓存）
     * 
     * @param userId 用户ID
     */
    public void incrementDivinationCount(Long userId) {
        if (userId == null) {
            return;
        }

        AtomicInteger count = divinationCountCache.computeIfAbsent(userId, k -> new AtomicInteger(0));
        int newCount = count.incrementAndGet();
        cacheTimestamp.put(userId, System.currentTimeMillis());
        log.debug("增加占卜次数缓存: userId={}, newCount={}", userId, newCount);
    }

    /**
     * 清除用户缓存
     * 
     * @param userId 用户ID
     */
    public void clearCache(Long userId) {
        if (userId == null) {
            return;
        }
        divinationCountCache.remove(userId);
        cacheTimestamp.remove(userId);
        log.debug("清除占卜次数缓存: userId={}", userId);
    }

    /**
     * 清除所有缓存
     */
    public void clearAllCache() {
        divinationCountCache.clear();
        cacheTimestamp.clear();
        log.info("清除所有占卜次数缓存");
    }

    /**
     * 获取缓存统计信息（用于监控）
     */
    public Map<String, Object> getCacheStats() {
        Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("cacheSize", divinationCountCache.size());
        stats.put("cacheKeys", divinationCountCache.keySet());
        return stats;
    }
}

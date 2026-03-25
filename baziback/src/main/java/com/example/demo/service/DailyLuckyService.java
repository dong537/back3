package com.example.demo.service;

import com.example.demo.entity.DailyLucky;
import com.example.demo.exception.BusinessException;
import com.example.demo.mapper.DailyLuckyMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 每日幸运服务
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DailyLuckyService {
    
    private final DailyLuckyMapper dailyLuckyMapper;
    
    /**
     * 获取指定日期的每日幸运
     */
    public DailyLucky getDailyLucky(LocalDate date) {
        if (date == null) {
            date = LocalDate.now();
        }
        
        DailyLucky dailyLucky = dailyLuckyMapper.findByDate(date);
        
        // 如果没有找到，返回默认值或随机生成
        if (dailyLucky == null) {
            log.warn("日期 {} 的每日幸运数据不存在，返回默认值", date);
            return getDefaultDailyLucky(date);
        }
        
        return dailyLucky;
    }
    
    /**
     * 获取今天的每日幸运
     */
    public DailyLucky getTodayLucky() {
        return getDailyLucky(LocalDate.now());
    }
    
    /**
     * 获取未来几天的每日幸运
     */
    public List<DailyLucky> getFutureLucky(int days) {
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(days - 1);
        return dailyLuckyMapper.findByDateRange(today, endDate);
    }
    
    /**
     * 获取日期范围的每日幸运
     */
    public List<DailyLucky> getLuckyByDateRange(LocalDate startDate, LocalDate endDate) {
        return dailyLuckyMapper.findByDateRange(startDate, endDate);
    }
    
    /**
     * 创建或更新每日幸运
     */
    public DailyLucky saveDailyLucky(DailyLucky dailyLucky) {
        if (dailyLucky == null) {
            throw new BusinessException("每日幸运数据不能为空");
        }
        
        if (dailyLucky.getId() == null) {
            // 检查该日期是否已存在
            DailyLucky existing = dailyLuckyMapper.findByDate(dailyLucky.getLuckyDate());
            if (existing != null) {
                // 更新现有记录
                dailyLucky.setId(existing.getId());
                dailyLuckyMapper.update(dailyLucky);
                return dailyLucky;
            } else {
                // 插入新记录
                dailyLuckyMapper.insert(dailyLucky);
                return dailyLucky;
            }
        } else {
            // 更新现有记录
            dailyLuckyMapper.update(dailyLucky);
            return dailyLucky;
        }
    }
    
    /**
     * 获取默认的每日幸运（当数据库中没有数据时）
     */
    private DailyLucky getDefaultDailyLucky(LocalDate date) {
        // 根据日期生成简单的默认值
        int dayOfYear = date.getDayOfYear();
        String[] numbers = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "0"};
        String[] colors = {"葡萄紫", "天空蓝", "翡翠绿", "玫瑰红", "柠檬黄", "深海蓝", "樱花粉", "薄荷绿", "象牙白", "薰衣草紫"};
        String[] constellations = {"白羊座", "金牛座", "双子座", "巨蟹座", "狮子座", "处女座", "天秤座", "天蝎座", "射手座", "摩羯座", "水瓶座", "双鱼座"};
        String[] foods = {"红烧排骨", "清蒸鱼", "白切鸡", "糖醋里脊", "麻婆豆腐", "宫保鸡丁", "水煮肉片", "回锅肉", "鱼香肉丝", "蒜蓉西兰花"};
        
        return DailyLucky.builder()
                .luckyDate(date)
                .luckyNumber(numbers[dayOfYear % numbers.length])
                .luckyColor(colors[dayOfYear % colors.length])
                .luckyConstellation(constellations[dayOfYear % constellations.length])
                .luckyFood(foods[dayOfYear % foods.length])
                .suitableActions("保持积极心态 规划未来 与他人交流")
                .unsuitableActions("过度焦虑 冲动决策 忽视健康")
                .description("今日运势平稳，保持乐观心态")
                .build();
    }
    
    /**
     * 格式化每日幸运数据为前端需要的格式
     */
    public Map<String, Object> formatDailyLucky(DailyLucky dailyLucky) {
        if (dailyLucky == null) {
            return new HashMap<>();
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("date", dailyLucky.getLuckyDate().toString());
        result.put("luckyNumber", dailyLucky.getLuckyNumber());
        result.put("luckyColor", dailyLucky.getLuckyColor());
        result.put("luckyConstellation", dailyLucky.getLuckyConstellation());
        result.put("luckyFood", dailyLucky.getLuckyFood());
        
        // 将宜忌事项拆分为数组
        result.put("suitableActions", dailyLucky.getSuitableActions() != null 
            ? dailyLucky.getSuitableActions().split("[\\s,，]+") 
            : new String[0]);
        result.put("unsuitableActions", dailyLucky.getUnsuitableActions() != null 
            ? dailyLucky.getUnsuitableActions().split("[\\s,，]+") 
            : new String[0]);
        
        result.put("description", dailyLucky.getDescription());
        
        return result;
    }
}

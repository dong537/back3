package com.example.demo.bazi.analyzer;

import com.example.demo.bazi.constants.*;
import com.example.demo.bazi.model.*;
import java.util.*;

/**
 * 大运分析器
 */
public class DaYunAnalyzer {

    /**
     * 计算大运
     * @param chart 八字命盘
     * @param birthYear 出生年份
     * @param isMale 是否男命
     * @param startAge 起运岁数（通常为1-10之间）
     * @return 大运数据
     */
    public Map<String, Object> calculate(BaZiChart chart, int birthYear, boolean isMale, int startAge) {
        Map<String, Object> result = new LinkedHashMap<>();
        
        int qiYunYear = birthYear + startAge;
        result.put("起运日期", qiYunYear);
        
        List<Map<String, Object>> daYunList = new ArrayList<>();
        
        // 获取月柱天干地支
        String yueGan = chart.getYueZhu().getTianGan();
        String yueZhi = chart.getYueZhu().getDiZhi();
        String riZhu = chart.getRiZhuTianGan();
        
        TianGan currentGan = TianGan.fromName(yueGan);
        DiZhi currentZhi = DiZhi.fromName(yueZhi);
        
        // 判断顺逆
        // 阳年男命、阴年女命顺行；阴年男命、阳年女命逆行
        String nianGan = chart.getNianZhu().getTianGan();
        boolean isYangYear = TianGan.fromName(nianGan).isYang();
        boolean isShun = (isYangYear && isMale) || (!isYangYear && !isMale);
        
        // 计算10步大运
        for (int i = 0; i < 10; i++) {
            if (isShun) {
                currentGan = TianGan.fromIndex(currentGan.getIndex() + 1);
                currentZhi = DiZhi.fromIndex(currentZhi.getIndex() + 1);
            } else {
                currentGan = TianGan.fromIndex(currentGan.getIndex() - 1 + 10);
                currentZhi = DiZhi.fromIndex(currentZhi.getIndex() - 1 + 12);
            }
            
            String ganZhi = currentGan.getName() + currentZhi.getName();
            int startYear = qiYunYear + i * 10;
            
            DaYun daYun = new DaYun(ganZhi, startYear, riZhu);
            daYunList.add(daYun.toMap());
        }
        
        result.put("大运", daYunList);
        
        return result;
    }

    /**
     * 使用默认参数计算大运（假设起运年龄为3岁）
     */
    public Map<String, Object> calculate(BaZiChart chart, int birthYear, boolean isMale) {
        // 默认起运年龄为3岁
        return calculate(chart, birthYear, isMale, 3);
    }

    /**
     * 从指定起运年份开始计算大运
     */
    public Map<String, Object> calculateFromStartYear(BaZiChart chart, int startYear, boolean isMale) {
        Map<String, Object> result = new LinkedHashMap<>();
        
        result.put("起运日期", startYear);
        
        List<Map<String, Object>> daYunList = new ArrayList<>();
        
        String yueGan = chart.getYueZhu().getTianGan();
        String yueZhi = chart.getYueZhu().getDiZhi();
        String riZhu = chart.getRiZhuTianGan();
        
        TianGan currentGan = TianGan.fromName(yueGan);
        DiZhi currentZhi = DiZhi.fromName(yueZhi);
        
        String nianGan = chart.getNianZhu().getTianGan();
        boolean isYangYear = TianGan.fromName(nianGan).isYang();
        boolean isShun = (isYangYear && isMale) || (!isYangYear && !isMale);
        
        for (int i = 0; i < 10; i++) {
            if (isShun) {
                currentGan = TianGan.fromIndex(currentGan.getIndex() + 1);
                currentZhi = DiZhi.fromIndex(currentZhi.getIndex() + 1);
            } else {
                currentGan = TianGan.fromIndex(currentGan.getIndex() - 1 + 10);
                currentZhi = DiZhi.fromIndex(currentZhi.getIndex() - 1 + 12);
            }
            
            String ganZhi = currentGan.getName() + currentZhi.getName();
            int currentStartYear = startYear + i * 10;
            
            DaYun daYun = new DaYun(ganZhi, currentStartYear, riZhu);
            daYunList.add(daYun.toMap());
        }
        
        result.put("大运", daYunList);
        
        return result;
    }
}

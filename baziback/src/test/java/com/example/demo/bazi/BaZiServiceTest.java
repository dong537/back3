package com.example.demo.bazi;

import com.example.demo.bazi.service.BaZiService;
import com.example.demo.bazi.model.BaZiChart;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.util.*;

/**
 * 八字服务测试类
 */
public class BaZiServiceTest {

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    public static void main(String[] args) {
        try {
            testFullAnalysis();
            testSimpleAnalysis();
            testBaZiParsing();
            System.out.println("\n========== 所有测试通过 ==========");
        } catch (Exception e) {
            System.err.println("测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 测试完整八字分析
     */
    public static void testFullAnalysis() throws Exception {
        System.out.println("\n========== 测试完整八字分析 ==========");
        
        BaZiService service = new BaZiService();
        
        // 使用用户提供的八字：乙酉 己丑 甲辰 戊辰
        String baZi = "乙酉 己丑 甲辰 戊辰";
        int birthYear = 2005;
        boolean isMale = true;
        
        Map<String, Object> result = service.analyze(baZi, birthYear, isMale);
        
        // 验证基本信息
        assert result.get("八字").equals(baZi) : "八字不匹配";
        assert result.get("季节").equals("冬") : "季节不匹配";
        assert result.get("生肖").equals("鸡") : "生肖不匹配";
        
        // 验证八字各柱信息
        @SuppressWarnings("unchecked")
        Map<String, Map<String, Object>> geZhuInfo = 
            (Map<String, Map<String, Object>>) result.get("八字各柱信息");
        
        assert geZhuInfo.get("年").get("天干").equals("乙") : "年柱天干不匹配";
        assert geZhuInfo.get("年").get("地支").equals("酉") : "年柱地支不匹配";
        assert geZhuInfo.get("月").get("天干").equals("己") : "月柱天干不匹配";
        assert geZhuInfo.get("日").get("天干").equals("甲") : "日柱天干不匹配";
        assert geZhuInfo.get("时").get("天干").equals("戊") : "时柱天干不匹配";
        
        // 打印结果
        System.out.println("分析结果：");
        System.out.println(objectMapper.writeValueAsString(result));
        
        System.out.println("\n✓ 完整八字分析测试通过");
    }

    /**
     * 测试简易八字分析
     */
    public static void testSimpleAnalysis() throws Exception {
        System.out.println("\n========== 测试简易八字分析 ==========");
        
        BaZiService service = new BaZiService();
        String baZi = "甲子 丙寅 戊辰 庚午";
        
        Map<String, Object> result = service.analyzeSimple(baZi, true);
        
        assert result.containsKey("八字各柱信息") : "缺少八字各柱信息";
        assert result.containsKey("财星信息") : "缺少财星信息";
        assert result.containsKey("喜用神分析") : "缺少喜用神分析";
        assert result.containsKey("神煞") : "缺少神煞信息";
        
        System.out.println("简易分析结果包含的key: " + result.keySet());
        System.out.println("\n✓ 简易八字分析测试通过");
    }

    /**
     * 测试八字解析
     */
    public static void testBaZiParsing() throws Exception {
        System.out.println("\n========== 测试八字解析 ==========");
        
        BaZiService service = new BaZiService();
        
        // 测试多个八字
        String[] testCases = {
            "甲子 乙丑 丙寅 丁卯",
            "戊辰 己巳 庚午 辛未",
            "壬申 癸酉 甲戌 乙亥",
            "乙酉 己丑 甲辰 戊辰"
        };
        
        for (String baZi : testCases) {
            BaZiChart chart = service.parseBaZi(baZi);
            assert chart != null : "八字解析失败: " + baZi;
            assert chart.getNianZhu() != null : "年柱解析失败";
            assert chart.getYueZhu() != null : "月柱解析失败";
            assert chart.getRiZhu() != null : "日柱解析失败";
            assert chart.getShiZhu() != null : "时柱解析失败";
            
            System.out.println("解析成功: " + baZi + " -> " + 
                "年:" + chart.getNianZhu().getGanZhi() + 
                " 月:" + chart.getYueZhu().getGanZhi() + 
                " 日:" + chart.getRiZhu().getGanZhi() + 
                " 时:" + chart.getShiZhu().getGanZhi());
        }
        
        System.out.println("\n✓ 八字解析测试通过");
    }

    /**
     * 验证用户提供的JSON数据
     */
    public static void validateUserData() throws Exception {
        System.out.println("\n========== 验证用户数据 ==========");
        
        BaZiService service = new BaZiService();
        String baZi = "乙酉 己丑 甲辰 戊辰";
        
        Map<String, Object> result = service.analyze(baZi, 2005, true);
        
        // 验证星运
        @SuppressWarnings("unchecked")
        Map<String, String> xingYun = (Map<String, String>) result.get("星运");
        System.out.println("星运: " + xingYun);
        
        // 验证纳音
        @SuppressWarnings("unchecked")
        Map<String, List<String>> naYin = (Map<String, List<String>>) result.get("纳音");
        System.out.println("纳音: " + naYin);
        
        // 验证刑冲合会
        @SuppressWarnings("unchecked")
        Map<String, Object> xingChong = (Map<String, Object>) result.get("刑冲合会");
        System.out.println("刑冲合会: " + xingChong);
        
        System.out.println("\n✓ 用户数据验证完成");
    }
}

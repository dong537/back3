package com.example.demo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 多语言支持服务
 * 对标 cantian.ai 的多语言功能
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MultiLanguageService {

    private final DeepSeekService deepSeekService;

    // 支持的语言列表
    private static final Map<String, String> SUPPORTED_LANGUAGES = new HashMap<>();
    
    static {
        SUPPORTED_LANGUAGES.put("zh-CN", "简体中文");
        SUPPORTED_LANGUAGES.put("zh-TW", "繁體中文");
        SUPPORTED_LANGUAGES.put("en", "English");
        SUPPORTED_LANGUAGES.put("ja", "日本語");
        SUPPORTED_LANGUAGES.put("ko", "한국어");
        SUPPORTED_LANGUAGES.put("es", "Español");
        SUPPORTED_LANGUAGES.put("fr", "Français");
        SUPPORTED_LANGUAGES.put("de", "Deutsch");
        SUPPORTED_LANGUAGES.put("ru", "Русский");
        SUPPORTED_LANGUAGES.put("pt", "Português");
    }

    /**
     * 获取支持的语言列表
     */
    public Map<String, String> getSupportedLanguages() {
        return new HashMap<>(SUPPORTED_LANGUAGES);
    }

    /**
     * 翻译八字分析结果
     */
    public String translateBaziAnalysis(String originalContent, String targetLanguage) throws Exception {
        if (!SUPPORTED_LANGUAGES.containsKey(targetLanguage)) {
            throw new IllegalArgumentException("不支持的目标语言：" + targetLanguage);
        }

        String languageName = SUPPORTED_LANGUAGES.get(targetLanguage);
        log.info("开始翻译八字分析结果到：{}", languageName);

        StringBuilder prompt = new StringBuilder();
        prompt.append("请将以下八字命理分析内容翻译成").append(languageName).append("，");
        prompt.append("保持专业术语的准确性，确保命理概念的正确传达。\n\n");
        prompt.append("原文：\n").append(originalContent);

        return deepSeekService.chat(prompt.toString());
    }

    /**
     * 生成多语言八字报告
     */
    public String generateMultiLanguageReport(String baziData, String reportType, String language) throws Exception {
        if (!SUPPORTED_LANGUAGES.containsKey(language)) {
            throw new IllegalArgumentException("不支持的语言：" + language);
        }

        String languageName = SUPPORTED_LANGUAGES.get(language);
        log.info("生成{}语言的八字报告，类型：{}", languageName, reportType);

        StringBuilder prompt = new StringBuilder();
        prompt.append("请用").append(languageName).append("生成一份详细的八字命理分析报告。\n\n");
        prompt.append("八字信息：\n").append(baziData).append("\n\n");
        
        prompt.append("报告类型：").append(getReportTypeName(reportType, language)).append("\n\n");
        
        prompt.append("请按照以下结构生成报告：\n");
        prompt.append("1. 概述\n");
        prompt.append("2. 详细分析\n");
        prompt.append("3. 具体建议\n");
        prompt.append("4. 注意事项\n");
        prompt.append("5. 总结\n\n");
        
        prompt.append("注意：\n");
        prompt.append("- 使用").append(languageName).append("撰写\n");
        prompt.append("- 保持专业性和准确性\n");
        prompt.append("- 适当保留关键命理术语的原文（如有必要）\n");

        return deepSeekService.chat(prompt.toString());
    }

    /**
     * 翻译趋势分析结果
     */
    public Map<String, Object> translateTrendAnalysis(Map<String, Object> trendData, String targetLanguage) throws Exception {
        if (!SUPPORTED_LANGUAGES.containsKey(targetLanguage)) {
            throw new IllegalArgumentException("不支持的目标语言：" + targetLanguage);
        }

        String languageName = SUPPORTED_LANGUAGES.get(targetLanguage);
        log.info("开始翻译趋势分析到：{}", languageName);

        // 这里简化处理，实际应该递归翻译所有文本字段
        Map<String, Object> translatedData = new HashMap<>(trendData);
        
        // 翻译AI解读部分
        if (trendData.containsKey("aiInsight")) {
            String originalInsight = (String) trendData.get("aiInsight");
            String translatedInsight = translateBaziAnalysis(originalInsight, targetLanguage);
            translatedData.put("aiInsight", translatedInsight);
        }

        return translatedData;
    }

    /**
     * 获取本地化的报告类型名称
     */
    private String getReportTypeName(String reportType, String language) {
        Map<String, Map<String, String>> typeNames = new HashMap<>();
        
        // 中文
        Map<String, String> zhNames = new HashMap<>();
        zhNames.put("comprehensive", "综合分析");
        zhNames.put("career", "事业运势");
        zhNames.put("love", "感情运势");
        zhNames.put("health", "健康运势");
        zhNames.put("wealth", "财运分析");
        typeNames.put("zh-CN", zhNames);
        typeNames.put("zh-TW", zhNames);
        
        // 英文
        Map<String, String> enNames = new HashMap<>();
        enNames.put("comprehensive", "Comprehensive Analysis");
        enNames.put("career", "Career Fortune");
        enNames.put("love", "Love Fortune");
        enNames.put("health", "Health Fortune");
        enNames.put("wealth", "Wealth Analysis");
        typeNames.put("en", enNames);
        
        // 日文
        Map<String, String> jaNames = new HashMap<>();
        jaNames.put("comprehensive", "総合分析");
        jaNames.put("career", "仕事運");
        jaNames.put("love", "恋愛運");
        jaNames.put("health", "健康運");
        jaNames.put("wealth", "財運分析");
        typeNames.put("ja", jaNames);
        
        // 韩文
        Map<String, String> koNames = new HashMap<>();
        koNames.put("comprehensive", "종합 분석");
        koNames.put("career", "직업 운세");
        koNames.put("love", "애정 운세");
        koNames.put("health", "건강 운세");
        koNames.put("wealth", "재물 분석");
        typeNames.put("ko", koNames);

        return typeNames.getOrDefault(language, enNames).getOrDefault(reportType, "Comprehensive Analysis");
    }

    /**
     * 验证语言代码是否支持
     */
    public boolean isLanguageSupported(String language) {
        return SUPPORTED_LANGUAGES.containsKey(language);
    }

    /**
     * 获取默认语言
     */
    public String getDefaultLanguage() {
        return "zh-CN";
    }
}

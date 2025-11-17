package com.example.demo.controller;

import com.example.demo.dto.request.yijing.*;
import com.example.demo.dto.response.McpCallResult;
import com.example.demo.service.YijingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 易经八字MCP服务控制器
 * 提供卦象生成、八字分析、命理咨询等RESTful API接口
 */
@RestController
@Slf4j
@Validated
@RequestMapping("/api/yijing")
@RequiredArgsConstructor
public class YijingController {

    private final YijingService yijingService;

    /**
     * 获取可用工具列表
     * 返回MCP服务器支持的所有工具定义
     *
     * @return 工具列表JSON字符串
     */
    @GetMapping("/tools")
    public ResponseEntity<String> listTools() {
        return ResponseEntity.ok(yijingService.listTools());
    }

    /**
     * 生成六爻卦象
     * 根据指定方法生成易经卦象
     *
     * 请求参数：
     * - method: 起卦方式 (number/time/plum_blossom/random)
     * - question: 求卦意图/问题描述
     * - seed: 起卦种子信息（可选）
     *
     * 示例请求：
     * {
     *   "method": "number",
     *   "question": "今天的运势如何？",
     *   "seed": "123"
     * }
     *
     * @param request 起卦请求参数
     * @return 生成的卦象数据
     */
    @PostMapping("/hexagram/generate")
    public ResponseEntity<Map<String, Object>> generateHexagram(@RequestBody @Validated YijingGenerateHexagramRequest request) {
        return ResponseEntity.ok(toResponse(yijingService.generateHexagram(request)));
    }

    /**
     * 解读卦象含义
     * 对生成的卦象进行多维度解读分析
     *
     * 请求参数：
     * - hexagram: 卦象数据对象（包含本卦和变卦）
     * - focus: 解读焦点 (overall/specific_line/changing)
     * - line_number: 关注的爻位1-6（当focus为specific_line时必需）
     * - context: 应用场景 (general/career/relationship/health/finance)
     * - detail_level: 详细程度 (brief/standard/detailed)
     *
     * 示例请求：
     * {
     *   "hexagram": {"name": "乾", "lines": [1,1,1,1,1,1]},
     *   "focus": "overall",
     *   "context": "career",
     *   "detail_level": "detailed"
     * }
     *
     * @param request 卦象解读请求参数
     * @return 解读结果数据
     */
    @PostMapping("/hexagram/interpret")
    public ResponseEntity<Map<String, Object>> interpretHexagram(@RequestBody @Validated YijingInterpretRequest request) {
        return ResponseEntity.ok(toResponse(yijingService.interpretHexagram(request)));
    }

    /**
     * 卦象决策建议
     * 基于卦象为用户提供行动建议
     *
     * 请求参数：
     * - hexagram: 卦象数据对象
     * - question: 用户的具体问题
     * - options: 可选行动方案列表（可选）
     * - time_frame: 决策时间框架 (immediate/short_term/long_term)
     *
     * 示例请求：
     * {
     *   "hexagram": {"name": "谦", "lines": [0,0,0,1,1,1]},
     *   "question": "是否应该接受这份工作？",
     *   "options": ["接受", "拒绝", "再考虑"],
     *   "time_frame": "short_term"
     * }
     *
     * @param request 决策建议请求参数
     * @return 建议方案数据
     */
    @PostMapping("/hexagram/advise")
    public ResponseEntity<Map<String, Object>> advise(@RequestBody @Validated YijingAdviseRequest request) {
        return ResponseEntity.ok(toResponse(yijingService.advise(request)));
    }

    /**
     * 生成八字命盘
     * 根据出生信息生成四柱八字命盘
     *
     * 请求参数：
     * - birth_time: 出生时间（阳历，ISO格式）
     * - is_lunar: 是否为农历日期（可选）
     * - gender: 性别 (male/female)
     * - birth_location: 出生地经纬度（可选，用于真太阳时校正）
     *   - longitude: 经度
     *   - latitude: 纬度
     *
     * 示例请求：
     * {
     *   "birth_time": "1990-01-01T08:30:00",
     *   "gender": "male",
     *   "is_lunar": false,
     *   "birth_location": {"longitude": 116.4, "latitude": 39.9}
     * }
     *
     * @param request 八字排盘请求参数
     * @return 八字命盘数据
     */
    @PostMapping("/bazi/chart/generate")
    public ResponseEntity<Map<String, Object>> generateBaziChart(@RequestBody @Validated YijingBaziGenerateChartRequest request) {
        return ResponseEntity.ok(toResponse(yijingService.generateBaziChart(request)));
    }

    /**
     * 分析八字命盘
     * 对八字命盘进行多维度命理分析
     *
     * 请求参数：
     * - chart: 八字命盘数据对象
     * - analysis_type: 分析类型数组 [personality/career/wealth/relationship/health]
     * - detail_level: 详细程度 (brief/standard/detailed)
     *
     * 示例请求：
     * {
     *   "chart": {"year": "庚午", "month": "戊子", "day": "甲寅", "hour": "乙亥"},
     *   "analysis_type": ["personality", "career"],
     *   "detail_level": "detailed"
     * }
     *
     * @param request 八字分析请求参数
     * @return 分析结果数据
     */
    @PostMapping("/bazi/analyze")
    public ResponseEntity<Map<String, Object>> analyzeBazi(@RequestBody @Validated YijingBaziAnalyzeRequest request) {
        return ResponseEntity.ok(toResponse(yijingService.analyzeBazi(request)));
    }

    /**
     * 预测未来运势
     * 基于八字命盘进行运程预测
     *
     * 请求参数：
     * - chart: 八字命盘数据对象
     * - start_date: 预测起始日期（ISO格式）
     * - end_date: 预测结束日期（ISO格式）
     * - aspects: 预测方面数组 [overall/career/wealth/relationship/health]
     * - resolution: 预测精度 (year/month/day)
     *
     * 示例请求：
     * {
     *   "chart": {"year": "庚午", "month": "戊子", "day": "甲寅", "hour": "乙亥"},
     *   "start_date": "2025-01-01",
     *   "end_date": "2025-12-31",
     *   "aspects": ["career", "wealth"],
     *   "resolution": "month"
     * }
     *
     * @param request 运势预测请求参数
     * @return 运程预测数据
     */
    @PostMapping("/bazi/forecast")
    public ResponseEntity<Map<String, Object>> forecastBazi(@RequestBody @Validated YijingBaziForecastRequest request) {
        return ResponseEntity.ok(toResponse(yijingService.forecastBazi(request)));
    }

    /**
     * 易经八字综合分析
     * 结合八字和卦象进行双重命理分析
     *
     * 请求参数：
     * - bazi_chart: 八字命盘数据对象
     * - hexagram: 卦象数据（可选，不提供则从八字自动生成）
     * - question: 用户的具体问题
     * - analysis_aspects: 分析重点数组
     *
     * 示例请求：
     * {
     *   "bazi_chart": {"year": "庚午", "month": "戊子", "day": "甲寅", "hour": "乙亥"},
     *   "question": "适合创业吗？",
     *   "analysis_aspects": ["career", "wealth", "timing"],
     *   "hexagram": {"name": "屯", "lines": [1,0,0,0,1,0]}
     * }
     *
     * @param request 综合分析请求参数
     * @return 综合分析结果
     */
    @PostMapping("/combined-analysis")
    public ResponseEntity<Map<String, Object>> combinedAnalysis(@RequestBody YijingCombinedAnalysisRequest request) {
        return ResponseEntity.ok(toResponse(yijingService.combinedAnalysis(request)));
    }

    /**
     * 命理咨询服务
     * 提供个性化的命理咨询和建议
     *
     * 请求参数：
     * - user_profile: 用户基本信息对象（包含八字等命理数据）
     * - question: 咨询问题描述
     * - context: 历史咨询上下文（可选）
     * - consultation_type: 咨询类型 (guidance/analysis/prediction/suggestion)
     *
     * 示例请求：
     * {
     *   "user_profile": {"name": "张三", "bazi": {"year": "庚午", "month": "戊子", "day": "甲寅"}},
     *   "question": "最近感情困扰，不知道如何选择？",
     *   "consultation_type": "guidance",
     *   "context": [{"previous_question": "...", "answer": "..."}]
     * }
     *
     * @param request 咨询请求参数
     * @return 咨询建议结果
     */
    @PostMapping("/destiny-consult")
    public ResponseEntity<Map<String, Object>> destinyConsult(@RequestBody @Validated YijingDestinyConsultRequest request) {
        return ResponseEntity.ok(toResponse(yijingService.destinyConsult(request)));
    }

    /**
     * 易经八字知识学习
     * 提供体系化的命理知识学习服务
     *
     * 请求参数：
     * - topic: 学习主题（如"五行相生相克"、"六爻基础"等）
     * - system: 知识体系 (yijing/bazi/both)
     * - level: 学习级别 (beginner/intermediate/advanced)
     * - format: 内容格式 (text/interactive/visual)
     *
     * 示例请求：
     * {
     *   "topic": "五行相生相克",
     *   "system": "both",
     *   "level": "beginner",
     *   "format": "interactive"
     * }
     *
     * @param request 学习请求参数
     * @return 学习内容和资料
     */
    @PostMapping("/knowledge/learn")
    public ResponseEntity<Map<String, Object>> knowledgeLearn(@RequestBody @Validated YijingKnowledgeLearnRequest request) {
        return ResponseEntity.ok(toResponse(yijingService.knowledgeLearn(request)));
    }

    /**
     * 命理案例分析
     * 提供历史或现代命理案例研究
     *
     * 请求参数：
     * - system: 案例类型 (yijing/bazi/combined)
     * - case_id: 特定案例ID（可选，不提供则返回案例列表）
     * - category: 案例分类（如"历史人物"、"现代名人"等，可选）
     * - analysis_focus: 分析重点数组（可选）
     *
     * 示例请求：
     * {
     *   "system": "bazi",
     *   "category": "历史人物",
     *   "analysis_focus": ["格局分析", "大运走势"],
     *   "case_id": "case_001"
     * }
     *
     * @param request 案例研究请求参数
     * @return 案例详情或列表
     */
    @PostMapping("/case-study")
    public ResponseEntity<Map<String, Object>> caseStudy(@RequestBody YijingCaseStudyRequest request) {
        return ResponseEntity.ok(toResponse(yijingService.caseStudy(request)));
    }

    /**
     * 统一响应格式转换
     * 将MCP调用结果转换为标准响应格式
     *
     * @param result MCP服务调用结果
     * @return 包含success、data、raw的标准响应Map
     */
    private Map<String, Object> toResponse(McpCallResult result) {
        Map<String, Object> map = new HashMap<>();
        map.put("success", result != null && result.isSuccess());
        map.put("data", result == null ? null : result.getData());
        map.put("raw", result == null ? null : result.getRaw());
        return map;
    }
}
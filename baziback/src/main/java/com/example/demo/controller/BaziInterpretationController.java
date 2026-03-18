package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.exception.BusinessException;
import com.example.demo.service.BaziInterpretationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 八字解释控制器
 */
@RestController
@RequestMapping("/api/bazi/interpretation")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin
public class BaziInterpretationController {

    private final BaziInterpretationService baziInterpretationService;

    /**
     * 根据十神类型和干支位置获取解释
     */
    @GetMapping("/by-type-position")
    public Result<Map<String, Object>> getInterpretation(
            @RequestParam String godType,
            @RequestParam String ganzhiPosition) {

        log.info("查询八字解释: godType={}, ganzhiPosition={}", godType, ganzhiPosition);

        if (godType == null || godType.isBlank()) {
            throw new BusinessException("godType不能为空");
        }
        if (ganzhiPosition == null || ganzhiPosition.isBlank()) {
            throw new BusinessException("ganzhiPosition不能为空");
        }

        var interpretation = baziInterpretationService.getInterpretation(godType, ganzhiPosition);
        if (interpretation == null) {
            log.warn("未找到对应的解释: godType={}, ganzhiPosition={}", godType, ganzhiPosition);
            throw new BusinessException("未找到对应的解释");
        }

        return Result.success(
                baziInterpretationService.buildInterpretationDetailMap(interpretation)
        );
    }

    /**
     * 根据八字数据提取十神信息并获取对应的解释
     */
    @PostMapping("/from-bazi-data")
    public Result<List<Map<String, Object>>> getInterpretationsFromBaziData(@RequestBody Map<String, Object> baziData) {
        log.info("批量查询八字解释: baziData keys={}", baziData != null ? baziData.keySet() : "null");

        if (baziData == null || baziData.isEmpty()) {
            throw new BusinessException("baziData不能为空");
        }

        List<Map<String, Object>> interpretations = baziInterpretationService.getInterpretationsFromBaziData(baziData);
        log.info("查询到 {} 条解释", interpretations.size());

        return Result.success(interpretations);
    }
}

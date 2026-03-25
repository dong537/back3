package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.dto.response.bazi.BaziInterpretationResponse;
import com.example.demo.dto.response.bazi.BaziResponseMapper;
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

@RestController
@RequestMapping("/api/bazi/interpretation")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin
public class BaziInterpretationController {

    private final BaziInterpretationService baziInterpretationService;

    @GetMapping("/by-type-position")
    public Result<BaziInterpretationResponse> getInterpretation(
            @RequestParam String godType,
            @RequestParam String ganzhiPosition) {

        log.info("查询八字解读: godType={}, ganzhiPosition={}", godType, ganzhiPosition);

        if (godType == null || godType.isBlank()) {
            throw new BusinessException("godType不能为空");
        }
        if (ganzhiPosition == null || ganzhiPosition.isBlank()) {
            throw new BusinessException("ganzhiPosition不能为空");
        }

        var interpretation = baziInterpretationService.getInterpretation(godType, ganzhiPosition);
        if (interpretation == null) {
            log.warn("未找到对应的解读: godType={}, ganzhiPosition={}", godType, ganzhiPosition);
            throw new BusinessException("未找到对应的解读");
        }

        return Result.success(BaziResponseMapper.fromInterpretationMap(
                baziInterpretationService.buildInterpretationDetailMap(interpretation)
        ));
    }

    @PostMapping("/from-bazi-data")
    public Result<List<BaziInterpretationResponse>> getInterpretationsFromBaziData(
            @RequestBody Map<String, Object> baziData) {
        log.info("批量查询八字解读: baziData keys={}", baziData != null ? baziData.keySet() : "null");

        if (baziData == null || baziData.isEmpty()) {
            throw new BusinessException("baziData不能为空");
        }

        List<BaziInterpretationResponse> interpretations = baziInterpretationService
                .getInterpretationsFromBaziData(baziData)
                .stream()
                .map(BaziResponseMapper::fromInterpretationMap)
                .toList();
        log.info("查询到 {} 条解读", interpretations.size());

        return Result.success(interpretations);
    }
}

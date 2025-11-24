package com.example.demo.controller;

import com.example.demo.annotation.RequireAuth;
import com.example.demo.dto.request.bazi.CreateBaziInfoRequest;
import com.example.demo.dto.request.bazi.UpdateBaziInfoRequest;
import com.example.demo.dto.response.Result;
import com.example.demo.dto.response.bazi.UserBaziInfoResponse;
import com.example.demo.entity.UserBaziInfo;
import com.example.demo.service.UserBaziInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户八字信息控制器
 */
@Slf4j
@Tag(name = "用户八字信息管理", description = "用户八字信息的增删改查")
@RestController
@RequestMapping("/api/bazi")
@RequiredArgsConstructor
public class UserBaziInfoController {
    
    private final UserBaziInfoService baziInfoService;
    
    /**
     * 创建八字信息
     */
    @Operation(summary = "创建八字信息", description = "为用户创建新的八字信息")
    @RequireAuth
    @PostMapping("/info")
    public Result<UserBaziInfoResponse> createBaziInfo(
            @Validated @RequestBody CreateBaziInfoRequest request,
            HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            log.info("创建八字信息，用户ID：{}", userId);
            
            UserBaziInfo baziInfo = new UserBaziInfo();
            BeanUtils.copyProperties(request, baziInfo);
            baziInfo.setUserId(userId);
            
            UserBaziInfo created = baziInfoService.createBaziInfo(baziInfo);
            UserBaziInfoResponse response = convertToResponse(created);
            
            return Result.success(response);
        } catch (Exception e) {
            log.error("创建八字信息失败", e);
            return Result.error("创建八字信息失败：" + e.getMessage());
        }
    }
    
    /**
     * 更新八字信息
     */
    @Operation(summary = "更新八字信息", description = "更新已有的八字信息")
    @RequireAuth
    @PutMapping("/info")
    public Result<UserBaziInfoResponse> updateBaziInfo(
            @Validated @RequestBody UpdateBaziInfoRequest request,
            HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            log.info("更新八字信息，用户ID：{}，八字ID：{}", userId, request.getId());
            
            UserBaziInfo baziInfo = new UserBaziInfo();
            BeanUtils.copyProperties(request, baziInfo);
            baziInfo.setUserId(userId);
            
            UserBaziInfo updated = baziInfoService.updateBaziInfo(baziInfo);
            UserBaziInfoResponse response = convertToResponse(updated);
            
            return Result.success(response);
        } catch (Exception e) {
            log.error("更新八字信息失败", e);
            return Result.error("更新八字信息失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取八字信息列表
     */
    @Operation(summary = "获取八字信息列表", description = "获取用户的所有八字信息")
    @RequireAuth
    @GetMapping("/info/list")
    public Result<List<UserBaziInfoResponse>> getBaziInfoList(HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            log.info("获取八字信息列表，用户ID：{}", userId);
            
            List<UserBaziInfo> list = baziInfoService.getUserBaziInfoList(userId);
            List<UserBaziInfoResponse> responseList = list.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
            
            return Result.success(responseList);
        } catch (Exception e) {
            log.error("获取八字信息列表失败", e);
            return Result.error("获取八字信息列表失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取默认八字信息
     */
    @Operation(summary = "获取默认八字信息", description = "获取用户的默认八字信息")
    @RequireAuth
    @GetMapping("/info/default")
    public Result<UserBaziInfoResponse> getDefaultBaziInfo(HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            log.info("获取默认八字信息，用户ID：{}", userId);
            
            UserBaziInfo baziInfo = baziInfoService.getDefaultBaziInfo(userId);
            if (baziInfo == null) {
                return Result.error("未找到默认八字信息");
            }
            
            UserBaziInfoResponse response = convertToResponse(baziInfo);
            return Result.success(response);
        } catch (Exception e) {
            log.error("获取默认八字信息失败", e);
            return Result.error("获取默认八字信息失败：" + e.getMessage());
        }
    }
    
    /**
     * 设置默认八字
     */
    @Operation(summary = "设置默认八字", description = "将指定的八字信息设为默认")
    @RequireAuth
    @PutMapping("/info/{id}/default")
    public Result<Void> setDefaultBaziInfo(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            log.info("设置默认八字，用户ID：{}，八字ID：{}", userId, id);
            
            baziInfoService.setDefaultBaziInfo(userId, id);
            return Result.success();
        } catch (Exception e) {
            log.error("设置默认八字失败", e);
            return Result.error("设置默认八字失败：" + e.getMessage());
        }
    }
    
    /**
     * 删除八字信息
     */
    @Operation(summary = "删除八字信息", description = "删除指定的八字信息")
    @RequireAuth
    @DeleteMapping("/info/{id}")
    public Result<Void> deleteBaziInfo(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            log.info("删除八字信息，用户ID：{}，八字ID：{}", userId, id);
            
            baziInfoService.deleteBaziInfo(userId, id);
            return Result.success();
        } catch (Exception e) {
            log.error("删除八字信息失败", e);
            return Result.error("删除八字信息失败：" + e.getMessage());
        }
    }
    
    /**
     * 转换为响应DTO
     */
    private UserBaziInfoResponse convertToResponse(UserBaziInfo baziInfo) {
        return UserBaziInfoResponse.builder()
                .id(baziInfo.getId())
                .name(baziInfo.getName())
                .gender(baziInfo.getGender())
                .genderDesc(baziInfo.getGender() == 1 ? "男" : "女")
                .birthYear(baziInfo.getBirthYear())
                .birthMonth(baziInfo.getBirthMonth())
                .birthDay(baziInfo.getBirthDay())
                .birthHour(baziInfo.getBirthHour())
                .birthMinute(baziInfo.getBirthMinute())
                .isLunar(baziInfo.getIsLunar())
                .calendarDesc(baziInfo.getIsLunar() == 1 ? "农历" : "公历")
                .timezone(baziInfo.getTimezone())
                .birthplace(baziInfo.getBirthplace())
                .baziData(baziInfo.getBaziData())
                .isDefault(baziInfo.getIsDefault())
                .createTime(baziInfo.getCreateTime())
                .updateTime(baziInfo.getUpdateTime())
                .build();
    }
}

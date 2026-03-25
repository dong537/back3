package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.entity.TbContactRecord;
import com.example.demo.service.ContactRecordService;
import com.example.demo.util.AuthUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 联系方式记录控制器
 */
@RestController
@RequestMapping("/api/contact-record")
@RequiredArgsConstructor
@Slf4j
public class ContactRecordController {

    private final ContactRecordService contactRecordService;
    private final AuthUtil authUtil;

    /**
     * 记录联系方式查看/点击
     */
    @PostMapping
    public Result<TbContactRecord> recordContact(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestBody Map<String, Object> request,
            ServerHttpRequest serverRequest) {
        
        Long userId = authUtil.tryGetUserId(token); // 允许未登录用户记录
        
        String contactType = (String) request.getOrDefault("contactType", "wechat");
        String contactName = (String) request.getOrDefault("contactName", "李钧泽");
        String contactInfo = (String) request.getOrDefault("contactInfo", "");
        String sourcePage = (String) request.getOrDefault("sourcePage", "");
        String sourceType = (String) request.getOrDefault("sourceType", "divination_result");
        String actionType = (String) request.getOrDefault("actionType", "view");
        Long relatedRecordId = request.get("relatedRecordId") != null ? 
                Long.valueOf(request.get("relatedRecordId").toString()) : null;
        
        // 获取IP地址和User-Agent
        String ipAddress = getClientIpAddress(serverRequest);
        String userAgent = serverRequest.getHeaders().getFirst("User-Agent");
        
        TbContactRecord record = TbContactRecord.builder()
                .userId(userId)
                .contactType(contactType)
                .contactName(contactName)
                .contactInfo(contactInfo)
                .sourcePage(sourcePage)
                .sourceType(sourceType)
                .relatedRecordId(relatedRecordId)
                .actionType(actionType)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();
        
        TbContactRecord saved = contactRecordService.saveRecord(record);
        log.info("记录联系方式: userId={}, contactType={}, actionType={}, sourcePage={}", 
                userId, contactType, actionType, sourcePage);
        
        return Result.success(saved);
    }

    /**
     * 获取当前用户的联系记录
     */
    @GetMapping
    public Result<List<TbContactRecord>> getUserRecords(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = authUtil.requireUserId(token);
        List<TbContactRecord> records = contactRecordService.getUserRecordsPaged(userId, page, size);
        return Result.success(records);
    }

    /**
     * 获取用户联系记录统计
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats(
            @RequestHeader(value = "Authorization", required = false) String token) {
        Long userId = authUtil.requireUserId(token);
        Map<String, Object> stats = contactRecordService.getUserContactStats(userId);
        return Result.success(stats);
    }

    /**
     * 获取客户端IP地址（WebFlux版本）
     */
    private String getClientIpAddress(ServerHttpRequest request) {
        String ip = request.getHeaders().getFirst("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeaders().getFirst("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeaders().getFirst("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeaders().getFirst("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeaders().getFirst("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            // WebFlux中获取远程地址
            try {
                var remoteAddress = request.getRemoteAddress();
                if (remoteAddress != null && remoteAddress.getAddress() != null) {
                    ip = remoteAddress.getAddress().getHostAddress();
                }
            } catch (Exception e) {
                // 忽略获取IP失败的情况
                log.debug("获取远程地址失败", e);
            }
        }
        // 处理多个IP的情况，取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip != null ? ip : "unknown";
    }
}

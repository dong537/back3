package com.example.demo.controller;

import com.example.demo.annotation.RateLimit;
import com.example.demo.dto.request.auth.PhoneLoginRequest;
import com.example.demo.dto.request.auth.SendSmsRequest;
import com.example.demo.dto.response.Result;
import com.example.demo.entity.User;
import com.example.demo.mapper.UserMapper;
import com.example.demo.service.SmsService;
import com.example.demo.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器
 */
@Slf4j
@Tag(name = "用户认证", description = "登录、注册、短信验证码等认证相关功能")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final SmsService smsService;
    private final JwtUtil jwtUtil;
    private final UserMapper userMapper;
    
    /**
     * 发送短信验证码
     */
    @Operation(summary = "发送短信验证码", description = "发送6位数字验证码到指定手机号")
    @RateLimit(timeWindow = 60, maxCount = 1, limitType = RateLimit.LimitType.IP)
    @PostMapping("/sms/send")
    public Result<Map<String, Object>> sendSms(@Validated @RequestBody SendSmsRequest request) {
        try {
            log.info("发送短信验证码，手机号：{}", request.getPhone());
            
            smsService.sendVerificationCode(request.getPhone());
            
            Map<String, Object> data = new HashMap<>();
            data.put("phone", request.getPhone());
            data.put("expireSeconds", 300); // 5分钟有效期
            
            return Result.success(data);
        } catch (Exception e) {
            log.error("发送短信验证码失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 手机号验证码登录/注册
     */
    @Operation(summary = "手机号登录", description = "使用手机号和验证码登录，如果用户不存在则自动注册")
    @RateLimit(timeWindow = 60, maxCount = 5, limitType = RateLimit.LimitType.IP)
    @PostMapping("/phone/login")
    public Result<Map<String, Object>> phoneLogin(@Validated @RequestBody PhoneLoginRequest request) {
        try {
            log.info("手机号登录，手机号：{}", request.getPhone());
            
            // 1. 验证验证码
            if (!smsService.verifyCode(request.getPhone(), request.getCode())) {
                return Result.error("验证码错误或已过期");
            }
            
            // 2. 查找或创建用户
            User user = userMapper.findByPhone(request.getPhone());
            boolean isNewUser = false;
            
            if (user == null) {
                // 自动注册
                user = new User();
                user.setPhone(request.getPhone());
                user.setUsername("用户" + request.getPhone().substring(7)); // 使用后4位作为用户名
                user.setNickname("用户" + request.getPhone().substring(7));
                // 生成随机密码（避免SQL无密码报错）
                user.setPassword(generateRandomPassword());
                user.setStatus(1);
                user.setCreateTime(LocalDateTime.now());
                user.setUpdateTime(LocalDateTime.now());
                
                userMapper.insert(user);
                isNewUser = true;
                log.info("自动注册新用户，手机号：{}，用户ID：{}", request.getPhone(), user.getId());
            }
            
            // 3. 更新最后登录信息
            user.setLastLoginTime(LocalDateTime.now());
            userMapper.updateUser(user);
            
            // 4. 生成Token
            String accessToken = jwtUtil.generateToken(user.getId(), user.getUsername());
            String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getUsername());
            
            // 5. 返回结果
            Map<String, Object> data = new HashMap<>();
            data.put("accessToken", accessToken);
            data.put("refreshToken", refreshToken);
            data.put("tokenType", "Bearer");
            data.put("expiresIn", 86400); // 24小时
            data.put("user", Map.of(
                    "id", user.getId(),
                    "username", user.getUsername(),
                    "nickname", user.getNickname(),
                    "phone", user.getPhone(),
                    "isNewUser", isNewUser
            ));
            
            log.info("登录成功，用户ID：{}，是否新用户：{}", user.getId(), isNewUser);
            return Result.success(data);
            
        } catch (Exception e) {
            log.error("手机号登录失败", e);
            return Result.error("登录失败：" + e.getMessage());
        }
    }
    
    /**
     * 刷新Token
     */
    @Operation(summary = "刷新Token", description = "使用RefreshToken获取新的AccessToken")
    @PostMapping("/token/refresh")
    public Result<Map<String, Object>> refreshToken(@RequestHeader("Authorization") String authorization) {
        try {
            // 提取token
            String refreshToken = authorization.replace("Bearer ", "");
            
            // 验证token
            if (!jwtUtil.validateToken(refreshToken)) {
                return Result.error("RefreshToken无效或已过期");
            }
            
            // 生成新的AccessToken
            String newAccessToken = jwtUtil.refreshToken(refreshToken);
            
            Map<String, Object> data = new HashMap<>();
            data.put("accessToken", newAccessToken);
            data.put("tokenType", "Bearer");
            data.put("expiresIn", 86400);
            
            return Result.success(data);
        } catch (Exception e) {
            log.error("刷新Token失败", e);
            return Result.error("刷新Token失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取验证码剩余时间
     */
    @Operation(summary = "获取验证码剩余时间", description = "查询验证码还有多久过期")
    @GetMapping("/sms/ttl/{phone}")
    public Result<Map<String, Object>> getCodeTTL(@PathVariable String phone) {
        try {
            Long ttl = smsService.getCodeTTL(phone);
            
            Map<String, Object> data = new HashMap<>();
            data.put("phone", phone);
            data.put("ttl", ttl);
            data.put("expired", ttl <= 0);
            
            return Result.success(data);
        } catch (Exception e) {
            log.error("获取验证码剩余时间失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 生成随机密码（用于手机号自动注册）
     */
    private String generateRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder(16);
        for (int i = 0; i < 16; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        return password.toString();
    }
}

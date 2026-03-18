package com.example.demo.service;

import com.example.demo.dto.request.user.LoginRequest;
import com.example.demo.dto.request.user.RegisterRequest;
import com.example.demo.entity.User;
import com.example.demo.mapper.UserMapper;
import com.example.demo.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户服务类
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
    
    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final CreditService creditService;
    
    /**
     * 用户注册
     */
    public Map<String, Object> register(RegisterRequest request) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 1. 检查用户名是否已存在
            User existUser = userMapper.findByUsername(request.getUsername());
            if (existUser != null) {
                result.put("success", false);
                result.put("message", "用户名已存在");
                return result;
            }
            
            // 2. 检查邮箱是否已存在
            if (request.getEmail() != null && !request.getEmail().isEmpty()) {
                User emailUser = userMapper.findByEmail(request.getEmail());
                if (emailUser != null) {
                    result.put("success", false);
                    result.put("message", "邮箱已被注册");
                    return result;
                }
            }
            
            // 3. 检查手机号是否已存在
            if (request.getPhone() != null && !request.getPhone().isEmpty()) {
                User phoneUser = userMapper.findByPhone(request.getPhone());
                if (phoneUser != null) {
                    result.put("success", false);
                    result.put("message", "手机号已被注册");
                    return result;
                }
            }
            
            // 4. 创建新用户
            User user = new User();
            user.setUsername(request.getUsername());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setEmail(request.getEmail());
            user.setPhone(request.getPhone());
            user.setNickname(request.getUsername()); // 默认昵称为用户名
            user.setStatus(1); // 正常状态
            
            // 5. 保存到数据库
            int rows = userMapper.insert(user);
            if (rows > 0) {
                log.info("用户注册成功: username={}, id={}", user.getUsername(), user.getId());
                result.put("userId", user.getId());
                result.put("username", user.getUsername());
            } else {
                result.put("success", false);
                result.put("message", "注册失败，请稍后重试");
            }
            
        } catch (Exception e) {
            log.error("用户注册失败", e);
            result.put("success", false);
            result.put("message", "系统错误：" + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 用户登录
     */
    public Map<String, Object> login(LoginRequest request, String ip) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 1. 查询用户
            User user = userMapper.findByUsername(request.getUsername());
            if (user == null) {
                result.put("success", false);
                result.put("message", "用户名或密码错误");
                return result;
            }
            
            // 2. 检查用户状态
            if (user.getStatus() == 0) {
                result.put("success", false);
                result.put("message", "账号已被禁用，请联系管理员");
                return result;
            }
            
            // 3. 验证密码
            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                result.put("success", false);
                result.put("message", "用户名或密码错误");
                return result;
            }
            
            // 4. 生成JWT token
            String token = jwtUtil.generateToken(user.getId(), user.getUsername());
            
            // 5. 更新最后登录信息
            user.setLastLoginTime(LocalDateTime.now());
            user.setLastLoginIp(ip);
            userMapper.updateLastLogin(user);
            
            // 6. 返回结果（直接返回token和user，不包装在success/message中）
            log.info("用户登录成功: username={}, ip={}", user.getUsername(), ip);
            result.put("token", token);
            result.put("user", buildUserVO(user));
            
        } catch (Exception e) {
            log.error("用户登录失败", e);
            result.put("success", false);
            result.put("message", "系统错误：" + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 根据token获取用户信息
     */
    public Map<String, Object> getUserInfo(String token) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 使用JWT工具验证和解析token
            if (token == null || !jwtUtil.validateToken(token)) {
                result.put("success", false);
                result.put("message", "token无效");
                return result;
            }
            
            Long userId = jwtUtil.extractUserId(token);
            User user = userMapper.findById(userId);
            if (user == null) {
                result.put("success", false);
                result.put("message", "用户不存在");
                return result;
            }
            
            // 直接返回user对象，不包装在success/message中
            result.put("user", buildUserVO(user));
            
        } catch (Exception e) {
            log.error("获取用户信息失败", e);
            result.put("success", false);
            result.put("message", "系统错误: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 构建用户视图对象（不包含密码等敏感信息）
     */
    private Map<String, Object> buildUserVO(User user) {
        if (user == null) {
            return new HashMap<>();
        }
        Map<String, Object> userVO = new HashMap<>();
        userVO.put("id", user.getId());
        userVO.put("username", user.getUsername() != null ? user.getUsername() : "");
        userVO.put("email", user.getEmail() != null ? user.getEmail() : "");
        userVO.put("phone", user.getPhone() != null ? user.getPhone() : "");
        userVO.put("nickname", user.getNickname() != null ? user.getNickname() : "");
        userVO.put("avatar", user.getAvatar() != null ? user.getAvatar() : "");
        // 统一从CreditService获取积分，确保数据一致性
        Integer currentPoints = creditService.getCurrentPoints(user.getId());
        userVO.put("currentPoints", currentPoints != null ? currentPoints : 0);
        userVO.put("totalPoints", user.getTotalPoints() != null ? user.getTotalPoints() : 0);
        userVO.put("createTime", user.getCreateTime() != null ? user.getCreateTime().toString() : null);
        return userVO;
    }
}

package com.example.demo.service;

import com.example.demo.dto.request.user.LoginRequest;
import com.example.demo.dto.request.user.RegisterRequest;
import com.example.demo.dto.response.Result;
import com.example.demo.entity.User;
import com.example.demo.mapper.UserMapper;
import com.example.demo.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    /**
     * 用户注册
     */
    public Result<Map<String, Object>> register(RegisterRequest request) {
        try {
            // 1. 检查用户名是否已存在
            User existUser = userMapper.findByUsername(request.getUsername());
            if (existUser != null) {
                return Result.badRequest("用户名已存在");
            }
            
            // 2. 检查邮箱是否已存在
            if (request.getEmail() != null && !request.getEmail().isEmpty()) {
                User emailUser = userMapper.findByEmail(request.getEmail());
                if (emailUser != null) {
                    return Result.badRequest("邮箱已被注册");
                }
            }
            
            // 3. 检查手机号是否已存在
            if (request.getPhone() != null && !request.getPhone().isEmpty()) {
                User phoneUser = userMapper.findByPhone(request.getPhone());
                if (phoneUser != null) {
                    return Result.badRequest("手机号已被注册");
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
                Map<String, Object> data = new HashMap<>();
                data.put("userId", user.getId());
                data.put("username", user.getUsername());
                return Result.success("注册成功", data);
            } else {
                return Result.error("注册失败，请稍后重试");
            }
            
        } catch (Exception e) {
            log.error("用户注册失败", e);
            return Result.error("系统错误，请稍后重试");
        }
    }
    
    /**
     * 用户登录
     */
    public Result<Map<String, Object>> login(LoginRequest request, String ip) {
        try {
            // 1. 查询用户
            User user = userMapper.findByUsername(request.getUsername());
            if (user == null) {
                return Result.badRequest("用户名或密码错误");
            }
            
            // 2. 检查用户状态
            if (user.getStatus() == 0) {
                return Result.forbidden("账号已被禁用，请联系管理员");
            }
            
            // 3. 验证密码
            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                return Result.badRequest("用户名或密码错误");
            }
            
            // 4. 生成token
            String accessToken = jwtUtil.generateToken(user.getId(), user.getUsername());
            String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getUsername());
            
            // 5. 更新最后登录信息
            user.setLastLoginTime(LocalDateTime.now());
            user.setLastLoginIp(ip);
            userMapper.updateLastLogin(user);
            
            // 6. 返回结果
            log.info("用户登录成功: username={}, ip={}", user.getUsername(), ip);
            Map<String, Object> data = new HashMap<>();
            data.put("accessToken", accessToken);
            data.put("refreshToken", refreshToken);
            data.put("tokenType", "Bearer");
            data.put("expiresIn", 86400);  // 24小时
            data.put("id", user.getId());
            data.put("username", user.getUsername());
            data.put("name", user.getNickname());
            return Result.success("登录成功", data);
            
        } catch (Exception e) {
            log.error("用户登录失败", e);
            return Result.error("系统错误，请稍后重试");
        }
    }
    
    /**
     * 根据userId获取用户信息
     */
    public Result<Map<String, Object>> getUserInfoById(Long userId) {
        try {
            if (userId == null) {
                return Result.badRequest("用户ID不能为空");
            }
            
            User user = userMapper.findById(userId);
            if (user == null) {
                return Result.badRequest("用户不存在");
            }
            
            return Result.success(buildUserVO(user));
            
        } catch (Exception e) {
            log.error("获取用户信息失败", e);
            return Result.error("系统错误，请稍后重试");
        }
    }
    
    
    /**
     * 解析token获取userId
     */
    private Long parseToken(String token) {
        try {
            return jwtUtil.getUserIdFromToken(token);
        } catch (Exception e) {
            log.warn("解析token失败: {}", token);
            return null;
        }
    }
    
    /**
     * 构建用户视图对象（不包含密码等敏感信息）
     */
    private Map<String, Object> buildUserVO(User user) {
        Map<String, Object> userVO = new HashMap<>();
        userVO.put("id", user.getId());
        userVO.put("username", user.getUsername());
        userVO.put("email", user.getEmail());
        userVO.put("phone", user.getPhone());
        userVO.put("nickname", user.getNickname());
        userVO.put("avatar", user.getAvatar());
        if (user.getCreateTime() != null) {
            userVO.put("createTime", user.getCreateTime().toString());
        } else {
            userVO.put("createTime", null);
        }
        return userVO;
    }
}

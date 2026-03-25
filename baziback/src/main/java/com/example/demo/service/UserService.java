package com.example.demo.service;

import com.example.demo.dto.request.user.LoginRequest;
import com.example.demo.dto.request.user.RegisterRequest;
import com.example.demo.entity.User;
import com.example.demo.exception.BusinessException;
import com.example.demo.exception.UnauthorizedException;
import com.example.demo.mapper.UserMapper;
import com.example.demo.util.I18nHelper;
import com.example.demo.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final CreditService creditService;

    public Map<String, Object> register(RegisterRequest request) {
        Map<String, Object> result = new HashMap<>();

        User existingUser = userMapper.findByUsername(request.getUsername());
        if (existingUser != null) {
            throw new BusinessException(I18nHelper.message("user.username.exists", "用户名已存在"));
        }

        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            User existingEmailUser = userMapper.findByEmail(request.getEmail());
            if (existingEmailUser != null) {
                throw new BusinessException(I18nHelper.message("user.email.exists", "邮箱已被注册"));
            }
        }

        if (request.getPhone() != null && !request.getPhone().isEmpty()) {
            User existingPhoneUser = userMapper.findByPhone(request.getPhone());
            if (existingPhoneUser != null) {
                throw new BusinessException(I18nHelper.message("user.phone.exists", "手机号已被注册"));
            }
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setNickname(request.getUsername());
        user.setStatus(1);

        int rows = userMapper.insert(user);
        if (rows <= 0) {
            throw new BusinessException(I18nHelper.message("user.register.failed", "注册失败，请稍后重试"));
        }

        log.info("User registered successfully: username={}, id={}", user.getUsername(), user.getId());
        result.put("userId", user.getId());
        result.put("username", user.getUsername());
        return result;
    }

    public Map<String, Object> login(LoginRequest request, String ip) {
        Map<String, Object> result = new HashMap<>();

        User user = userMapper.findByUsername(request.getUsername());
        if (user == null) {
            throw new UnauthorizedException(I18nHelper.message("auth.invalid.credentials", "用户名或密码错误"));
        }

        if (user.getStatus() == 0) {
            throw new UnauthorizedException(I18nHelper.message("auth.account.disabled", "账号已被禁用，请联系管理员"));
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException(I18nHelper.message("auth.invalid.credentials", "用户名或密码错误"));
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        user.setLastLoginTime(LocalDateTime.now());
        user.setLastLoginIp(ip);
        userMapper.updateLastLogin(user);

        log.info("User logged in successfully: username={}, ip={}", user.getUsername(), ip);
        result.put("token", token);
        result.put("user", buildUserVO(user));
        return result;
    }

    public Map<String, Object> getUserInfo(Long userId) {
        Map<String, Object> result = new HashMap<>();

        User user = userMapper.findById(userId);
        if (user == null) {
            throw new UnauthorizedException(I18nHelper.message("auth.user.not_found", "用户不存在"));
        }

        result.put("user", buildUserVO(user));
        return result;
    }

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

        Integer currentPoints = creditService.getCurrentPoints(user.getId());
        userVO.put("currentPoints", currentPoints != null ? currentPoints : 0);
        userVO.put("totalPoints", user.getTotalPoints() != null ? user.getTotalPoints() : 0);
        userVO.put("createTime", user.getCreateTime() != null ? user.getCreateTime().toString() : null);
        return userVO;
    }
}

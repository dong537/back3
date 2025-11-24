package com.example.demo.service;

import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.aliyun.teaopenapi.models.Config;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 短信验证码服务
 */
@Slf4j
@Service
public class SmsService {
    
    @Value("${aliyun.sms.access-key-id:}")
    private String accessKeyId;
    
    @Value("${aliyun.sms.access-key-secret:}")
    private String accessKeySecret;
    
    @Value("${aliyun.sms.sign-name:参天AI}")
    private String signName;
    
    @Value("${aliyun.sms.template-code:}")
    private String templateCode;
    
    @Value("${sms.code.expire:300}") // 验证码有效期，默认5分钟
    private Integer codeExpire;
    
    @Value("${sms.code.length:6}") // 验证码长度，默认6位
    private Integer codeLength;
    
    private final StringRedisTemplate redisTemplate;
    
    public SmsService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    
    /**
     * 发送验证码
     */
    public boolean sendVerificationCode(String phone) {
        try {
            // 1. 检查发送频率（60秒内只能发送一次）
            String rateLimitKey = "sms:rate:" + phone;
            if (Boolean.TRUE.equals(redisTemplate.hasKey(rateLimitKey))) {
                log.warn("发送验证码过于频繁，手机号：{}", phone);
                throw new RuntimeException("发送过于频繁，请稍后再试");
            }
            
            // 2. 生成验证码
            String code = generateCode();
            log.info("生成验证码，手机号：{}，验证码：{}", phone, code);
            
            // 3. 发送短信（如果配置了阿里云）
            if (accessKeyId != null && !accessKeyId.isEmpty()) {
                sendSms(phone, code);
            } else {
                log.warn("未配置阿里云短信服务，验证码：{}", code);
            }
            
            // 4. 存储验证码到Redis
            String codeKey = "sms:code:" + phone;
            redisTemplate.opsForValue().set(codeKey, code, codeExpire, TimeUnit.SECONDS);
            
            // 5. 设置发送频率限制（60秒）
            redisTemplate.opsForValue().set(rateLimitKey, "1", 60, TimeUnit.SECONDS);
            
            log.info("验证码发送成功，手机号：{}", phone);
            return true;
        } catch (Exception e) {
            log.error("发送验证码失败，手机号：{}", phone, e);
            throw new RuntimeException("发送验证码失败：" + e.getMessage());
        }
    }
    
    /**
     * 验证验证码
     */
    public boolean verifyCode(String phone, String code) {
        String codeKey = "sms:code:" + phone;
        String storedCode = redisTemplate.opsForValue().get(codeKey);
        
        if (storedCode == null) {
            log.warn("验证码不存在或已过期，手机号：{}", phone);
            return false;
        }
        
        boolean valid = storedCode.equals(code);
        if (valid) {
            // 验证成功后删除验证码
            redisTemplate.delete(codeKey);
            log.info("验证码验证成功，手机号：{}", phone);
        } else {
            log.warn("验证码错误，手机号：{}，输入：{}，正确：{}", phone, code, storedCode);
        }
        
        return valid;
    }
    
    /**
     * 生成随机验证码
     */
    private String generateCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < codeLength; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }
    
    /**
     * 调用阿里云短信API发送短信
     */
    private void sendSms(String phone, String code) throws Exception {
        Config config = new Config()
                .setAccessKeyId(accessKeyId)
                .setAccessKeySecret(accessKeySecret)
                .setEndpoint("dysmsapi.aliyuncs.com");
        
        Client client = new Client(config);
        
        SendSmsRequest request = new SendSmsRequest()
                .setPhoneNumbers(phone)
                .setSignName(signName)
                .setTemplateCode(templateCode)
                .setTemplateParam("{\"code\":\"" + code + "\"}");
        
        SendSmsResponse response = client.sendSms(request);
        
        if (!"OK".equals(response.getBody().getCode())) {
            log.error("阿里云短信发送失败，Code：{}，Message：{}", 
                    response.getBody().getCode(), response.getBody().getMessage());
            throw new RuntimeException("短信发送失败");
        }
        
        log.info("阿里云短信发送成功，RequestId：{}", response.getBody().getRequestId());
    }
    
    /**
     * 获取验证码剩余有效时间（秒）
     */
    public Long getCodeTTL(String phone) {
        String codeKey = "sms:code:" + phone;
        return redisTemplate.getExpire(codeKey, TimeUnit.SECONDS);
    }
}

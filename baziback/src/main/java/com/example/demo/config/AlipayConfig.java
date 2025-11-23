package com.example.demo.config;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 支付宝配置类
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "alipay")
public class AlipayConfig {

    /**
     * 应用ID
     */
    private String appId;

    /**
     * 应用私钥
     */
    private String privateKey;

    /**
     * 支付宝公钥
     */
    private String alipayPublicKey;

    /**
     * 签名类型
     */
    private String signType = "RSA2";

    /**
     * 字符编码（支付宝推荐使用GBK）
     */
    private String charset = "GBK";

    /**
     * 数据格式
     */
    private String format = "json";

    /**
     * 是否沙箱环境
     */
    private Boolean sandbox = false;

    /**
     * 支付成功后的前端回调地址
     */
    private String returnUrl;

    /**
     * 支付宝异步通知地址
     */
    private String notifyUrl;

    /**
     * 支付宝网关地址
     */
    private static final String GATEWAY_URL = "https://openapi.alipay.com/gateway.do";
    private static final String SANDBOX_GATEWAY_URL = "https://openapi-sandbox.dl.alipaydev.com/gateway.do";

    /**
     * 创建AlipayClient实例
     */
    @Bean
    public AlipayClient alipayClient() {
        String gatewayUrl = sandbox ? SANDBOX_GATEWAY_URL : GATEWAY_URL;
        
        // 打印配置信息用于调试
        System.out.println("=== 支付宝配置信息 ===");
        System.out.println("Gateway URL: " + gatewayUrl);
        System.out.println("App ID: " + appId);
        System.out.println("Format: " + format);
        System.out.println("Charset: " + charset);
        System.out.println("Sign Type: " + signType);
        System.out.println("====================");
        
        return new DefaultAlipayClient(
                gatewayUrl,      // 网关地址
                appId,           // 应用ID
                privateKey,      // 应用私钥
                format,          // 格式：json
                charset,         // 字符编码：GBK
                alipayPublicKey, // 支付宝公钥
                signType         // 签名类型：RSA2
        );
    }
}

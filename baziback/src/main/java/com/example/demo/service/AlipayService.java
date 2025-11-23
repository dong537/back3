package com.example.demo.service;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.domain.AlipayTradePagePayModel;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.example.demo.config.AlipayConfig;
import com.example.demo.entity.Order;
import com.example.demo.entity.PaymentLog;
import com.example.demo.mapper.PaymentLogMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 支付宝支付服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AlipayService {

    private final AlipayClient alipayClient;
    private final AlipayConfig alipayConfig;
    private final PaymentLogMapper paymentLogMapper;
    private final ObjectMapper objectMapper;

    /**
     * 创建支付表单
     * 参考官方文档：https://opendocs.alipay.com/open/028r8t
     */
    public String createPayForm(Order order) throws AlipayApiException {
        // 创建API对应的request类
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        
        // 设置同步回调地址（支付完成后跳转的页面）
        request.setReturnUrl(alipayConfig.getReturnUrl());
        
        // 设置异步通知地址（支付宝服务器主动通知商户服务器）
        request.setNotifyUrl(alipayConfig.getNotifyUrl());

        // 创建业务模型
        AlipayTradePagePayModel model = new AlipayTradePagePayModel();
        
        // 商户订单号（必填）- 商户网站唯一订单号
        model.setOutTradeNo(order.getOrderNo());
        
        // 订单总金额（必填）- 单位为元，精确到小数点后两位
        model.setTotalAmount(order.getAmount().toString());
        
        // 订单标题（必填）- 注意：不可使用特殊字符，如 /，=，& 等
        model.setSubject(order.getProductName());
        
        // 产品码（必填）- 电脑网站支付固定值
        model.setProductCode("FAST_INSTANT_TRADE_PAY");
        
        // 订单描述（可选）
        if (order.getProductDesc() != null && !order.getProductDesc().isEmpty()) {
            model.setBody(order.getProductDesc());
        }
        
        // 超时时间（可选）- 该笔订单允许的最晚付款时间，逾期将关闭交易
        // 取值范围：1m～15d。m-分钟，h-小时，d-天
        model.setTimeoutExpress("30m");

        // 将业务模型设置到request中
        request.setBizModel(model);
        
        log.info("创建支付请求 - 订单号: {}, 金额: {}, 商品: {}", 
            order.getOrderNo(), order.getAmount(), order.getProductName());
        log.info("业务参数 - OutTradeNo: {}, TotalAmount: {}, Subject: {}, ProductCode: {}", 
            model.getOutTradeNo(), model.getTotalAmount(), model.getSubject(), model.getProductCode());

        try {
            // 调用SDK生成表单，并直接将完整的form html输出到页面
            AlipayTradePagePayResponse response = alipayClient.pageExecute(request);
            
            if (response.isSuccess()) {
                log.info("创建支付表单成功，订单号：{}", order.getOrderNo());
                return response.getBody();
            } else {
                String errorMsg = String.format("创建支付表单失败 - code: %s, msg: %s, subCode: %s, subMsg: %s", 
                    response.getCode(), response.getMsg(), response.getSubCode(), response.getSubMsg());
                log.error(errorMsg);
                throw new AlipayApiException(errorMsg);
            }
        } catch (AlipayApiException e) {
            log.error("调用支付宝API异常，订单号：{}", order.getOrderNo(), e);
            throw e;
        }
    }

    /**
     * 将支付宝返回的表单转换为可直接跳转的URL，便于前端直接打开。
     * 如果解析失败则返回null，不影响原有表单方式。
     */
    public String buildPayUrlFromForm(String payFormHtml) {
        if (payFormHtml == null || payFormHtml.isBlank()) {
            return null;
        }

        try {
            String charsetName = alipayConfig.getCharset();
            Charset charset = charsetName != null ? Charset.forName(charsetName) : StandardCharsets.UTF_8;

            // 解析form action
            Matcher actionMatcher = Pattern.compile("<form[^>]*action\\s*=\\s*\\\"(.*?)\\\"", Pattern.CASE_INSENSITIVE)
                    .matcher(payFormHtml);
            if (!actionMatcher.find()) {
                log.warn("支付表单中未找到action，无法生成跳转链接");
                return null;
            }
            String action = actionMatcher.group(1);

            // 解析所有隐藏域参数
            Matcher inputMatcher = Pattern.compile("<input[^>]*name\\s*=\\s*\\\"(.*?)\\\"[^>]*value\\s*=\\s*\\\"(.*?)\\\"[^>]*>", Pattern.CASE_INSENSITIVE)
                    .matcher(payFormHtml);
            List<String> params = new ArrayList<>();
            while (inputMatcher.find()) {
                String name = inputMatcher.group(1);
                String value = inputMatcher.group(2);
                if (name == null || name.isBlank()) {
                    continue;
                }
                // 解除HTML/转义字符，避免biz_content等参数被双重转义
                String normalizedValue = value
                        .replace("&quot;", "\"")
                        .replace("\\\"", "\"")
                        .replace("&amp;", "&");

                params.add(name + "=" + URLEncoder.encode(normalizedValue, charset));
            }

            if (params.isEmpty()) {
                log.warn("支付表单中未找到参数，无法生成跳转链接");
                return null;
            }

            String separator = action.contains("?") ? "&" : "?";
            return action + separator + String.join("&", params);
        } catch (Exception e) {
            // 出现异常不影响原有表单使用
            log.warn("解析支付表单生成跳转链接失败", e);
            return null;
        }
    }

    /**
     * 验证支付宝异步通知签名
     */
    public boolean verifyNotify(Map<String, String> params) {
        try {
            return AlipaySignature.rsaCheckV1(
                    params,
                    alipayConfig.getAlipayPublicKey(),
                    alipayConfig.getCharset(),
                    alipayConfig.getSignType()
            );
        } catch (AlipayApiException e) {
            log.error("验证支付宝签名失败", e);
            return false;
        }
    }

    /**
     * 记录支付日志
     */
    public void logPaymentNotify(String orderNo, String tradeNo, String notifyType, Map<String, String> params) {
        try {
            PaymentLog log = PaymentLog.builder()
                    .orderNo(orderNo)
                    .tradeNo(tradeNo)
                    .notifyType(notifyType)
                    .notifyData(objectMapper.writeValueAsString(params))
                    .notifyTime(LocalDateTime.now())
                    .build();
            paymentLogMapper.insert(log);
        } catch (Exception e) {
            log.error("记录支付日志失败", e);
        }
    }

    /**
     * 查询订单支付状态
     */
    public AlipayTradeQueryResponse queryOrderStatus(String orderNo) throws AlipayApiException {
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        request.setBizContent("{\"out_trade_no\":\"" + orderNo + "\"}");
        return alipayClient.execute(request);
    }
}

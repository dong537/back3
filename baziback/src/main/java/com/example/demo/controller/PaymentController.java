package com.example.demo.controller;

import com.alipay.api.AlipayApiException;
import com.example.demo.annotation.RequireAuth;
import com.example.demo.dto.request.payment.CreateOrderRequest;
import com.example.demo.dto.response.Result;
import com.example.demo.dto.response.payment.CreateOrderResponse;
import com.example.demo.dto.response.payment.MembershipInfoResponse;
import com.example.demo.dto.response.payment.MembershipPackageResponse;
import com.example.demo.dto.response.payment.OrderDetailResponse;
import com.example.demo.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 支付控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * 创建订单并获取支付表单
     * POST /api/payment/create
     */
    @RequireAuth
    @PostMapping("/create")
    public Result<CreateOrderResponse> createOrder(@Validated @RequestBody CreateOrderRequest request,
                                                    HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            log.info("创建订单请求，用户ID：{}，订单类型：{}", userId, request.getOrderType());
            
            CreateOrderResponse response = paymentService.createOrderAndPay(userId, request);
            return Result.success(response);
        } catch (IllegalArgumentException e) {
            log.error("创建订单参数错误", e);
            return Result.badRequest(e.getMessage());
        } catch (AlipayApiException e) {
            log.error("创建支付表单失败", e);
            return Result.error("创建支付失败，请稍后重试");
        } catch (Exception e) {
            log.error("创建订单失败", e);
            return Result.error("创建订单失败：" + e.getMessage());
        }
    }

    /**
     * 支付宝异步通知回调
     * POST /api/payment/alipay/notify
     */
    @PostMapping("/alipay/notify")
    public String alipayNotify(HttpServletRequest request) {
        try {
            // 获取支付宝POST过来的所有参数
            Map<String, String> params = new HashMap<>();
            Map<String, String[]> requestParams = request.getParameterMap();
            
            for (String name : requestParams.keySet()) {
                String[] values = requestParams.get(name);
                String valueStr = String.join(",", values);
                params.put(name, valueStr);
            }

            log.info("收到支付宝异步通知，订单号：{}", params.get("out_trade_no"));

            // 处理通知
            boolean success = paymentService.handlePaymentNotify(params);
            
            if (success) {
                return "success";
            } else {
                return "failure";
            }
        } catch (Exception e) {
            log.error("处理支付宝异步通知失败", e);
            return "failure";
        }
    }

    /**
     * 支付成功前端回调页面
     * GET /api/payment/success
     */
    @GetMapping("/success")
    public Result<Map<String, String>> paymentSuccess(@RequestParam String out_trade_no) {
        log.info("支付成功回调，订单号：{}", out_trade_no);
        Map<String, String> data = new HashMap<>();
        data.put("orderNo", out_trade_no);
        data.put("message", "支付成功");
        return Result.success(data);
    }

    /**
     * 查询订单详情
     * GET /api/payment/order/{orderNo}
     */
    @RequireAuth
    @GetMapping("/order/{orderNo}")
    public Result<OrderDetailResponse> getOrderDetail(@PathVariable String orderNo,
                                                       HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            log.info("查询订单详情，用户ID：{}，订单号：{}", userId, orderNo);
            
            OrderDetailResponse response = paymentService.getOrderDetail(orderNo);
            if (response == null) {
                return Result.error("订单不存在");
            }
            
            return Result.success(response);
        } catch (Exception e) {
            log.error("查询订单详情失败", e);
            return Result.error("查询订单失败：" + e.getMessage());
        }
    }

    /**
     * 查询用户订单列表
     * GET /api/payment/orders
     */
    @RequireAuth
    @GetMapping("/orders")
    public Result<List<OrderDetailResponse>> getUserOrders(HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            log.info("查询用户订单列表，用户ID：{}", userId);
            
            List<OrderDetailResponse> orders = paymentService.getUserOrders(userId);
            return Result.success(orders);
        } catch (Exception e) {
            log.error("查询订单列表失败", e);
            return Result.error("查询订单列表失败：" + e.getMessage());
        }
    }

    /**
     * 查询用户会员信息
     * GET /api/payment/membership/info
     */
    @RequireAuth
    @GetMapping("/membership/info")
    public Result<MembershipInfoResponse> getMembershipInfo(HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            log.info("查询用户会员信息，用户ID：{}", userId);
            
            MembershipInfoResponse response = paymentService.getUserMembershipInfo(userId);
            return Result.success(response);
        } catch (Exception e) {
            log.error("查询会员信息失败", e);
            return Result.error("查询会员信息失败：" + e.getMessage());
        }
    }

    /**
     * 查询所有会员套餐
     * GET /api/payment/membership/packages
     */
    @GetMapping("/membership/packages")
    public Result<List<MembershipPackageResponse>> getMembershipPackages() {
        try {
            log.info("查询会员套餐列表");
            List<MembershipPackageResponse> packages = paymentService.getAllMembershipPackages();
            return Result.success(packages);
        } catch (Exception e) {
            log.error("查询会员套餐失败", e);
            return Result.error("查询会员套餐失败：" + e.getMessage());
        }
    }
}

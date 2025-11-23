package com.example.demo.service;

import com.alipay.api.AlipayApiException;
import com.example.demo.dto.request.payment.CreateOrderRequest;
import com.example.demo.dto.response.payment.CreateOrderResponse;
import com.example.demo.dto.response.payment.MembershipInfoResponse;
import com.example.demo.dto.response.payment.MembershipPackageResponse;
import com.example.demo.dto.response.payment.OrderDetailResponse;
import com.example.demo.entity.Membership;
import com.example.demo.entity.MembershipPackage;
import com.example.demo.entity.Order;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 支付服务（整合订单、会员、支付宝）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final OrderService orderService;
    private final MembershipService membershipService;
    private final AlipayService alipayService;
    private final ObjectMapper objectMapper;

    /**
     * 创建订单并生成支付表单
     */
    @Transactional
    public CreateOrderResponse createOrderAndPay(Long userId, CreateOrderRequest request) throws AlipayApiException {
        Order order;
        
        if (request.getOrderType() == 1) {
            // 普通支付
            if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("订单金额必须大于0");
            }
            order = orderService.createOrder(
                    userId,
                    request.getOrderType(),
                    request.getProductName(),
                    request.getProductDesc(),
                    request.getAmount()
            );
        } else if (request.getOrderType() == 2) {
            // 会员购买
            if (request.getPackageId() == null) {
                throw new IllegalArgumentException("会员套餐ID不能为空");
            }
            MembershipPackage pkg = membershipService.getPackageById(request.getPackageId());
            if (pkg == null) {
                throw new IllegalArgumentException("会员套餐不存在");
            }
            if (pkg.getStatus() != 1) {
                throw new IllegalArgumentException("会员套餐已下架");
            }
            
            order = orderService.createOrder(
                    userId,
                    request.getOrderType(),
                    pkg.getPackageName(),
                    pkg.getDescription(),
                    pkg.getSalePrice()
            );
        } else {
            throw new IllegalArgumentException("不支持的订单类型");
        }

        // 生成支付表单
        String payForm = alipayService.createPayForm(order);
        // 构造可直接跳转的支付链接，方便前端通过window.open等方式拉起支付
        String payUrl = alipayService.buildPayUrlFromForm(payForm);

        return CreateOrderResponse.builder()
                .orderNo(order.getOrderNo())
                .payForm(payForm)
                .payUrl(payUrl)
                .expireMinutes(30)
                .build();
    }

    /**
     * 处理支付宝异步通知
     */
    @Transactional
    public boolean handlePaymentNotify(Map<String, String> params) {
        // 验证签名
        if (!alipayService.verifyNotify(params)) {
            log.error("支付宝异步通知签名验证失败");
            return false;
        }

        String orderNo = params.get("out_trade_no");
        String tradeNo = params.get("trade_no");
        String tradeStatus = params.get("trade_status");

        // 记录日志
        alipayService.logPaymentNotify(orderNo, tradeNo, tradeStatus, params);

        // 查询订单
        Order order = orderService.getOrderByOrderNo(orderNo);
        if (order == null) {
            log.error("订单不存在，订单号：{}", orderNo);
            return false;
        }

        // 如果已经处理过，直接返回成功
        if (order.getStatus() == 1) {
            log.info("订单已处理，订单号：{}", orderNo);
            return true;
        }

        // 处理支付成功
        if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
            // 更新订单状态
            orderService.updateOrderPaid(orderNo, tradeNo);

            // 如果是会员购买，创建会员
            if (order.getOrderType() == 2) {
                // 从订单描述或商品名称推断会员类型
                Integer membershipType = inferMembershipType(order.getProductName());
                if (membershipType != null) {
                    membershipService.createMembership(order.getUserId(), membershipType, order.getId());
                }
            }

            log.info("支付成功处理完成，订单号：{}", orderNo);
            return true;
        }

        return false;
    }

    /**
     * 查询订单详情
     */
    public OrderDetailResponse getOrderDetail(String orderNo) {
        Order order = orderService.getOrderByOrderNo(orderNo);
        if (order == null) {
            return null;
        }

        return OrderDetailResponse.builder()
                .orderNo(order.getOrderNo())
                .orderType(order.getOrderType())
                .productName(order.getProductName())
                .productDesc(order.getProductDesc())
                .amount(order.getAmount())
                .status(order.getStatus())
                .statusDesc(orderService.getOrderStatusDesc(order.getStatus()))
                .tradeNo(order.getTradeNo())
                .payTime(order.getPayTime())
                .expireTime(order.getExpireTime())
                .createTime(order.getCreateTime())
                .build();
    }

    /**
     * 查询用户订单列表
     */
    public List<OrderDetailResponse> getUserOrders(Long userId) {
        List<Order> orders = orderService.getOrdersByUserId(userId);
        return orders.stream()
                .map(order -> OrderDetailResponse.builder()
                        .orderNo(order.getOrderNo())
                        .orderType(order.getOrderType())
                        .productName(order.getProductName())
                        .productDesc(order.getProductDesc())
                        .amount(order.getAmount())
                        .status(order.getStatus())
                        .statusDesc(orderService.getOrderStatusDesc(order.getStatus()))
                        .tradeNo(order.getTradeNo())
                        .payTime(order.getPayTime())
                        .expireTime(order.getExpireTime())
                        .createTime(order.getCreateTime())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 查询用户会员信息
     */
    public MembershipInfoResponse getUserMembershipInfo(Long userId) {
        Membership membership = membershipService.getActiveMembership(userId);
        
        if (membership == null) {
            return MembershipInfoResponse.builder()
                    .isMember(false)
                    .build();
        }

        return MembershipInfoResponse.builder()
                .isMember(true)
                .membershipType(membership.getMembershipType())
                .membershipTypeDesc(membershipService.getMembershipTypeDesc(membership.getMembershipType()))
                .startTime(membership.getStartTime())
                .endTime(membership.getEndTime())
                .remainingDays(membershipService.getRemainingDays(membership.getEndTime()))
                .build();
    }

    /**
     * 查询所有会员套餐
     */
    public List<MembershipPackageResponse> getAllMembershipPackages() {
        List<MembershipPackage> packages = membershipService.getAllPackages();
        return packages.stream()
                .map(this::convertToPackageResponse)
                .collect(Collectors.toList());
    }

    /**
     * 转换会员套餐为响应DTO
     */
    private MembershipPackageResponse convertToPackageResponse(MembershipPackage pkg) {
        // 计算折扣
        BigDecimal discount = pkg.getSalePrice()
                .divide(pkg.getOriginalPrice(), 2, RoundingMode.HALF_UP)
                .multiply(BigDecimal.TEN);
        String discountStr = discount.setScale(1, RoundingMode.HALF_UP) + "折";

        // 解析特权列表
        List<String> features = null;
        try {
            features = objectMapper.readValue(pkg.getFeatures(), new TypeReference<List<String>>() {});
        } catch (Exception e) {
            log.error("解析会员特权失败", e);
        }

        return MembershipPackageResponse.builder()
                .id(pkg.getId())
                .packageName(pkg.getPackageName())
                .packageType(pkg.getPackageType())
                .durationDays(pkg.getDurationDays())
                .originalPrice(pkg.getOriginalPrice())
                .salePrice(pkg.getSalePrice())
                .discount(discountStr)
                .description(pkg.getDescription())
                .features(features)
                .build();
    }

    /**
     * 从商品名称推断会员类型
     */
    private Integer inferMembershipType(String productName) {
        if (productName.contains("月度")) {
            return 1;
        } else if (productName.contains("季度")) {
            return 2;
        } else if (productName.contains("年度")) {
            return 3;
        }
        return null;
    }
}

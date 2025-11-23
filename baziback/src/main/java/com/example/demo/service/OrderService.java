package com.example.demo.service;

import com.example.demo.entity.Order;
import com.example.demo.mapper.OrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 订单服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderMapper orderMapper;

    /**
     * 创建订单
     */
    @Transactional
    public Order createOrder(Long userId, Integer orderType, String productName, String productDesc, BigDecimal amount) {
        // 生成订单号
        String orderNo = generateOrderNo();

        // 创建订单
        Order order = Order.builder()
                .orderNo(orderNo)
                .userId(userId)
                .orderType(orderType)
                .productName(productName)
                .productDesc(productDesc)
                .amount(amount)
                .status(0) // 待支付
                .expireTime(LocalDateTime.now().plusMinutes(30)) // 30分钟后过期
                .build();

        orderMapper.insert(order);
        log.info("创建订单成功，订单号：{}，用户ID：{}", orderNo, userId);
        
        return order;
    }

    /**
     * 根据订单号查询订单
     */
    public Order getOrderByOrderNo(String orderNo) {
        return orderMapper.findByOrderNo(orderNo);
    }

    /**
     * 根据用户ID查询订单列表
     */
    public List<Order> getOrdersByUserId(Long userId) {
        return orderMapper.findByUserId(userId);
    }

    /**
     * 更新订单为已支付
     */
    @Transactional
    public boolean updateOrderPaid(String orderNo, String tradeNo) {
        int rows = orderMapper.updateStatus(orderNo, 1, tradeNo, LocalDateTime.now());
        if (rows > 0) {
            log.info("订单支付成功，订单号：{}，交易号：{}", orderNo, tradeNo);
            return true;
        }
        return false;
    }

    /**
     * 取消订单
     */
    @Transactional
    public boolean cancelOrder(String orderNo) {
        int rows = orderMapper.cancelOrder(orderNo);
        if (rows > 0) {
            log.info("取消订单成功，订单号：{}", orderNo);
            return true;
        }
        return false;
    }

    /**
     * 生成订单号
     */
    private String generateOrderNo() {
        return "ORD" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * 获取订单状态描述
     */
    public String getOrderStatusDesc(Integer status) {
        return switch (status) {
            case 0 -> "待支付";
            case 1 -> "已支付";
            case 2 -> "已取消";
            case 3 -> "已退款";
            default -> "未知状态";
        };
    }
}

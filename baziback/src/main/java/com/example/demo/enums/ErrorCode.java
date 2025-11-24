package com.example.demo.enums;

import lombok.Getter;

/**
 * 统一错误码枚举
 * 错误码格式：模块(2位) + 类型(2位) + 序号(2位)
 * 
 * 模块划分：
 * 10 - 系统通用
 * 20 - 用户模块
 * 30 - 支付模块
 * 40 - 分析模块
 * 50 - 知识库模块
 * 60 - 反馈模块
 */
@Getter
public enum ErrorCode {
    
    // ========== 系统通用错误 10xxxx ==========
    SUCCESS(100000, "操作成功"),
    SYSTEM_ERROR(100001, "系统异常，请稍后重试"),
    PARAM_ERROR(100002, "参数错误"),
    PARAM_MISSING(100003, "缺少必要参数"),
    PARAM_INVALID(100004, "参数格式不正确"),
    REQUEST_METHOD_ERROR(100005, "请求方法不支持"),
    REQUEST_TIMEOUT(100006, "请求超时"),
    RATE_LIMIT_EXCEEDED(100007, "请求过于频繁，请稍后再试"),
    SERVICE_UNAVAILABLE(100008, "服务暂时不可用"),
    DATABASE_ERROR(100009, "数据库操作失败"),
    
    // ========== 用户模块错误 20xxxx ==========
    USER_NOT_FOUND(200001, "用户不存在"),
    USER_ALREADY_EXISTS(200002, "用户已存在"),
    USERNAME_ALREADY_EXISTS(200003, "用户名已存在"),
    EMAIL_ALREADY_EXISTS(200004, "邮箱已被注册"),
    PHONE_ALREADY_EXISTS(200005, "手机号已被注册"),
    PASSWORD_ERROR(200006, "密码错误"),
    PASSWORD_TOO_WEAK(200007, "密码强度不足"),
    TOKEN_INVALID(200008, "登录凭证无效"),
    TOKEN_EXPIRED(200009, "登录凭证已过期"),
    UNAUTHORIZED(200010, "未授权，请先登录"),
    PERMISSION_DENIED(200011, "权限不足"),
    USER_DISABLED(200012, "用户已被禁用"),
    USER_INFO_INCOMPLETE(200013, "用户信息不完整"),
    
    // ========== 支付模块错误 30xxxx ==========
    ORDER_NOT_FOUND(300001, "订单不存在"),
    ORDER_ALREADY_PAID(300002, "订单已支付"),
    ORDER_EXPIRED(300003, "订单已过期"),
    ORDER_CANCELLED(300004, "订单已取消"),
    PAYMENT_FAILED(300005, "支付失败"),
    PAYMENT_AMOUNT_ERROR(300006, "支付金额错误"),
    PAYMENT_VERIFY_FAILED(300007, "支付验证失败"),
    REFUND_FAILED(300008, "退款失败"),
    MEMBERSHIP_NOT_FOUND(300009, "会员信息不存在"),
    MEMBERSHIP_EXPIRED(300010, "会员已过期"),
    MEMBERSHIP_PACKAGE_NOT_FOUND(300011, "会员套餐不存在"),
    MEMBERSHIP_PACKAGE_DISABLED(300012, "会员套餐已下架"),
    INSUFFICIENT_BALANCE(300013, "余额不足"),
    
    // ========== 分析模块错误 40xxxx ==========
    BAZI_INFO_NOT_FOUND(400001, "八字信息不存在"),
    BAZI_INFO_INVALID(400002, "八字信息格式错误"),
    ANALYSIS_FAILED(400003, "分析失败，请稍后重试"),
    ANALYSIS_HISTORY_NOT_FOUND(400004, "分析历史不存在"),
    REPORT_NOT_FOUND(400005, "报告不存在"),
    REPORT_GENERATION_FAILED(400006, "报告生成失败"),
    REPORT_EXPORT_FAILED(400007, "报告导出失败"),
    MCP_SERVICE_ERROR(400008, "MCP服务调用失败"),
    AI_MODEL_ERROR(400009, "AI模型调用失败"),
    ANALYSIS_QUOTA_EXCEEDED(400010, "分析次数已用完，请升级会员"),
    INVALID_ANALYSIS_TYPE(400011, "不支持的分析类型"),
    
    // ========== 知识库模块错误 50xxxx ==========
    CATEGORY_NOT_FOUND(500001, "分类不存在"),
    CATEGORY_ALREADY_EXISTS(500002, "分类已存在"),
    CATEGORY_HAS_CHILDREN(500003, "该分类下有子分类，无法删除"),
    CATEGORY_HAS_ARTICLES(500004, "该分类下有文章，无法删除"),
    ARTICLE_NOT_FOUND(500005, "文章不存在"),
    ARTICLE_ALREADY_EXISTS(500006, "文章已存在"),
    ARTICLE_DISABLED(500007, "文章已下架"),
    COLLECTION_ALREADY_EXISTS(500008, "已收藏过该内容"),
    COLLECTION_NOT_FOUND(500009, "收藏记录不存在"),
    
    // ========== 反馈模块错误 60xxxx ==========
    FEEDBACK_NOT_FOUND(600001, "反馈不存在"),
    FEEDBACK_ALREADY_PROCESSED(600002, "反馈已处理"),
    FEEDBACK_CONTENT_EMPTY(600003, "反馈内容不能为空"),
    RATING_OUT_OF_RANGE(600004, "评分必须在1-5之间"),
    
    // ========== 文件模块错误 70xxxx ==========
    FILE_NOT_FOUND(700001, "文件不存在"),
    FILE_UPLOAD_FAILED(700002, "文件上传失败"),
    FILE_SIZE_EXCEEDED(700003, "文件大小超过限制"),
    FILE_TYPE_NOT_SUPPORTED(700004, "不支持的文件类型"),
    FILE_DOWNLOAD_FAILED(700005, "文件下载失败");
    
    /**
     * 错误码
     */
    private final Integer code;
    
    /**
     * 错误信息
     */
    private final String message;
    
    ErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}

package com.example.demo.dto.response;

import lombok.Data;

/**
 * 统一响应格式
 * @param <T> 数据类型
 */
@Data
public class Result<T> {
    
    /**
     * 响应码：200-成功，400-客户端错误，500-服务器错误
     */
    private Integer code;
    
    /**
     * 响应消息
     */
    private String msg;
    
    /**
     * 响应数据
     */
    private T data;
    
    /**
     * 私有构造函数
     */
    private Result() {}
    
    private Result(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }
    
    /**
     * 成功响应（带数据）
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "操作成功", data);
    }
    
    /**
     * 成功响应（带自定义消息和数据）
     */
    public static <T> Result<T> success(String msg, T data) {
        return new Result<>(200, msg, data);
    }
    
    /**
     * 成功响应（无数据）
     */
    public static <T> Result<T> success() {
        return new Result<>(200, "操作成功", null);
    }
    
    /**
     * 失败响应
     */
    public static <T> Result<T> error(String msg) {
        return new Result<>(500, msg, null);
    }
    
    /**
     * 失败响应（带错误码）
     */
    public static <T> Result<T> error(Integer code, String msg) {
        return new Result<>(code, msg, null);
    }
    
    /**
     * 客户端错误（400）
     */
    public static <T> Result<T> badRequest(String msg) {
        return new Result<>(400, msg, null);
    }
    
    /**
     * 未授权（401）
     */
    public static <T> Result<T> unauthorized(String msg) {
        return new Result<>(401, msg, null);
    }
    
    /**
     * 禁止访问（403）
     */
    public static <T> Result<T> forbidden(String msg) {
        return new Result<>(403, msg, null);
    }
}

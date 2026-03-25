package com.example.demo.common;

import com.example.demo.util.I18nHelper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {
    private Integer code;
    private String message;
    private T data;

    public static <T> Result<T> success(T data) {
        return new Result<>(200, I18nHelper.message("result.success", "操作成功"), data);
    }

    public static <T> Result<T> success(String message, T data) {
        return new Result<>(200, I18nHelper.localize(message), data);
    }

    public static <T> Result<T> success() {
        return new Result<>(200, I18nHelper.message("result.success", "操作成功"), null);
    }

    public static <T> Result<T> error(String message) {
        return new Result<>(500, I18nHelper.localize(message), null);
    }

    public static <T> Result<T> error(Integer code, String message) {
        return new Result<>(code, I18nHelper.localize(message), null);
    }

    public static <T> Result<T> error(Integer code, String message, T data) {
        return new Result<>(code, I18nHelper.localize(message), data);
    }

    public static <T> Result<T> unauthorized(String message) {
        return new Result<>(401, I18nHelper.localize(message), null);
    }

    public static <T> Result<T> forbidden(String message) {
        return new Result<>(403, I18nHelper.localize(message), null);
    }

    public static <T> Result<T> badRequest(String message) {
        return new Result<>(400, I18nHelper.localize(message), null);
    }
}

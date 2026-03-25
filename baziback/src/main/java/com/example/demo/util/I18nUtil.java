package com.example.demo.util;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

/**
 * i18n 消息国际化工具类
 * 用于获取国际化的错误消息和成功消息
 */
@Component
@RequiredArgsConstructor
public class I18nUtil {

    private final MessageSource messageSource;

    /**
     * 获取国际化消息
     * @param code 消息键
     * @return 国际化消息
     */
    public String getMessage(String code) {
        try {
            return messageSource.getMessage(code, null, LocaleContextHolder.getLocale());
        } catch (Exception e) {
            // 如果消息不存在，返回消息键本身
            return code;
        }
    }

    /**
     * 获取带参数的国际化消息
     * @param code 消息键
     * @param args 参数数组
     * @return 国际化消息
     */
    public String getMessage(String code, Object... args) {
        try {
            return messageSource.getMessage(code, args, LocaleContextHolder.getLocale());
        } catch (Exception e) {
            // 如果消息不存在，返回消息键本身
            return code;
        }
    }

    /**
     * 获取错误消息
     * @param errorCode 错误代码
     * @return 错误消息
     */
    public String getErrorMessage(String errorCode) {
        return getMessage("error." + errorCode);
    }

    /**
     * 获取带参数的错误消息
     * @param errorCode 错误代码
     * @param args 参数数组
     * @return 错误消息
     */
    public String getErrorMessage(String errorCode, Object... args) {
        return getMessage("error." + errorCode, args);
    }

    /**
     * 获取成功消息
     * @param successCode 成功代码
     * @return 成功消息
     */
    public String getSuccessMessage(String successCode) {
        return getMessage("success." + successCode);
    }
}

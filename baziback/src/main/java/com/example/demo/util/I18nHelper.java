package com.example.demo.util;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Locale;

@Component
public class I18nHelper {

    private static MessageSource messageSource;

    public I18nHelper(MessageSource messageSource) {
        I18nHelper.messageSource = messageSource;
    }

    public static String message(String key, String defaultMessage) {
        return message(key, null, defaultMessage);
    }

    public static String message(String key, Object[] args, String defaultMessage) {
        if (!StringUtils.hasText(key)) {
            return defaultMessage;
        }
        if (messageSource == null) {
            return defaultMessage;
        }
        return messageSource.getMessage(key, args, defaultMessage, currentLocale());
    }

    public static String localize(String text) {
        if (!StringUtils.hasText(text)) {
            return text;
        }
        if (messageSource == null) {
            return text;
        }
        return messageSource.getMessage(text, null, text, currentLocale());
    }

    public static Locale currentLocale() {
        Locale locale = LocaleContextHolder.getLocale();
        if (locale == null) {
            return Locale.SIMPLIFIED_CHINESE;
        }
        return "en".equalsIgnoreCase(locale.getLanguage()) ? Locale.US : Locale.SIMPLIFIED_CHINESE;
    }
}

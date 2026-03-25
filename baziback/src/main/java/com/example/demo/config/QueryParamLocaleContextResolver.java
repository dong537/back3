package com.example.demo.config;

import org.springframework.context.i18n.SimpleLocaleContext;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.i18n.LocaleContextResolver;

import java.util.Locale;

public class QueryParamLocaleContextResolver implements LocaleContextResolver {

    private static final Locale DEFAULT_LOCALE = Locale.SIMPLIFIED_CHINESE;

    @Override
    public org.springframework.context.i18n.LocaleContext resolveLocaleContext(ServerWebExchange exchange) {
        return new SimpleLocaleContext(resolveLocale(exchange));
    }

    @Override
    public void setLocaleContext(ServerWebExchange exchange,
                                 org.springframework.context.i18n.LocaleContext localeContext) {
        throw new UnsupportedOperationException("Changing locale is not supported");
    }

    private Locale resolveLocale(ServerWebExchange exchange) {
        String lang = exchange.getRequest().getQueryParams().getFirst("lang");
        if (!StringUtils.hasText(lang)) {
            lang = exchange.getRequest().getHeaders().getFirst("X-Language");
        }
        if (!StringUtils.hasText(lang)) {
            lang = exchange.getRequest().getHeaders().getFirst("Accept-Language");
        }
        return normalize(lang);
    }

    static Locale normalize(String rawLocale) {
        if (!StringUtils.hasText(rawLocale)) {
            return DEFAULT_LOCALE;
        }
        String candidate = rawLocale.trim();
        int commaIndex = candidate.indexOf(',');
        if (commaIndex >= 0) {
            candidate = candidate.substring(0, commaIndex);
        }
        candidate = candidate.replace('_', '-').toLowerCase(Locale.ROOT);
        if (candidate.startsWith("en")) {
            return Locale.US;
        }
        return DEFAULT_LOCALE;
    }
}

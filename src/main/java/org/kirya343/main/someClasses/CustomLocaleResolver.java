package org.kirya343.main.someClasses;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.LocaleResolver;

import java.util.Locale;

public class CustomLocaleResolver implements LocaleResolver {

    private final String cookieName = "locale";
    private final int cookieMaxAgeSeconds = 30 * 24 * 60 * 60;

    @Override
    public @NonNull Locale resolveLocale(@NonNull HttpServletRequest request) {
        // 1. Проверяем куку
        if (request.getCookies() != null) {
            for (var cookie : request.getCookies()) {
                if (cookieName.equals(cookie.getName())) {
                    return Locale.forLanguageTag(cookie.getValue());
                }
            }
        }

        // 2. Если куки нет — берём язык из заголовка браузера
        String headerLang = request.getHeader("Accept-Language");
        if (headerLang != null && !headerLang.isEmpty()) {
            return Locale.forLanguageTag(headerLang.split(",")[0]);
        }

        // 3. Если ничего нет — fallback
        return Locale.ENGLISH;
    }

    @Override
    public void setLocale(@NonNull HttpServletRequest request, @Nullable HttpServletResponse response, @Nullable Locale locale) {
        // Читаем параметр lang из запроса
        String newLang = request.getParameter("lang");
        if (newLang != null && !newLang.isEmpty()) {
            Cookie cookie = new Cookie(cookieName, newLang);
            cookie.setPath("/"); // доступна на всём сайте
            cookie.setMaxAge(cookieMaxAgeSeconds);
            response.addCookie(cookie);
        }
    }
}
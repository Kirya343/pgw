package org.kirya343.main.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Locale;

@Controller
public class LanguageController {

    @Autowired
    private HttpServletRequest request;

    @GetMapping("/lang")
    public String changeLanguage(@RequestParam("lang") String lang) {
        Locale locale = new Locale(lang);
        LocaleContextHolder.setLocale(locale);
        return "redirect:" + request.getHeader("Referer"); // Перенаправляет на предыдущую страницу
    }
}


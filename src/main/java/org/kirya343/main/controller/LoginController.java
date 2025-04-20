package org.kirya343.main.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error,
                            HttpServletRequest request,
                            Model model) {
        if (error != null) {
            model.addAttribute("error", "Неверные учетные данные");
        }
        // Получаем Referer из запроса и передаем в модель
        String referer = request.getHeader("Referer");
        if (referer != null && !referer.contains("/login")) {
            model.addAttribute("referer", referer);
        }
        return "login";
    }

    @GetMapping("/loginSuccess")
    public String loginSuccess(OAuth2AuthenticationToken authentication, Model model, HttpServletRequest request) {
        // Получаем имя пользователя из токена аутентификации
        String name = authentication.getPrincipal().getAttribute("name");
        model.addAttribute("name", name);  // Добавляем имя пользователя в модель

        // Получаем сохранённый в сессии URL до авторизации (если он был)
        String referer = (String) request.getSession().getAttribute("url_prior_login");

        // Если URL существует и не содержит "/login", перенаправляем туда
        if (referer != null && !referer.contains("/login")) {
            return "redirect:" + referer;
        }

        // Если URL не сохранён или он невалиден, перенаправляем на каталог
        return "redirect:/catalog";
    }

}

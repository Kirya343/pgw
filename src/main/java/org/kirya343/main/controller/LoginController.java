package org.kirya343.main.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import org.kirya343.main.services.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class LoginController {

    private final UserService userService;

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error, Model model) {
        if (error != null) {
            model.addAttribute("error", "Неверные учетные данные");
        }

        return "redirect:/oauth2/authorization/google";
        //return "login";
    }

    @GetMapping("/loginSuccess")
    public String loginSuccess(OAuth2AuthenticationToken authentication, HttpServletRequest request) {

        OAuth2User oauth2User = authentication.getPrincipal();

        // Проверяем, есть ли пользователь в базе
        var user = userService.findUserFromOAuth2(oauth2User);
        if (user != null) {
            return "redirect:/catalog";
        }

        request.getSession().setAttribute("oauth2User", oauth2User);
        return "redirect:/register";
    }

    @GetMapping("/register")
    public String registerPage(HttpServletRequest request, Model model) {
        OAuth2User oauth2User = (OAuth2User) request.getSession().getAttribute("oauth2User");

        if (oauth2User == null) {
            return "redirect:/login";
        }

        // Всегда добавляем атрибуты в модель
        model.addAttribute("name", oauth2User.getAttribute("name"));
        model.addAttribute("email", oauth2User.getAttribute("email"));
        model.addAttribute("picture", oauth2User.getAttribute("picture"));

        return "register";
    }

    @PostMapping("/register")
    public String confirmRegister(HttpServletRequest request) {
        OAuth2User oauth2User = (OAuth2User) request.getSession().getAttribute("oauth2User");

        if (oauth2User == null) {
            return "redirect:/login?error=no_auth_data";
        }

        // Регистрируем только при явном подтверждении (POST)
        userService.registerUserFromOAuth2(oauth2User);
        request.getSession().removeAttribute("oauth2User");

        return "redirect:/catalog";
    }
}

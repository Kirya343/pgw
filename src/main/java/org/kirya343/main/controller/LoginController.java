package org.kirya343.main.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.kirya343.main.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
public class LoginController {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private UserService userService;

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error, Model model) {
        logger.info("Доступ к странице входа.");

        if (error != null) {
            logger.warn("Ошибка входа: неверные учетные данные.");
            model.addAttribute("error", "Неверные учетные данные");
        }

        return "redirect:/oauth2/authorization/google";
        //return "login";
    }

    @GetMapping("/loginSuccess")
    public String loginSuccess(OAuth2AuthenticationToken authentication, HttpServletRequest request) {
        logger.info("Обработка успешного входа.");

        OAuth2User oauth2User = authentication.getPrincipal();
        logger.info("Успешная аутентификация через OAuth2 для пользователя: {}", (Object) oauth2User.getAttribute("email"));

        // Проверяем, есть ли пользователь в базе
        var user = userService.findUserFromOAuth2(oauth2User);
        if (user != null) {
            logger.info("Пользователь найден в базе данных, перенаправляем в каталог.");
            return "redirect:/catalog";
        }

        // Если пользователя нет в базе, перенаправляем на страницу регистрации
        logger.info("Пользователь не найден в базе данных, перенаправляем на страницу регистрации.");
        request.getSession().setAttribute("oauth2User", oauth2User);
        return "redirect:/register";
    }

    @GetMapping("/register")
    public String registerPage(HttpServletRequest request, Model model) {
        OAuth2User oauth2User = (OAuth2User) request.getSession().getAttribute("oauth2User");

        if (oauth2User == null) {
            // Пробуем получить из аутентификации
            logger.info("Пробуем получить из аутентификации.");
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof OAuth2User) {
                oauth2User = (OAuth2User) auth.getPrincipal();
                request.getSession().setAttribute("oauth2User", oauth2User);
            } else {
                return "redirect:/login";
            }
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
        logger.info("Регистрируем пользователя.");
        userService.registerUserFromOAuth2(oauth2User);
        request.getSession().removeAttribute("oauth2User");

        return "redirect:/catalog";
    }
}

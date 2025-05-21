package org.kirya343.main.controller;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Locale;

import org.kirya343.main.model.User;
import org.kirya343.main.services.UserService;
import org.kirya343.main.services.components.AuthService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class InfoController {

    private final UserService userService;
    private final AuthService authService;

    public InfoController(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    @GetMapping("/info")
    public String home(HttpServletRequest request, Model model, @AuthenticationPrincipal OAuth2User oauth2User, Locale locale) {
        User user = null;
        if (oauth2User != null) {
            user = userService.findUserFromOAuth2(oauth2User);
        }
        authService.addAuthenticationAttributes(model, oauth2User, user);
        model.addAttribute("requestURI", request.getRequestURI());
        return "info";
    }
}

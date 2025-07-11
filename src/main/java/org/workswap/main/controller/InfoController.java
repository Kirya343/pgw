package org.workswap.main.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import java.util.Locale;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.workswap.main.services.components.AuthService;

@Controller
@RequiredArgsConstructor
public class InfoController {

    private final AuthService authService;

    @GetMapping("/info")
    public String home(HttpServletRequest request, Model model, @AuthenticationPrincipal OAuth2User oauth2User, Locale locale) {
        authService.validateAndAddAuthentication(model, oauth2User);
        model.addAttribute("requestURI", request.getRequestURI());
        return "info";
    }
}

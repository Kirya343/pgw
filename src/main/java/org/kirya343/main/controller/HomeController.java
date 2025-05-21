package org.kirya343.main.controller;

import java.util.Locale;

import org.kirya343.main.model.User;
import org.kirya343.main.services.ListingService;
import org.kirya343.main.services.StorageService;
import org.kirya343.main.services.UserService;
import org.kirya343.main.services.components.AdminCheckService;
import org.kirya343.main.services.components.AuthService;
import org.kirya343.main.services.components.AvatarService;
import org.kirya343.main.services.components.StatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(HttpServletRequest request, Model model) {

        model.addAttribute("requestURI", request.getRequestURI());
        return "redirect:/catalog";
    }
}

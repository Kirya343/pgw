package org.kirya343.main.controller;

import org.kirya343.main.model.User;
import org.kirya343.main.services.AvatarService;
import org.kirya343.main.services.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.security.oauth2.core.user.OAuth2User;

@Controller
@RequestMapping("/terms")
public class TermsController {

    private final UserService userService;
    private final AvatarService avatarService;

    public TermsController(UserService userService, AvatarService avatarService) {
        this.userService = userService;
        this.avatarService = avatarService;
    }

    @GetMapping
    public String showTermsForm(@AuthenticationPrincipal OAuth2User oauth2User, Model model) {

        if (oauth2User != null) {
            User user = userService.findUserFromOAuth2(oauth2User);
            String name = user.getName() != null ? user.getName() : oauth2User.getAttribute("name");
            String avatarPath = avatarService.resolveAvatarPath(user);

            model.addAttribute("isAuthenticated", true);
            model.addAttribute("userName", name != null ? name : "Пользователь");
            model.addAttribute("avatarUrl", avatarPath);
        } else {
            model.addAttribute("isAuthenticated", false);
            model.addAttribute("userName", "Пользователь");
            model.addAttribute("avatarUrl", "/images/avatar-placeholder.jpg");
        }
        return "terms";
    }
}

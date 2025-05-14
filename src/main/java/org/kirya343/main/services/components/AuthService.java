package org.kirya343.main.services.components;

import org.kirya343.main.model.User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

@Service
public class AuthService {

    private final AvatarService avatarService;

    public AuthService(AvatarService avatarService) {
        this.avatarService = avatarService;
    }

    public void addAuthenticationAttributes(Model model, OAuth2User oauth2User, User user) {
        if (oauth2User != null) {
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
    }
}


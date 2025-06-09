package org.kirya343.main.services.components;

import org.kirya343.main.model.User;
import org.kirya343.main.model.User.Role;
import org.kirya343.main.services.UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoleCheckService {

    private final UserService userService;

    public void CheckRoles(Model model, OAuth2User oauth2User) {
        
        User user = userService.findUserFromOAuth2(oauth2User);

        if (user.getRole().isAtLeast(Role.USER)) {
            model.addAttribute("rolePremium", true);
        }

        if (user.getRole().isAtLeast(Role.PREMIUM)) {
            model.addAttribute("rolePremium", true);
        }

        if (user.getRole().isAtLeast(Role.BUSINESS)) {
            model.addAttribute("roleBusiness", true);
        }

        if (user.getRole().isAtLeast(Role.ADMIN)) {
            model.addAttribute("roleAdmin", true);
        }
    }
}
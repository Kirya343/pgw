package org.workswap.main.services.components;

import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.workswap.datasource.main.model.User;
import org.workswap.datasource.main.model.User.Role;
import org.workswap.main.services.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoleCheckService {

    private final UserService userService;

    public void checkRoles(Model model, OAuth2User oauth2User) {
        
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

    public boolean hasRoleAdmin(User user) {
        if(user.getRole() == Role.ADMIN) {
            return true;
        }
        return false;
    }
}
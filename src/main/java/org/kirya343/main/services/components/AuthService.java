package org.kirya343.main.services.components;

import org.kirya343.main.exceptions.UserNotRegisteredException;
import org.kirya343.main.model.User;
import org.kirya343.main.services.UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    /* private final AvatarService avatarService; */
    private final StatService statService;
    private final RoleCheckService roleCheckService;

    private void addAuthenticationAttributes(Model model, OAuth2User oauth2User, User user) {
        if (oauth2User != null) {
            double averageRating = statService.getAverageRating(user);

            roleCheckService.CheckRoles(model, oauth2User);

            model.addAttribute("isAuthenticated", true);
            /* model.addAttribute("avatarPath", avatarPath); */
            model.addAttribute("user", user);
            model.addAttribute("rating", averageRating);
        } else {
            model.addAttribute("isAuthenticated", false);
        }
    }

    public void validateAndAddAuthentication(Model model, OAuth2User oauth2User) {
        if (oauth2User == null) {
            addAuthenticationAttributes(model, null, null);
            return;
        }

        User user = userService.findUserFromOAuth2(oauth2User);
        if (user == null) {
            throw new UserNotRegisteredException("User not found in the system");
        }

        addAuthenticationAttributes(model, oauth2User, user);
    }
}


package org.kirya343.main.controller;

import org.kirya343.main.model.User;
import org.kirya343.main.services.components.AuthService;
import org.kirya343.main.services.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.security.oauth2.core.user.OAuth2User;

@Controller
@RequestMapping
public class TermsController {

    private final UserService userService;
    private final AuthService authService;

    public TermsController(UserService userService, AuthService authService ) {
        this.userService = userService;
        this.authService  = authService ;
    }

    @GetMapping("/terms")
    public String showTermsForm(@AuthenticationPrincipal OAuth2User oauth2User, Model model) {
        User user = oauth2User != null ? userService.findUserFromOAuth2(oauth2User) : null;
        authService.addAuthenticationAttributes(model, oauth2User, user);
        return "terms";
    }

    @GetMapping("/privacy-policy")
    public String showPrivacyPolicyForm(@AuthenticationPrincipal OAuth2User oauth2User, Model model) {
        User user = oauth2User != null ? userService.findUserFromOAuth2(oauth2User) : null;
        authService.addAuthenticationAttributes(model, oauth2User, user);
        return "privacy-policy";
    }
}

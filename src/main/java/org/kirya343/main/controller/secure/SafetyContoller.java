package org.kirya343.main.controller.secure;

import java.util.Locale;

import org.kirya343.main.services.components.AuthService;
import org.springframework.boot.actuate.web.exchanges.HttpExchange.Principal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;


@Controller
@RequestMapping("/secure/safety")
@RequiredArgsConstructor
public class SafetyContoller {

    private final AuthService authService;

    @GetMapping
    public String securityPage(Model model, @AuthenticationPrincipal OAuth2User oauth2User, Locale locale) {
        if (oauth2User == null) {
            return "redirect:/login";
        }

        authService.validateAndAddAuthentication(model, oauth2User);

        model.addAttribute("activePage", "safety");

        model.addAttribute("policyUpdateDate", "01.05.2025"); // Заменить на реальную дату
        
        return "secure/safety";
    }

    @PostMapping("/delete-account")
    public ResponseEntity<?> deleteAccount(Principal principal) {
        // Логика удаления аккаунта
        return ResponseEntity.ok().build();
    }

    @PostMapping("/delete-listings")
    public ResponseEntity<?> deleteListings(Principal principal) {
        // Логика удаления объявлений
        return ResponseEntity.ok().build();
    }

    @PostMapping("/delete-messages")
    public ResponseEntity<?> deleteMessages(Principal principal) {
        // Логика удаления сообщений
        return ResponseEntity.ok().build();
    }
}

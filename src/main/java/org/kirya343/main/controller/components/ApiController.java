package org.kirya343.main.controller.components;

import java.security.Principal;
import java.time.LocalDateTime;

import org.kirya343.main.model.User;
import org.kirya343.main.repository.UserRepository;
import org.kirya343.main.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class ApiController {

    private final UserService userService;
    private final UserRepository userRepository;

    public ApiController(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @PostMapping("/api/users/accept-terms")
    public ResponseEntity<?> acceptTerms(Principal principal) {
        System.out.println("Запрос на принятие условий получен");
        User user = userService.findByUsername(principal.getName());
        if (user != null) {
            user.setTermsAcceptanceDate(LocalDateTime.now());
            user.setTermsAccepted(true);
            userRepository.save(user);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}

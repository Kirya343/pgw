package org.kirya343.main.controller.components;

import java.security.Principal;
import java.time.LocalDateTime;

import org.kirya343.main.model.User;
import org.kirya343.main.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class ApiController {

    private final UserService userService;

    @PostMapping("/api/users/accept-terms")
    public ResponseEntity<?> acceptTerms(Principal principal) {
        String sub = principal.getName();
        System.out.println("Principal: " + (principal != null ? sub : "null"));

        User user = userService.findBySub(sub);
        System.out.println("User: " +  user.getEmail());
        
        user.setTermsAcceptanceDate(LocalDateTime.now());
        user.setTermsAccepted(true);
        
        System.out.println("Date: " +  user.getTermsAcceptanceDate());
        System.out.println("Status: " +  user.isTermsAccepted());
        
        userService.save(user);

        System.out.println("Saved date: " +  user.getTermsAcceptanceDate());
        System.out.println("Saved status: " +  user.isTermsAccepted());
        
        return ResponseEntity.ok().build();
    }
}

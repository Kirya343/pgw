package org.kirya343.main.controller.secure;

import java.util.Locale;

import org.kirya343.main.model.User;
import org.kirya343.main.services.UserService;
import org.kirya343.main.services.components.AdminCheckService;
import org.kirya343.main.services.components.AvatarService;
import org.springframework.boot.actuate.web.exchanges.HttpExchange.Principal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
@RequestMapping("/secure/safety")
public class SafetyContoller {

    private final UserService userService;
    private final AvatarService avatarService;
    private final AdminCheckService adminCheckService;

    public SafetyContoller(UserService userService, AvatarService avatarService, AdminCheckService adminCheckService) {
        this.userService = userService;
        this.avatarService = avatarService;
        this.adminCheckService = adminCheckService;
    }

    @GetMapping
    public String securityPage(Model model, @AuthenticationPrincipal OAuth2User oauth2User, Locale locale) {
        if (oauth2User == null) {
            return "redirect:/login";
        }

        boolean isAdmin = adminCheckService.isAdmin(oauth2User);

        // Получаем или создаем пользователя
        User user = userService.findUserFromOAuth2(oauth2User);

        // Определяем URL аватара с приоритетом avatarUrl над picture
        String avatarPath = avatarService.resolveAvatarPath(user);

        // Получаем email и имя (используем данные из БД, а не напрямую из OAuth2)
        String email = user.getEmail() != null ? user.getEmail() : oauth2User.getAttribute("email");
        String name = user.getName();


        // Получаем даты из сервиса (можно хранить в базе данных или properties)
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("userEmail", email != null ? email : "Email не распознан");
        model.addAttribute("userName", name != null ? name : "Пользователь");
        model.addAttribute("avatarPath", avatarPath);
        model.addAttribute("user", user);

        model.addAttribute("activePage", "safety");

        model.addAttribute("policyUpdateDate", "01.01.2023"); // Заменить на реальную дату
        model.addAttribute("termsAcceptanceDate", "15.05.2023"); // Заменить на дату принятия пользователем
        
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

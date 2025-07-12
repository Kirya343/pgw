package org.workswap.main.controller.secure;

import java.util.Locale;

import org.springframework.boot.actuate.web.exchanges.HttpExchange.Principal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.workswap.datasource.main.model.User;
import org.workswap.main.services.UserService;
import org.workswap.main.services.components.AuthService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;


@Controller
@RequestMapping("/secure/security")
@RequiredArgsConstructor
public class SecurityContoller {

    private final AuthService authService;
    private final UserService userService;

    @GetMapping
    public String securityPage(Model model, @AuthenticationPrincipal OAuth2User oauth2User, Locale locale) {
        if (oauth2User == null) {
            return "redirect:/login";
        }

        authService.validateAndAddAuthentication(model, oauth2User);

        model.addAttribute("activePage", "security");

        model.addAttribute("policyUpdateDate", "01.05.2025"); // Заменить на реальную дату
        
        return "secure/security";
    }

    @PostMapping("/delete-account")
    public String deleteAccount(@AuthenticationPrincipal OAuth2User oauth2User,
                                HttpServletRequest request,
                                HttpServletResponse response) throws ServletException {
        User user = userService.findUserFromOAuth2(oauth2User);
        userService.deleteById(user.getId());

        // Завершаем текущую сессию
        request.logout();

        // Инвалидируем сессию и удаляем куки (на всякий случай)
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        // Удаляем куку
        Cookie cookie = new Cookie("JSESSIONID", null);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        return "redirect:/catalog";
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

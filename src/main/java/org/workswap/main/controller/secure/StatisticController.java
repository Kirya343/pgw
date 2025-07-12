package org.workswap.main.controller.secure;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.workswap.datasource.main.model.Listing;
import org.workswap.datasource.main.model.User;
import org.workswap.datasource.stats.model.StatSnapshot;
import org.workswap.main.services.*;
import org.workswap.main.services.components.AuthService;
import org.workswap.main.services.components.StatService;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Locale;
import java.util.Map;

@Controller
@RequestMapping("/secure/statistic")
@RequiredArgsConstructor
public class StatisticController {

    private final UserService userService;
    private final ListingService listingService;
    private final StatService statService;
    private final AuthService authService;

    @GetMapping
    public String getAccountPage(Model model, @AuthenticationPrincipal OAuth2User oauth2User, Locale locale) {
        if (oauth2User == null) {
            return "redirect:/login";
        }

        // Получаем или создаем пользователя
        User user = userService.findUserFromOAuth2(oauth2User);

        List<Listing> listings = listingService.localizeAccountListings(user, locale);

        Map<String, Object> userStats = statService.getUserStats(user, locale);

        authService.validateAndAddAuthentication(model, oauth2User);

        // Передаем данные в модель
        model.addAttribute("listings", listings);
        model.addAttribute("stats", userStats);
        model.addAttribute("intervalTypes", StatSnapshot.IntervalType.values());

        // Переменная для отображения активной страницы
        model.addAttribute("activePage", "account");

        return "secure/statistic";
    }
}

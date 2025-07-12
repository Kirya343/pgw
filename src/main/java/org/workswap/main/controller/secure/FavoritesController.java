package org.workswap.main.controller.secure;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.workswap.datasource.main.model.Listing;
import org.workswap.datasource.main.model.User;
import org.workswap.main.services.ListingService;
import org.workswap.main.services.UserService;
import org.workswap.main.services.components.AuthService;

import java.util.List;
import java.util.Locale;

@Controller
@RequiredArgsConstructor
public class FavoritesController {
    private final UserService userService;
    private final AuthService authService;
    private final ListingService listingService;

    @GetMapping("/secure/favorites")
    public String getFavoritesPage(@AuthenticationPrincipal OAuth2User oauth2User,
                                   Model model,
                                   Locale locale) {

        User user = userService.findUserFromOAuth2(oauth2User);

        List<Listing> listings = listingService.localizeFavoriteListings(user, locale);

        authService.validateAndAddAuthentication(model, oauth2User);

        // Переменная для отображения активной страницы
        model.addAttribute("listings", listings);
        model.addAttribute("activePage", "favorites");

        return "secure/favorites";
    }
}


package org.kirya343.main.controller.secure;

import org.kirya343.main.model.FavoriteListing;
import org.kirya343.main.model.Listing;
import org.kirya343.main.model.User;
import org.kirya343.main.services.components.AuthService;
import org.kirya343.main.services.FavoriteListingService;
import org.kirya343.main.services.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class FavoritesController {

    private final FavoriteListingService favoriteListingService;
    private final UserService userService;
    private final AuthService authService;

    @GetMapping("/secure/favorites")
    public String getFavoritesPage(@AuthenticationPrincipal OAuth2User oauth2User,
                                   Model model,
                                   Locale locale,
                                   HttpSession session) {
        if (oauth2User == null) {
            return "redirect:/login";
        }

        User user = userService.findUserFromOAuth2(oauth2User);

        List<FavoriteListing> favoriteListings = favoriteListingService.findByUser(user);
        List<Listing> listings = favoriteListings.stream()
                .map(FavoriteListing::getListing)
                .collect(Collectors.toList());

        for (Listing listing : listings) {
            String title = null;
            String description = null;

            if ("fi".equals(locale.getLanguage()) && listing.getCommunityFi()) {
                title = listing.getTitleFi();
                description = listing.getDescriptionFi();
            } else if ("ru".equals(locale.getLanguage()) && listing.getCommunityRu()) {
                title = listing.getTitleRu();
                description = listing.getDescriptionRu();
            } else if ("en".equals(locale.getLanguage()) && listing.getCommunityEn()) {
                title = listing.getTitleEn();
                description = listing.getDescriptionEn();
            }

            // Если нет значения для выбранного языка, пробуем другие языки
            if (title == null || description == null) {
                if (title == null) {
                    title = listing.getTitleFi() != null ? listing.getTitleFi() :
                            listing.getTitleRu() != null ? listing.getTitleRu() :
                                    listing.getTitleEn();
                }
                if (description == null) {
                    description = listing.getDescriptionFi() != null ? listing.getDescriptionFi() :
                            listing.getDescriptionRu() != null ? listing.getDescriptionRu() :
                                    listing.getDescriptionEn();
                }
            }

            // Сохраняем в транзиентные поля
            listing.setLocalizedTitle(title);
            listing.setLocalizedDescription(description);
        }

        model.addAttribute("listings", listings);

        authService.validateAndAddAuthentication(model, oauth2User);

        // Переменная для отображения активной страницы
        model.addAttribute("activePage", "favorites");

        return "secure/favorites";
    }
}


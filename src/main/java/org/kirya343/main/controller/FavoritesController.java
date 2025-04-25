package org.kirya343.main.controller;

import org.kirya343.main.model.FavoriteListing;
import org.kirya343.main.model.Listing;
import org.kirya343.main.model.User;
import org.kirya343.main.services.AvatarService;
import org.kirya343.main.services.FavoriteListingService;
import org.kirya343.main.services.StatService;
import org.kirya343.main.services.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class FavoritesController {

    private final FavoriteListingService favoriteListingService;
    private final UserService userService;
    private final AvatarService avatarService;
    private StatService statService;

    @Autowired
    public FavoritesController(FavoriteListingService favoriteListingService, UserService userService, AvatarService avatarService) {
        this.favoriteListingService = favoriteListingService;
        this.userService = userService;
        this.avatarService = avatarService;
    }

    @GetMapping("/secure/favorites")
    public String getFavoritesPage(@AuthenticationPrincipal OAuth2User oauth2User,
                                   Model model,
                                   HttpSession session) {
        if (oauth2User == null) {
            return "redirect:/login";
        }

        String email = oauth2User.getAttribute("email");
        User user = userService.findUserFromOAuth2(oauth2User);


        if (user == null) {
            return "redirect:/register";
        }

        List<FavoriteListing> favoriteListings = favoriteListingService.findByUser(user);
        List<Listing> listings = favoriteListings.stream()
                .map(FavoriteListing::getListing)
                .collect(Collectors.toList());

        String avatarPath = avatarService.resolveAvatarPath(user);
        String name = user.getName();

        model.addAttribute("listings", listings);
        model.addAttribute("userEmail", email != null ? email : "Email не распознан");
        model.addAttribute("userName", name != null ? name : "Пользователь");
        model.addAttribute("avatarPath", avatarPath);
        model.addAttribute("user", user);

        return "secure/favorites";
    }
}


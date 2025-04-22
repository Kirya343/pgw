package org.kirya343.main.controller;

import org.kirya343.main.model.Listing;
import org.kirya343.main.model.User;
import org.kirya343.main.services.AvatarService;
import org.kirya343.main.services.ListingService;
import org.kirya343.main.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/listing")
public class ListingsController {

    private final ListingService listingService;
    private final UserService userService;
    private final AvatarService avatarService;

    @Autowired
    public ListingsController(ListingService listingService,
                              UserService userService,
                              AvatarService avatarService) {
        this.listingService = listingService;
        this.userService = userService;
        this.avatarService = avatarService;
    }

    @GetMapping("/{id}")
    public String getListing(@PathVariable Long id, Model model, @AuthenticationPrincipal OAuth2User oauth2User) {
        Listing listing = listingService.getListingById(id);
        if (listing == null) {
            return "redirect:/catalog";
        }

        if (oauth2User != null) {
            User user = userService.findUserFromOAuth2(oauth2User);
            String name = user.getName() != null ? user.getName() : oauth2User.getAttribute("name");
            String avatarPath = avatarService.resolveAvatarPath(user);

            model.addAttribute("isAuthenticated", true);
            model.addAttribute("userName", name != null ? name : "Пользователь");
            model.addAttribute("avatarUrl", avatarPath);
        } else {
            model.addAttribute("isAuthenticated", false);
            model.addAttribute("userName", "Пользователь");
            model.addAttribute("avatarUrl", "/images/avatar-placeholder.jpg");
        }

        // Увеличиваем счетчик просмотров
        listing.setViews(listing.getViews() + 1);
        listingService.save(listing);

        // Получаем автора объявления и его аватар
        User author = listing.getAuthor();
        String authorAvatarPath = "/images/avatar-placeholder.jpg"; // значение по умолчанию

        if (author != null) {
            authorAvatarPath = avatarService.resolveAvatarPath(author);
        }

        boolean isOwner = false;
        if (oauth2User != null) {
            User user = userService.findUserFromOAuth2(oauth2User);
            // Проверяем, является ли пользователь автором объявления
            isOwner = listing.getAuthor() != null && listing.getAuthor().getId().equals(user.getId());

            model.addAttribute("isOwner", isOwner);
            // ... остальной код ...
        }

        model.addAttribute("listing", listing);
        model.addAttribute("author", author != null ? author.getName() : "Неизвестный автор");
        model.addAttribute("authorAvatarPath", authorAvatarPath);
        model.addAttribute("createdAt", listing.getCreatedAt());
        model.addAttribute("category", listing.getCategory());

        // Добавляем похожие объявления
        model.addAttribute("similarListings", listingService.findSimilarListings(listing.getCategory(), id));

        System.out.println("Listing features: " + listing.getFeatures());
        System.out.println("Listing images: " + listing.getImages());
        System.out.println("Listing reviews: " + listing.getReviews());
        return "listing";
    }
}
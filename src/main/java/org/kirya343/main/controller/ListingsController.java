package org.kirya343.main.controller;

import org.kirya343.main.model.Listing;
import org.kirya343.main.model.User;
import org.kirya343.main.services.AvatarService;
import org.kirya343.main.services.ListingService;
import org.kirya343.main.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
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
    public String getListing(@PathVariable Long id, Model model) {
        Listing listing = listingService.getListingById(id);
        if (listing == null) {
            return "redirect:/catalog";
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
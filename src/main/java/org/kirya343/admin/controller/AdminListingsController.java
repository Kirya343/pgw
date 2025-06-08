package org.kirya343.admin.controller;

import org.kirya343.main.model.Listing;
import org.kirya343.main.services.ListingService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import lombok.RequiredArgsConstructor;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/listings")
public class AdminListingsController {

    private final ListingService listingService;

    @GetMapping
    public String usersList(Model model) {
        List<Listing> listings = listingService.getAllListings();

        for (Listing listing : listings) {
            String title = null;
            String description = null;

            if (listing.getTitleRu() != null && !listing.getTitleRu().isBlank()) {
                title = listing.getTitleRu();
            } else if (listing.getTitleFi() != null && !listing.getTitleFi().isBlank()) {
                title = listing.getTitleFi();
            } else if (listing.getTitleEn() != null && !listing.getTitleEn().isBlank()) {
                title = listing.getTitleEn();
            }

            if (listing.getDescriptionRu() != null && !listing.getDescriptionRu().isBlank()) {
                description = listing.getDescriptionRu();
            } else if (listing.getDescriptionFi() != null && !listing.getDescriptionFi().isBlank()) {
                description = listing.getDescriptionFi();
            } else if (listing.getDescriptionEn() != null && !listing.getDescriptionEn().isBlank()) {
                description = listing.getDescriptionEn();
            }

            // Сохраняем в транзиентные поля
            listing.setLocalizedTitle(title);
            listing.setLocalizedDescription(description);
        }

        model.addAttribute("listings", listings);
        model.addAttribute("activePage", "admin-listings");
        return "admin/listings/listings-list";
    }

    @GetMapping("/view/{id}")
    public String currentUser(@PathVariable Long id, Model model) {
        try {
            Listing listing = listingService.getListingById(id);
            model.addAttribute("listing", listing);
            model.addAttribute("activePage", "admin-listings");
            return "admin/users/view-listing";
        } catch (IllegalArgumentException e) {
            return "redirect:/admin/listings-list";
        }
    }

    @PostMapping("/update/{id}")
    public String modifyUser(@PathVariable Long id,
                           @AuthenticationPrincipal OAuth2User oauth2User,
                           RedirectAttributes redirectAttributes) {
        try {
            listingService.save(listingService.getListingById(id));
            redirectAttributes.addFlashAttribute("successMessage", "Пользователь успешно обновлен");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при обновлении пользователя");
        }
        return "redirect:/admin/listings";
    }

    @GetMapping("/delete/{id}")
    public String deleteNews(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            listingService.deleteListing(id);
            redirectAttributes.addFlashAttribute("successMessage", "Пользователь успешно удален");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при удалении пользователя");
        }
        return "redirect:/admin/listings";
    }
}
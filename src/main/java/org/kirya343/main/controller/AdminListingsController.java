package org.kirya343.main.controller;

import org.kirya343.main.model.Listing;
import org.kirya343.main.services.ListingService;
import org.kirya343.main.services.components.AuthService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Locale;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/listings")
public class AdminListingsController {

    private final ListingService listingService;
    private final AuthService authService;

    @GetMapping
    public String listingsList(Model model, @AuthenticationPrincipal OAuth2User oauth2User) {

        authService.validateAndAddAuthentication(model, oauth2User);
        
        List<Listing> listings = listingService.getAllListings();

        for (Listing listing : listings) {
            listingService.localizeListing(listing, Locale.of("ru"));
        }

        model.addAttribute("listings", listings);
        model.addAttribute("activePage", "admin-listings");
        return "admin/listings/listings-list";
    }

    @GetMapping("/view/{id}")
    public String currentListing(@PathVariable Long id, Model model) {
        try {
            Listing listing = listingService.getListingById(id);
            model.addAttribute("listing", listing);
            model.addAttribute("activePage", "admin-listings");
            return "admin/listings/view-listing";
        } catch (IllegalArgumentException e) {
            return "redirect:/admin/listings-list";
        }
    }

    @PostMapping("/update/{id}")
    public String modifyListing(@PathVariable Long id,
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
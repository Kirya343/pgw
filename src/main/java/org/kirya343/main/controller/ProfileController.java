package org.kirya343.main.controller;

import org.kirya343.main.model.Listing;
import org.kirya343.main.model.Resume;
import org.kirya343.main.model.Review;
import org.kirya343.main.model.User;
import org.kirya343.main.services.*;
import org.kirya343.main.services.components.AuthService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Locale;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ListingService listingService;
    private final UserService userService;
    private final FavoriteListingService favoriteListingService;
    private final AuthService authService;
    private final ReviewService reviewService;
    private final ResumeService resumeService;


    @GetMapping("/{id}")
    public String getProfile(@PathVariable Long id, Model model, @AuthenticationPrincipal OAuth2User oauth2User, Locale locale) {
        
        User profileUser = userService.findById(id);

        List<Listing> listings = listingService.localizeAccountListings(profileUser, locale);

        Resume resume = resumeService.getResumeByUser(profileUser);
        
        User user = null;
        if (oauth2User != null) {
            user = userService.findUserFromOAuth2(oauth2User);
        }

        authService.validateAndAddAuthentication(model, oauth2User);

        boolean isOwner = false;
        if (oauth2User != null) {
            isOwner = profileUser != null && profileUser.equals(user);
            model.addAttribute("isOwner", isOwner);
        }

        List<Review> reviews = reviewService.getReviewsByListingIdWithAuthors(id);

        model.addAttribute("profileUser", profileUser);
        model.addAttribute("listings", listings);
        model.addAttribute("reviews", reviews);
        model.addAttribute("resume", resume);

        model.addAttribute("activePage", "profile");

        return "profile";
    }
}

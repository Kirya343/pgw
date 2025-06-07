package org.kirya343.main.controller;

import org.kirya343.main.model.Listing;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Controller
@RequestMapping("/listing")
@RequiredArgsConstructor
public class ListingsController {

    private final ListingService listingService;
    private final UserService userService;
    private final FavoriteListingService favoriteListingService;
    private final AuthService authService;
    private final ReviewService reviewService;


    @GetMapping("/{id}")
    public String getListing(@PathVariable Long id, Model model, @AuthenticationPrincipal OAuth2User oauth2User, Locale locale) {
        
        Listing listing = listingService.getListingByIdWithAuthorAndReviews(id);
        
        User user = null;
        if (oauth2User != null) {
            user = userService.findUserFromOAuth2(oauth2User);
        }

        User author = listing.getAuthor();

        authService.validateAndAddAuthentication(model, oauth2User);

        // 3. Локализация названия и описания
        listingService.localizeListing(listing, locale);

        if (user != author && user != null) {
            listing.setViews(listing.getViews() + 1);
            listingService.save(listing);
        }

        boolean isOwner = false;
        if (oauth2User != null) {
            isOwner = listing.getAuthor() != null && listing.getAuthor().equals(user);
            model.addAttribute("isOwner", isOwner);
        }
        boolean isFavorite = favoriteListingService.isFavorite(user, listing);

        // 7. Обновляем рейтинги
        updateRatingForListingAndUser(listing, author);

        List<Review> reviews = reviewService.getReviewsByListingIdWithAuthors(id);

        model.addAttribute("isFavorite", isFavorite);
        model.addAttribute("listing", listing);
        model.addAttribute("reviews", reviews);

        model.addAttribute("similarListings", listingService.findSimilarListings(listing.getCategory(), id, locale));

        return "listing";
    }
    @PostMapping("/{id}/favorite")
    public String toggleFavorite(@PathVariable Long id,
                                 @AuthenticationPrincipal OAuth2User oauth2User,
                                 @RequestHeader(value = "referer", required = false) String referer) {
        User user = userService.findUserFromOAuth2(oauth2User);
        Listing listing = listingService.getListingById(id);

        favoriteListingService.toggleFavorite(user, listing);
        return "redirect:" + (referer != null ? referer : "/listing/" + id);
    }
    
    @PostMapping("/{id}/review")
    public String addReview(@PathVariable Long id,
                            @RequestParam String text, // Текст отзыва
                            @RequestParam double rating, // Рейтинг отзыва
                            @AuthenticationPrincipal OAuth2User oauth2User,
                            @RequestHeader(value = "referer", required = false) String referer) {

        Listing listing = listingService.getListingById(id);
        if (listing == null) {
            return "redirect:/catalog"; // Если объявление не найдено, перенаправляем на каталог
        }

        User profile = listing.getAuthor();

        // Получаем текущего пользователя
        User user = userService.findUserFromOAuth2(oauth2User);

        if (user == profile) {
            return "redirect:" + (referer != null ? referer : "/listing/" + id); // Если пользователь оставляет отзыв себе, перенаправляем на каталог
        }

        // Проверяем, оставлял ли пользователь уже отзыв к этому объявлению
        boolean alreadyReviewed = reviewService.hasUserReviewedListing(user, listing);
        if (alreadyReviewed) {
            return "redirect:" + (referer != null ? referer : "/listing/" + id); // Если отзыв уже есть, просто редиректим обратно
        }

        // Создаем новый отзыв
        Review review = new Review();
        review.setText(text);
        review.setRating(rating);
        review.setCreatedAt(LocalDateTime.now());
        review.setListing(listing);
        review.setProfile(profile);
        review.setAuthor(user);

        // Сохраняем отзыв
        reviewService.saveReview(review);

        // Обновляем рейтинг объявления
        double newListingRating = reviewService.calculateAverageRatingForListing(listing.getId());
        listing.setAverageRating(newListingRating);
        listingService.save(listing);

        // Обновляем рейтинг пользователя (автора объявления)
        User author = listing.getAuthor();
        double newUserRating = reviewService.calculateAverageRatingForUser(author);
        author.setAverageRating(newUserRating);
        userService.save(author); // исправил: нужно сохранять `author`, а не `user`

        // Перенаправляем обратно на страницу объявления
        return "redirect:" + (referer != null ? referer : "/listing/" + id);
    }

    private void updateRatingForListingAndUser(Listing listing, User author) {
        double newListingRating = reviewService.calculateAverageRatingForListing(listing.getId());
        listing.setAverageRating(newListingRating);
        listingService.save(listing);
        
        double newUserRating = reviewService.calculateAverageRatingForUser(author);
        author.setAverageRating(newUserRating);
        userService.save(author);
    }
}
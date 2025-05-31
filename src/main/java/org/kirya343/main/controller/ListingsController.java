package org.kirya343.main.controller;

import org.kirya343.main.model.Listing;
import org.kirya343.main.model.Review;
import org.kirya343.main.model.User;
import org.kirya343.main.services.*;
import org.kirya343.main.services.components.AuthService;
import org.kirya343.main.services.components.AvatarService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/listing")
@RequiredArgsConstructor
public class ListingsController {

    private final ListingService listingService;
    private final UserService userService;
    private final AvatarService avatarService;
    private final FavoriteListingService favoriteListingService;
    private final AuthService authService;
    private final ReviewService reviewService;

    private void setLocalizedTitleAndDescription(Listing listing, Locale locale) {
        String title = null;
        String description = null;
        String language = locale.getLanguage();

        if ("fi".equals(language) && listing.getCommunityFi()) {
            title = listing.getTitleFi();
            description = listing.getDescriptionFi();
        } else if ("ru".equals(language) && listing.getCommunityRu()) {
            title = listing.getTitleRu();
            description = listing.getDescriptionRu();
        } else if ("en".equals(language) && listing.getCommunityEn()) {
            title = listing.getTitleEn();
            description = listing.getDescriptionEn();
        }

        if (title == null || description == null) {
            title = chooseTitle(listing);
            description = chooseDescription(listing);
        }

        listing.setLocalizedTitle(title);
        listing.setLocalizedDescription(description);
    }

    private String chooseTitle(Listing listing) {
        return listing.getTitleFi() != null ? listing.getTitleFi() :
                listing.getTitleRu() != null ? listing.getTitleRu() :
                        listing.getTitleEn();
    }

    private String chooseDescription(Listing listing) {
        return listing.getDescriptionFi() != null ? listing.getDescriptionFi() :
                listing.getDescriptionRu() != null ? listing.getDescriptionRu() :
                        listing.getDescriptionEn();
    }

    private void updateRatingForListingAndUser(Listing listing, User author) {
        double newListingRating = reviewService.calculateAverageRatingForListing(listing.getId());
        listing.setAverageRating(newListingRating);
        listingService.save(listing);

        double newUserRating = reviewService.calculateAverageRatingForUser(author);
        author.setAverageRating(newUserRating);
        userService.save(author);
    }


    @GetMapping("/{id}")
    public String getListing(@PathVariable Long id, Model model, @AuthenticationPrincipal OAuth2User oauth2User, Locale locale) {
        Listing listing = listingService.getListingByIdWithAuthorAndReviews(id);
        User user = null;

        if (oauth2User != null) {
            user = userService.findUserFromOAuth2(oauth2User);
        }

        if (listing == null) {
            return "redirect:/catalog";
        }

        authService.validateAndAddAuthentication(model, oauth2User);

        // 3. Локализация названия и описания
        setLocalizedTitleAndDescription(listing, locale);

        // Получаем автора объявления и его аватар
        User author = listing.getAuthor();

        if (user != author && user != null) {
            // Увеличиваем счетчик просмотров
            listing.setViews(listing.getViews() + 1);
            listingService.save(listing);
        }

        String authorAvatarPath = avatarService.resolveAvatarPath(author);

        boolean isOwner = false;
        if (oauth2User != null) {
            isOwner = listing.getAuthor() != null && listing.getAuthor().equals(user);
            model.addAttribute("isOwner", isOwner);
        }

        boolean isFavorite = favoriteListingService.isFavorite(user, listing);
        model.addAttribute("isFavorite", isFavorite);
        model.addAttribute("listing", listing);
        model.addAttribute("author", author.getName());
        model.addAttribute("authorAvatarPath", authorAvatarPath);
        model.addAttribute("createdAt", listing.getCreatedAt());
        model.addAttribute("category", listing.getCategory());

        // 7. Обновляем рейтинги
        updateRatingForListingAndUser(listing, author);

        List<Review> reviews = reviewService.getReviewsByListingIdWithAuthors(id);
        model.addAttribute("reviews", reviews);

        Map<Long, String> reviewAuthorAvatars = reviews.stream()
                .collect(Collectors.toMap(
                        Review::getId,
                        review -> {
                            User reviewAuthor = review.getAuthor();
                            return reviewAuthor != null ? avatarService.resolveAvatarPath(reviewAuthor) : "/images/avatar-placeholder.jpg";
                        }
                ));
        model.addAttribute("reviewAuthorAvatars", reviewAuthorAvatars);


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

        // Находим объявление по ID
        Listing listing = listingService.getListingById(id);
        if (listing == null) {
            return "redirect:/catalog"; // Если объявление не найдено, перенаправляем на каталог
        }

        // Получаем текущего пользователя
        User user = userService.findUserFromOAuth2(oauth2User);

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

}
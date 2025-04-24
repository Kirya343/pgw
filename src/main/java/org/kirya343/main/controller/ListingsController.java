package org.kirya343.main.controller;

import org.kirya343.main.model.Listing;
import org.kirya343.main.model.Review;
import org.kirya343.main.model.User;
import org.kirya343.main.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/listing")
public class ListingsController {

    private final ListingService listingService;
    private final UserService userService;
    private final AvatarService avatarService;
    private final FavoriteListingService favoriteListingService;
    private final ReviewService reviewService;

    @Autowired
    public ListingsController(ListingService listingService,
                              UserService userService,
                              AvatarService avatarService, FavoriteListingService favoriteListingService, ReviewService reviewService) {
        this.listingService = listingService;
        this.userService = userService;
        this.avatarService = avatarService;
        this.favoriteListingService = favoriteListingService;
        this.reviewService = reviewService;
    }

    @GetMapping("/{id}")
    public String getListing(@PathVariable Long id, Model model, @AuthenticationPrincipal OAuth2User oauth2User) {
        Listing listing = listingService.getListingById(id);
        User user = null;

        if (oauth2User != null) {
            user = userService.findUserFromOAuth2(oauth2User);
        }

        if (listing == null) {
            return "redirect:/catalog";
        }

        if (oauth2User != null) {
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
            // Проверяем, является ли пользователь автором объявления
            isOwner = listing.getAuthor() != null && listing.getAuthor().getId().equals(user.getId());

            model.addAttribute("isOwner", isOwner);
        }

        boolean isFavorite = favoriteListingService.isFavorite(user, listing);
        model.addAttribute("isFavorite", isFavorite);
        model.addAttribute("listing", listing);
        model.addAttribute("author", author != null ? author.getName() : "Неизвестный автор");
        model.addAttribute("authorAvatarPath", authorAvatarPath);
        model.addAttribute("createdAt", listing.getCreatedAt());
        model.addAttribute("category", listing.getCategory());

        double newListingRating = reviewService.calculateAverageRatingForListing(listing.getId());
        listing.setAverageRating(newListingRating);
        listingService.save(listing);

        double newUserRating = reviewService.calculateAverageRatingForUser(author);
        author.setAverageRating(newUserRating);
        userService.save(user);

        // Получаем все отзывы для объявления
        List<Review> reviews = reviewService.getReviewsByListingId(id);
        model.addAttribute("reviews", reviews);
        String reviewAuthorAvatarPath = "/images/avatar-placeholder.jpg"; // значение по умолчанию
        for (Review review : reviews) {
            User reviewAuthor = review.getAuthor();
            if (reviewAuthor != null) {
                reviewAuthorAvatarPath = avatarService.resolveAvatarPath(reviewAuthor);
            }
            model.addAttribute("reviewAuthorAvatarPath", reviewAuthorAvatarPath);  // добавляем для каждого отзыва
        }

        // Добавляем похожие объявления
        model.addAttribute("similarListings", listingService.findSimilarListings(listing.getCategory(), id));

        System.out.println("Listing features: " + listing.getFeatures());
        System.out.println("Listing images: " + listing.getImages());
        System.out.println("Listing reviews: " + listing.getReviews());
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

        // Проверяем, что пользователь аутентифицирован
        if (oauth2User == null) {
            return "redirect:/login"; // Если пользователь не авторизован, перенаправляем на страницу логина
        }

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
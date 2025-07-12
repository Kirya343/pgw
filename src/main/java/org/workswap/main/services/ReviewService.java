package org.workswap.main.services;

import org.springframework.stereotype.Service;
import org.workswap.datasource.main.model.Listing;
import org.workswap.datasource.main.model.Review;
import org.workswap.datasource.main.model.User;
import org.workswap.datasource.main.repository.ReviewRepository;

import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ListingService listingService;

    // Метод для сохранения отзыва
    public Review saveReview(Review review) {
        return reviewRepository.save(review); // Сохраняем отзыв в базе данных
    }

    // Метод для получения всех отзывов по ID объявления
    public List<Review> getReviewsByListingId(Long listingId) {
        return reviewRepository.findByListingIdOrderByCreatedAtDesc(listingId); // Получаем отзывы для объявления
    }

    public List<Review> getReviewsByProfileId(Long profileId) {
        return reviewRepository.findByProfileIdOrderByCreatedAtDesc(profileId); // Получаем отзывы для объявления
    }

    public boolean hasUserReviewedListing(User user, Listing listing) {
        return reviewRepository.existsByAuthorAndListing(user, listing);
    }

    public boolean hasUserReviewedProfile(User user, User profile) {
        return reviewRepository.existsByAuthorAndProfile(user, profile);
    }

    public double calculateAverageRatingForListing(Long listingId) {
        List<Review> reviews = reviewRepository.findByListingIdOrderByCreatedAtDesc(listingId);

        if (reviews.isEmpty()) {
            return 0;  // Если нет отзывов, возвращаем 0
        }

        double totalRating = 0;
        for (Review review : reviews) {
            totalRating += review.getRating();
        }

        return totalRating / reviews.size();  // Средний рейтинг
    }

    public List<Review> getReviewsByListingIdWithAuthors(Long listingId) {
        return reviewRepository.findByListingIdWithAuthors(listingId);
    }

    public List<Review> getReviewsByProfileIdWithAuthors(Long profileId) {
        return reviewRepository.findByProfileIdWithAuthors(profileId);
    }

    public double calculateAverageRatingForUser(User user) {
        List<Listing> listings = listingService.getListingsByUser(user);  // Получаем все объявления пользователя

        List<Review> listingReviews = listings.stream()
            .flatMap(listing -> getReviewsByListingId(listing.getId()).stream())
            .collect(Collectors.toList());

        List<Review> profileReviews = getReviewsByProfileId(user.getId());

        List<Review> allReviews = new ArrayList<>();
        allReviews.addAll(listingReviews);
        allReviews.addAll(profileReviews);

        if (listings.isEmpty()) {
            return 0;  // Если у пользователя нет объявлений, возвращаем 0
        }

        double totalRating = 0;
        int totalReviews = 0;

        for (Review review : allReviews) {
            double rating = review.getRating();  // Получаем рейтинг для каждого объявления

            totalRating += rating;
            totalReviews++;
        }

        // Если есть хотя бы одно объявление с рейтингом больше 1, считаем средний рейтинг
        if (totalReviews > 0) {
        double average = totalRating / totalReviews;
        return Math.round(average * 10.0) / 10.0;  // округление до 1 знака после запятой
        } else {
            return 0.0;
        }
    }
}

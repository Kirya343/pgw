package org.kirya343.main.services;

import org.kirya343.main.model.Listing;
import org.kirya343.main.model.Review;
import org.kirya343.main.model.User;
import org.kirya343.main.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ListingService listingService;

    @Autowired
    public ReviewService(ReviewRepository reviewRepository, ListingService listingService) {
        this.reviewRepository = reviewRepository;
        this.listingService = listingService;
    }

    // Метод для сохранения отзыва
    public Review saveReview(Review review) {
        return reviewRepository.save(review); // Сохраняем отзыв в базе данных
    }

    // Метод для получения всех отзывов по ID объявления
    public List<Review> getReviewsByListingId(Long listingId) {
        return reviewRepository.findByListingIdOrderByCreatedAtDesc(listingId); // Получаем отзывы для объявления
    }

    public boolean hasUserReviewedListing(User user, Listing listing) {
        return reviewRepository.existsByAuthorAndListing(user, listing);
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

    public double calculateAverageRatingForUser(User user) {
        List<Listing> listings = listingService.getListingsByUser(user);  // Получаем все объявления пользователя

        if (listings.isEmpty()) {
            return 0;  // Если у пользователя нет объявлений, возвращаем 0
        }

        double totalRating = 0;
        int totalListings = 0;

        for (Listing listing : listings) {
            double listingRating = calculateAverageRatingForListing(listing.getId());  // Получаем рейтинг для каждого объявления

            // Учитываем только объявления с рейтингом больше 1
            if (listingRating > 0) {
                totalRating += listingRating;
                totalListings++;
            }
        }

        // Если есть хотя бы одно объявление с рейтингом больше 1, считаем средний рейтинг
        if (totalListings > 0) {
            return totalRating / totalListings;  // Средний рейтинг пользователя
        } else {
            return 0;  // Если у пользователя нет объявлений с рейтингом > 1, возвращаем 0
        }
    }
}

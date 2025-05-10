package org.kirya343.main.services;

import org.kirya343.main.model.Listing;
import org.kirya343.main.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Locale;

public interface ListingService {
    Page<Listing> findByCategory(String category, Pageable pageable);
    List<Listing> getListingsByUser(User user); // добавили метод
    void save(Listing listing);
    List<Listing> findByUserEmail(String email);
    List<Listing> getAllActiveListings(); // Предполагая, что у вас есть поле `active`
    List<Listing> getAllListings();// в сущности
    Page<Listing> findActiveByCategory(String category, Pageable pageable);
    Listing getListingById(Long id);
    Listing getListingByIdWithAuthorAndReviews(Long id);
    List<Listing> findSimilarListings(String category, Long excludeId, Locale locale);
    void deleteListing(Long id);
    Page<Listing> findActiveByCategoryAndCommunity(String community, String category, Pageable pageable);
    List<Listing> getRecentListings(int count);
}

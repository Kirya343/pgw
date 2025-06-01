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

    // Методы получения объявлений
    List<Listing> findByUserEmail(String email);
    List<Listing> getAllActiveListings(); // Предполагая, что у вас есть поле `active`
    List<Listing> getAllListings();// в сущности
    Page<Listing> findActiveByCategory(String category, Pageable pageable);
    Listing getListingById(Long id);
    Listing getListingByIdWithAuthorAndReviews(Long id);
    Page<Listing> findActiveByCategoryAndCommunity(String community, String category, Pageable pageable);
    List<Listing> getRecentListings(int count);
    Page<Listing> getListingsSorted(String category, String sortBy, Pageable pageable, String searchQuery, boolean hasReviews, Locale locale);
    Page<Listing> findListingsByCategoryAndCommunity(String category, Locale locale, Pageable pageable);

    // Методы для работы с похожими объявлениями
    List<Listing> findSimilarListings(String category, Long excludeId, Locale locale);

    // Методы для работы с объявлениями
    void deleteListing(Long id);
    void save(Listing listing);
    Listing saveAndReturn(Listing listing);

    List<Listing> searchListings(String searchQuery);
}

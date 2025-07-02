package org.kirya343.main.services;

import java.util.List;
import java.util.Locale;

import org.kirya343.main.model.DTOs.ListingDTO;
import org.kirya343.main.model.Category;
import org.kirya343.main.model.Listing;
import org.kirya343.main.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ListingService {
    Page<Listing> findByCategory(String category, Pageable pageable);
    List<Listing> getListingsByUser(User user); // добавили метод

    // Методы получения объявлений
    List<Listing> findByUserEmail(String email);
    List<Listing> getAllActiveListings(); // Предполагая, что у вас есть поле `active`
    List<Listing> getAllListings();// в сущности
    List<Listing> getActiveListingsByUser(User user);
    Page<Listing> findActiveByCategory(String category, Pageable pageable);
    Listing getListingById(Long id);
    Listing getListingByIdWithAuthorAndReviews(Long id);
    Page<Listing> findActiveByCategoryAndCommunity(String community, Category category, Pageable pageable);
    Page<Listing> findActiveByCommunity(String community, Pageable pageable);
    List<Listing> getRecentListings(int count);
    Page<Listing> getListingsSorted(Category category, String sortBy, Pageable pageable, String searchQuery, boolean hasReviews, Locale locale);

    // Методы для работы с похожими объявлениями
    List<Listing> findSimilarListings(Category category, Long excludeId, Locale locale);

    ListingDTO getListingDtoById(Long id);

    // Методы для работы с объявлениями
    void deleteListing(Long id);
    void save(Listing listing);
    Listing saveAndReturn(Listing listing);
    void localizeListing(Listing listing, Locale locale);
    void localizeListingIfLangPass(Listing listing, Locale locale);

    List<Listing> searchListings(String searchQuery);

    // Метод для локализации объявлений пользователя в аккаунте
    List<Listing> localizeAccountListings(User user, Locale locale);
    List<Listing> localizeActiveAccountListings(User user, Locale locale);
    List<Listing> localizeFavoriteListings(User user, Locale locale);
    List<Listing> localizeCatalogListings(List<Listing> listings, Locale locale);

    Page<Listing> findListingsByCategoryAndCommunity(Category category, Locale locale, Pageable pageable);
}

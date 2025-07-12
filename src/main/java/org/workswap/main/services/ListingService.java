package org.workswap.main.services;

import java.util.List;
import java.util.Locale;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.workswap.datasource.main.model.Listing;
import org.workswap.datasource.main.model.User;
import org.workswap.datasource.main.model.DTOs.ListingDTO;
import org.workswap.datasource.main.model.listingModels.Category;
import org.workswap.datasource.main.model.listingModels.Location;

public interface ListingService {

    Listing findListing(String param, String paramType);

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

    List<Listing> findByCategory(Category category);
    List<Listing> findByLocation(Location location);

    List<Listing> findActiveByCommunity(String community);
    List<Listing> getRecentListings(int count);
    Page<Listing> getListingsSorted(Category category, String sortBy, Pageable pageable, Location location, String searchQuery, boolean hasReviews, List<String> languages);

    // Методы для работы с похожими объявлениями
    List<Listing> findSimilarListings(Category category, Long excludeId, Locale locale);

    // Методы для работы с объявлениями
    void deleteListing(Long id);
    void save(Listing listing);
    Listing saveAndReturn(Listing listing);
    void localizeListing(Listing listing, Locale locale);

    List<Listing> searchListings(String searchQuery);

    // Метод для локализации объявлений пользователя в аккаунте
    List<Listing> localizeAccountListings(User user, Locale locale);
    List<Listing> localizeActiveAccountListings(User user, Locale locale);
    List<Listing> localizeFavoriteListings(User user, Locale locale);
    List<Listing> localizeCatalogListings(List<Listing> listings, Locale locale);

    // Конвертация в дто для чата
    ListingDTO convertToDTO(Listing listing, Locale locale);
}

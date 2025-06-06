package org.kirya343.main.services.impl;

import org.kirya343.main.model.FavoriteListing;
import org.kirya343.main.model.Listing;
import org.kirya343.main.model.User;
import org.kirya343.main.model.chat.Conversation;
import org.kirya343.main.repository.ConversationRepository;
import org.kirya343.main.repository.ListingRepository;
import org.kirya343.main.services.FavoriteListingService;
import org.kirya343.main.services.ListingService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ListingServiceImpl implements ListingService {

    private final ListingRepository listingRepository;
    private final ConversationRepository conversationRepository;
    private final FavoriteListingService favoriteListingService;

    @Override
    public Page<Listing> findByCategory(String category, Pageable pageable) {
        return listingRepository.findByCategory(category, pageable);
    }

    @Override
    public List<Listing> getRecentListings(int count) {
        Pageable pageable = PageRequest.of(0, count);
        return listingRepository.findAllByOrderByCreatedAtDesc(pageable).getContent();
    }


    @Override
    public List<Listing> getListingsByUser(User user) {
        return listingRepository.findByAuthor(user);
    }

    @Override
    public List<Listing> getActiveListingsByUser(User user) {
        return listingRepository.findByAuthorAndActiveTrue(user);
    } 

    @Override
    public void save(Listing listing) {
        listingRepository.save(listing);
    }
    
    @Override
    public Listing saveAndReturn(Listing listing) {
        return listingRepository.save(listing);
    }

    @Override
    public Page<Listing> findActiveByCategory(String category, Pageable pageable) {
        return listingRepository.findActiveByCategory(category, pageable);
    }

    @Override
    public List<Listing> findByUserEmail(String email) {
        return listingRepository.findByAuthorEmail(email);
    }

    @Override
    public List<Listing> getAllActiveListings() {
        return listingRepository.findByActiveTrue(); // Предполагая, что у вас есть поле `active` в сущности
    }

    @Override
    public List<Listing> getAllListings() {
        return listingRepository.findAll(); // Просто получаем все объявления
    }
    // Новый метод с JOIN FETCH (оптимизированный)
    @Override
    public Listing getListingByIdWithAuthorAndReviews(Long id) {
        return listingRepository.findByIdWithAuthorAndReviews(id).orElse(null);
    }

    // Оставляем стандартный тоже, если вдруг понадобится
    @Override
    public Listing getListingById(Long id) {
        return listingRepository.findById(id).orElse(null);
    }

    @Override
    public List<Listing> findSimilarListings(String category, Long excludeId, Locale locale) {
        List<Listing> listings = listingRepository.findByCategoryAndIdNot(category, excludeId, PageRequest.of(0, 4));

        // Фильтруем по локали
        String lang = locale.getLanguage();
        return listings.stream()
                .filter(listing -> {
                    if ("fi".equals(lang)) {
                        return Boolean.TRUE.equals(listing.getCommunityFi());
                    } else if ("ru".equals(lang)) {
                        return Boolean.TRUE.equals(listing.getCommunityRu());
                    } else if ("en".equals(lang)) {
                        return Boolean.TRUE.equals(listing.getCommunityEn());
                    } else {
                        return true; // если язык неизвестен — показываем всё
                    }
                })
                .collect(Collectors.toList());
    }

    @Override
    public void deleteListing(Long id) {
        Listing listing = listingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Объявление не найдено"));

        // Обнуляем связь listing у всех Conversation
        List<Conversation> conversations = conversationRepository.findAllByListing(listing);
        for (Conversation conversation : conversations) {
            conversation.setListing(null);
        }
        conversationRepository.saveAll(conversations);

        // Теперь можно удалить объявление
        listingRepository.delete(listing);
    }

    @Override
    public Page<Listing> findActiveByCategoryAndCommunity(String community, String category, Pageable pageable) {
        switch (community) {
            case "fi":
                return listingRepository.findByCategoryAndCommunityFiTrueAndActiveTrue(category, pageable);
            case "ru":
                return listingRepository.findByCategoryAndCommunityRuTrueAndActiveTrue(category, pageable);
            case "en":
                return listingRepository.findByCategoryAndCommunityEnTrueAndActiveTrue(category, pageable);
            default:
                return listingRepository.findByCategoryAndActiveTrue(category, pageable); // Для всех остальных языков
        }
    }

    @Override
    public Page<Listing> getListingsSorted(String category, String sortBy, Pageable pageable, String searchQuery, boolean hasReviews, Locale locale) {
    Sort sort;

    switch (sortBy) {
        case "price":
            sort = Sort.by(Sort.Direction.ASC, "price");
            break;
        case "rating":
            sort = Sort.by(Sort.Direction.DESC, "rating");
            break;
        case "popularity":
            sort = Sort.by(Sort.Direction.DESC, "views");
            break;
        case "date":
        default:
            sort = Sort.by(Sort.Direction.DESC, "createdAt");
            break;
    }

    Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);

    // Получаем все объявления по категории и языковой аудитории
    Page<Listing> baseListings = (category != null && (
                category.equals("offer-service") || category.equals("find-help") || category.equals("product")))
                ? findListingsByCategoryAndCommunity(category, locale, sortedPageable)
                : findListingsByCategoryAndCommunity("services", locale, sortedPageable);

        // Фильтруем по поиску
        List<Listing> filtered = baseListings.getContent();

        if (searchQuery != null && !searchQuery.isBlank()) {
            String lowered = searchQuery.toLowerCase();
            filtered = filtered.stream()
                    .filter(l ->
                            (l.getTitleRu() != null && l.getTitleRu().toLowerCase().contains(lowered)) ||
                            (l.getDescriptionRu() != null && l.getDescriptionRu().toLowerCase().contains(lowered)) ||
                            (l.getTitleFi() != null && l.getTitleFi().toLowerCase().contains(lowered)) ||
                            (l.getDescriptionFi() != null && l.getDescriptionFi().toLowerCase().contains(lowered)) ||
                            (l.getTitleEn() != null && l.getTitleEn().toLowerCase().contains(lowered)) ||
                            (l.getDescriptionEn() != null && l.getDescriptionEn().toLowerCase().contains(lowered)) ||
                            (l.getLocation() != null && l.getLocation().toLowerCase().contains(lowered))
                    )
                    .collect(Collectors.toList());
        }

        // Фильтруем по наличию отзывов
        if (hasReviews) {
            filtered = filtered.stream()
                    .filter(l -> l.getReviews() != null && !l.getReviews().isEmpty())
                    .collect(Collectors.toList());
        }

        // Возвращаем постранично вручную
        int start = (int) sortedPageable.getOffset();
        int end = Math.min(start + sortedPageable.getPageSize(), filtered.size());

        List<Listing> pageContent = (start <= end) ? filtered.subList(start, end) : List.of();
        return new PageImpl<>(pageContent, sortedPageable, filtered.size());
    }

    @Override
    public Page<Listing> findListingsByCategoryAndCommunity(String category, Locale locale, Pageable pageable) {
        // В зависимости от языка выбираем нужное комьюнити
        if ("fi".equals(locale.getLanguage())) {
            return findActiveByCategoryAndCommunity("fi", category, pageable);
        } else if ("ru".equals(locale.getLanguage())) {
            return findActiveByCategoryAndCommunity("ru", category, pageable);
        } else if ("en".equals(locale.getLanguage())) {
            return findActiveByCategoryAndCommunity("en", category, pageable);
        } else {
            return findActiveByCategory(category, pageable); // для других языков
        }
    }

    @Override
    public List<Listing> searchListings(String searchQuery) {
        String query = "%" + searchQuery.toLowerCase() + "%";
        
        return listingRepository.searchAllFields(query);
    }

    @Override
    public List<Listing> localizeAccountListings(User user, Locale locale) {
        List<Listing> listings = getListingsByUser(user);
        System.out.println("Got locale: " + locale);

        for (Listing listing : listings) {
            localizeListing(listing, locale);
        }

        System.out.println("Объявлений: " + listings.size());
        return listings;
    }

    @Override
    public List<Listing> localizeActiveAccountListings(User user, Locale locale) {
        List<Listing> listings = getActiveListingsByUser(user);
        System.out.println("Got locale: " + locale);

        for (Listing listing : listings) {
            localizeListing(listing, locale);
        }

        System.out.println("Объявлений: " + listings.size());
        return listings;
    }

    @Override
    public List<Listing> localizeFavoriteListings(User user, Locale locale) {
        List<FavoriteListing> favoriteListings = favoriteListingService.findByUser(user);
        List<Listing> listings = favoriteListings.stream()
                .map(FavoriteListing::getListing)
                .collect(Collectors.toList());

        for (Listing listing : listings) {
            localizeListing(listing, locale);
        }

        System.out.println("Объявлений: " + listings.size());
        return listings;
    }

    @Override
    public void localizeListing(Listing listing, Locale locale) {
        String title = null;
        String description = null;

        String lang = locale.getLanguage();

        // 1. Основной язык
        if ("fi".equals(lang) && listing.getCommunityFi()) {
            title = safe(listing.getTitleFi());
            description = safe(listing.getDescriptionFi());
        } else if ("ru".equals(lang) && listing.getCommunityRu()) {
            title = safe(listing.getTitleRu());
            description = safe(listing.getDescriptionRu());
        } else if ("en".equals(lang) && listing.getCommunityEn()) {
            title = safe(listing.getTitleEn());
            description = safe(listing.getDescriptionEn());
        }

        // 2. Fallback — проверяем другие языки
        if (isBlank(title) || isBlank(description)) {
            if (isBlank(title)) {
                if (!isBlank(listing.getTitleFi())) title = listing.getTitleFi();
                else if (!isBlank(listing.getTitleRu())) title = listing.getTitleRu();
                else if (!isBlank(listing.getTitleEn())) title = listing.getTitleEn();
            }

            if (isBlank(description)) {
                if (!isBlank(listing.getDescriptionFi())) description = listing.getDescriptionFi();
                else if (!isBlank(listing.getDescriptionRu())) description = listing.getDescriptionRu();
                else if (!isBlank(listing.getDescriptionEn())) description = listing.getDescriptionEn();
            }
        }

        listing.setLocalizedTitle(title);
        listing.setLocalizedDescription(description);
    }

    private String safe(String value) {
    return (value != null && !value.isBlank()) ? value : null;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}

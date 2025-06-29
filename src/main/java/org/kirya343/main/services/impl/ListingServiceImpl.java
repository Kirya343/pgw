package org.kirya343.main.services.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.kirya343.main.controller.mappers.ListingMapper;
import org.kirya343.main.model.DTOs.ListingDTO;
import org.kirya343.main.model.FavoriteListing;
import org.kirya343.main.model.Listing;
import org.kirya343.main.model.User;
import org.kirya343.main.model.chat.Conversation;
import org.kirya343.main.model.listingModels.ListingTranslation;
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

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ListingServiceImpl implements ListingService {

    private final ListingRepository listingRepository;
    private final ConversationRepository conversationRepository;
    private final FavoriteListingService favoriteListingService;
    private final ListingMapper listingMapper;
    
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
        return listingRepository.findListByActiveTrue(); // Предполагая, что у вас есть поле `active` в сущности
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

    @Override // Переписать, он не универсальный
    public List<Listing> findSimilarListings(String category, Long excludeId, Locale locale) {
        String lang = locale.getLanguage();

        // Если язык неизвестен — не фильтруем по сообществам, берем просто по категории и id
        if (!List.of("fi", "ru", "en", "it").contains(lang)) {
            return listingRepository.findByCategoryAndIdNot(category, excludeId, PageRequest.of(0, 4));
        }

        return listingRepository.findTop4ByCategoryAndIdNotAndCommunity(
            category,
            excludeId,
            lang,
            PageRequest.of(0, 4)
        );
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
        return listingRepository.findByCategoryAndLanguageAndActiveTrue(category, community, pageable);
    }

    @Override
    public Page<Listing> findActiveByCommunity(String community, Pageable pageable) {
        System.out.println("Finding active listings by community: " + community);
        if (community == null || community.isBlank()) {
            return listingRepository.findPageByActiveTrue(pageable); // Если язык не указан — просто все активные
        }
        return listingRepository.findByCommunityAndActiveTrue(community.toLowerCase(), pageable);
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

        String lang = locale.getLanguage();

        // Получаем все объявления по категории (если указана) и языковой аудитории
        Page<Listing> baseListings;
        if (category == null || category.isEmpty() || category.equalsIgnoreCase("all")) {
            baseListings = findActiveByCommunity(lang, sortedPageable);
        } else if (category.equals("services") || category.equals("all-services")) {
            // Для категории "services" и "all-services" показываем все услуги
            baseListings = findListingsByCategoryAndCommunity("offer-service", locale, sortedPageable);
            // Добавляем запросы услуг к результатам
            Page<Listing> requestServices = findListingsByCategoryAndCommunity("find-service", locale, sortedPageable);
            List<Listing> combined = new ArrayList<>();
            combined.addAll(baseListings.getContent());
            combined.addAll(requestServices.getContent());
            // Сортируем объединенный список согласно выбранной сортировке
            combined.sort((l1, l2) -> {
                switch (sortBy) {
                    case "price":
                        return Double.compare(l1.getPrice(), l2.getPrice());
                    case "rating":
                        return Double.compare(l2.getRating(), l1.getRating());
                    case "popularity":
                        return Integer.compare(l2.getViews(), l1.getViews());
                    default: // "date" или любой другой случай
                        return l2.getCreatedAt().compareTo(l1.getCreatedAt());
                }
            });
            // Возвращаем постранично
            int start = (int) sortedPageable.getOffset();
            int end = Math.min(start + sortedPageable.getPageSize(), combined.size());
            List<Listing> pageContent = (start <= end) ? combined.subList(start, end) : List.of();
            baseListings = new PageImpl<>(pageContent, sortedPageable, combined.size());
        } else {
            // Для конкретных категорий ("offer-service", "request-service", "product")
            baseListings = findListingsByCategoryAndCommunity(category, locale, sortedPageable);
        }

        // Фильтруем по поиску
        List<Listing> filtered = baseListings.getContent();

        if (searchQuery != null && !searchQuery.isBlank()) {
            String lowered = searchQuery.toLowerCase();
            filtered = filtered.stream()
                .filter(listing -> {
                    // Получаем перевод по locale
                    ListingTranslation translation = listing.getTranslations().get(locale.getLanguage());

                    return (translation != null && (
                            (translation.getTitle() != null && translation.getTitle().toLowerCase().contains(lowered)) ||
                            (translation.getDescription() != null && translation.getDescription().toLowerCase().contains(lowered))
                    )) || (listing.getLocation() != null && listing.getLocation().getName().toLowerCase().contains(lowered));
                })
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
        String community = locale.getLanguage();
        return findActiveByCategoryAndCommunity(community, category, pageable);
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

        return listings;
    }

    @Override
    public List<Listing> localizeCatalogListings(List<Listing> listings, Locale locale) {
        for (Listing listing : listings) {
            localizeListingIfLangPass(listing, locale);
        }

        return listings;
    }

    @Override
    public void localizeListing(Listing listing, Locale locale) {
        String lang = locale.getLanguage();
        Map<String, ListingTranslation> translations = listing.getTranslations();

        ListingTranslation selected = translations.get(lang);

        // fallback, если нужного языка нет
        if (selected == null || isBlank(selected.getTitle()) || isBlank(selected.getDescription())) {
            // Приоритет: fi > ru > en
            for (String fallbackLang : List.of("fi", "ru", "en", "it")) {
                selected = translations.get(fallbackLang);
                if (selected != null && !isBlank(selected.getTitle()) && !isBlank(selected.getDescription())) {
                    break;
                }
            }
        }

        if (selected != null) {
            listing.setLocalizedTitle(safe(selected.getTitle()));
            listing.setLocalizedDescription(safe(selected.getDescription()));
        } else {
            listing.setLocalizedTitle(null);
            listing.setLocalizedDescription(null);
        }
    }

    @Override
    public void localizeListingIfLangPass(Listing listing, Locale locale) {
        String lang = locale.getLanguage();
        Map<String, ListingTranslation> translations = listing.getTranslations();

        ListingTranslation selected = translations.get(lang);

        // fallback, если нужного языка нет
        if (selected == null || isBlank(selected.getTitle()) || isBlank(selected.getDescription())) {
            // Приоритет: fi > ru > en
            for (String fallbackLang : List.of("fi", "ru", "en")) {
                selected = translations.get(fallbackLang);
                if (selected != null && !isBlank(selected.getTitle()) && !isBlank(selected.getDescription())) {
                    break;
                }
            }
        }

        if (selected != null) {
            listing.setLocalizedTitle(safe(selected.getTitle()));
            listing.setLocalizedDescription(safe(selected.getDescription()));
        } else {
            listing.setLocalizedTitle(null);
            listing.setLocalizedDescription(null);
        }
    }

    private String safe(String value) {
    return (value != null && !value.isBlank()) ? value : null;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    @Transactional
    public ListingDTO getListingDtoById(Long id) {
        Listing listing = listingRepository.findWithTranslationsById(id).orElseThrow();
        Map<String, ListingTranslation> translations = listing.getTranslations(); // теперь работает
        return listingMapper.convertToDTO(listing, translations);
    }
}
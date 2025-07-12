package org.workswap.main.services.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import org.workswap.config.LocalisationConfig.LanguageUtils;
import org.workswap.datasource.main.model.FavoriteListing;
import org.workswap.datasource.main.model.Listing;
import org.workswap.datasource.main.model.User;
import org.workswap.datasource.main.model.DTOs.ListingDTO;
import org.workswap.datasource.main.model.ModelsSettings.SearchParamType;
import org.workswap.datasource.main.model.chat.Conversation;
import org.workswap.datasource.main.model.listingModels.Category;
import org.workswap.datasource.main.model.listingModels.ListingTranslation;
import org.workswap.datasource.main.model.listingModels.Location;
import org.workswap.datasource.main.repository.ConversationRepository;
import org.workswap.datasource.main.repository.ListingRepository;
import org.workswap.main.services.CategoryService;
import org.workswap.main.services.FavoriteListingService;
import org.workswap.main.services.ListingService;
import org.workswap.main.services.LocationService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ListingServiceImpl implements ListingService {

    private final ListingRepository listingRepository;
    private final ConversationRepository conversationRepository;
    private final FavoriteListingService favoriteListingService;
    private final CategoryService categoryService;
    private final LocationService locationService;
    private static final Logger logger = LoggerFactory.getLogger(ListingService.class);
    
    @Override 
    public Listing findListing(String param, String paramType) {
        SearchParamType searchParamType = SearchParamType.valueOf(paramType);
        switch (searchParamType) {
            case ID:
                return listingRepository.findById(Long.parseLong(param)).orElse(null);
            default:
                throw new IllegalArgumentException("Unknown param type: " + paramType);
        }
    }

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
    public List<Listing> findSimilarListings(Category category, Long excludeId, Locale locale) {
        String lang = locale.getLanguage();

        // Если язык неизвестен — не фильтруем по сообществам, берем просто по категории и id
        if (!LanguageUtils.SUPPORTED_LANGUAGES.contains(lang)) {
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
    public List<Listing> findByCategory(Category category) {
        List<Category> categories = categoryService.getAllDescendants(category);
        List<Listing> listings = listingRepository.findByCategories(categories);
        logger.info("Найдены объявления: " + listings.size());
        return listings;
    }

    @Override
    public List<Listing> findByLocation(Location location) {
        List<Location> locations = locationService.getAllDescendants(location);
        List<Listing> listings = listingRepository.findByLocations(locations);
        logger.info("Найдены объявления: " + listings.size());
        return listings;
    }

    @Override
    public List<Listing> findActiveByCommunity(String community) {
        List<Listing> listings = listingRepository.findByCommunityAndActiveTrue(community.toLowerCase());
        logger.info("Найдены активные объявления: " + listings.size());
        return listings;
    }

    @Override
    public Page<Listing> getListingsSorted(Category category, String sortBy, Pageable pageable, Location location, String searchQuery, boolean hasReviews, List<String> languages) {
        Sort sort;

        logger.info("[GetListingsSorted] Языки для поиска: " + languages);

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

        List<Listing> listingsByLanguages = new ArrayList<>();
        Set<Long> addedIds = new HashSet<>(); // для отслеживания уже добавленных объявлений

        for (String lang : languages) {
            logger.info("Поиск без категории по языку: " + lang);
            List<Listing> found = findActiveByCommunity(lang);
            for (Listing listing : found) {
                if (addedIds.add(listing.getId())) { // добавляется только если ID ещё не было
                    listingsByLanguages.add(listing);
                }
            }
        }

        logger.info("Объявления по языку: " + listingsByLanguages.size());

        // Фильтруем по категории

        List<Listing> filteredByCategory = listingsByLanguages;
        if (category != null) {
            List<Listing> categoryResults = findByCategory(category);
            filteredByCategory.retainAll(categoryResults); // оставляем только те, которые есть и там, и там
        }

        logger.info("Объявления прошли фильтр категории: " + listingsByLanguages.size());

        // Фильтруем по локации

        List<Listing> filteredByLocation = filteredByCategory;
        if (location != null) {
            List<Listing> locationResults = findByLocation(location);
            filteredByLocation.retainAll(locationResults); // оставляем только те, которые есть и там, и там
        }

        logger.info("Объявления прошли фильтр локации: " + listingsByLanguages.size());

        // Фильтруем по поиску

        List<Listing> filteredBySearch = filteredByLocation;
        if (searchQuery != null && !searchQuery.isBlank()) {
            List<Listing> searchResults = searchListings(searchQuery);
            filteredBySearch.retainAll(searchResults); // оставляем только те, которые есть и там, и там
        }

        logger.info("Объявления прошли фильтр поиска: " + listingsByLanguages.size());

        // Фильтруем по наличию отзывов

        if (hasReviews) {
            filteredBySearch = filteredBySearch.stream()
                    .filter(l -> l.getReviews() != null && !l.getReviews().isEmpty())
                    .collect(Collectors.toList());
        }

        logger.info("Объявления прошли фильтр наличия отзывов: " + listingsByLanguages.size());

        // Возвращаем постранично вручную
        int start = (int) sortedPageable.getOffset();
        int end = Math.min(start + sortedPageable.getPageSize(), filteredBySearch.size());

        List<Listing> pageContent = (start <= end) ? filteredBySearch.subList(start, end) : List.of();
        Page<Listing> sortedListings = new PageImpl<>(pageContent, sortedPageable, filteredBySearch.size());
        logger.info("Отфильтрованные объявления: " + sortedListings.getNumberOfElements());

        int listingCounter = 0;
        logger.info("===== Список объявлений переданных в страницу: " + pageContent.size() + " =====");
        for (Listing listing : pageContent) {
            listingCounter++;
            localizeListing(listing, Locale.of("ru"));
            logger.info("Объявление (" + listingCounter + "/" + pageContent.size() + "): " + listing.getLocalizedTitle());
        }

        listingCounter = 0;
        logger.info("===== Страница объявлений: " + sortedListings.getNumberOfElements() + " =====");
        for (Listing listing : sortedListings) {
            listingCounter++;
            localizeListing(listing, Locale.of("ru"));
            logger.info("Объявление (" + listingCounter + "/" + sortedListings.getNumberOfElements() + "): " + listing.getLocalizedTitle());
        }
        return sortedListings;
    }

    @Override
    public List<Listing> searchListings(String searchQuery) {
        String query = "%" + searchQuery.toLowerCase() + "%";
        
        return listingRepository.searchAllFields(query);
    }

    @Override
    public List<Listing> localizeAccountListings(User user, Locale locale) {
        List<Listing> listings = getListingsByUser(user);
        logger.info("Got locale: " + locale);

        for (Listing listing : listings) {
            localizeListing(listing, locale);
        }

        logger.info("Объявлений: " + listings.size());
        return listings;
    }

    @Override
    public List<Listing> localizeActiveAccountListings(User user, Locale locale) {
        List<Listing> listings = getActiveListingsByUser(user);
        logger.info("Got locale: " + locale);

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
            localizeListing(listing, locale);
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
            for (String fallbackLang : LanguageUtils.SUPPORTED_LANGUAGES) {
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

    @Override
    @Transactional
    public ListingDTO convertToDTO(Listing listing, Locale locale) {
        if (listing == null) {
            return null;
        }

        ListingDTO dto = new ListingDTO();
        dto.setId(listing.getId());
        dto.setPrice(listing.getPrice());
        dto.setPriceType(listing.getPriceType());
        dto.setCategory(listing.getCategory().getName());
        dto.setLocation(listing.getLocation().getName());
        dto.setRating(listing.getAverageRating());
        dto.setViews(listing.getViews());
        dto.setCreatedAt(listing.getCreatedAt());
        dto.setActive(listing.isActive());
        dto.setImagePath(listing.getImagePath());

        localizeListing(listing, locale);

        dto.setLocalizedTitle(listing.getLocalizedTitle());
        dto.setLocalizedDescription(listing.getLocalizedDescription());

        return dto;
    }
}
package org.kirya343.main.services.impl;

import org.kirya343.main.model.Listing;
import org.kirya343.main.model.User;
import org.kirya343.main.model.chat.Conversation;
import org.kirya343.main.repository.ConversationRepository;
import org.kirya343.main.repository.ListingRepository;
import org.kirya343.main.services.ListingService;
import org.springframework.data.domain.Page;
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
    public Page<Listing> getListingsSorted(String category, String sortBy, Pageable pageable, Locale locale) {
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

        if (category != null) {
            switch (category) {
                case "offer-service":
                case "find-help":
                case "product":
                    return findListingsByCategoryAndCommunity(category, locale, sortedPageable);
                default:
                    return findListingsByCategoryAndCommunity("services", locale, sortedPageable);
            }
        }

        return findListingsByCategoryAndCommunity("services", locale, sortedPageable);
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
}

package org.kirya343.main.services.impl;

import org.kirya343.main.model.Listing;
import org.kirya343.main.model.User;
import org.kirya343.main.model.chat.Conversation;
import org.kirya343.main.repository.ConversationRepository;
import org.kirya343.main.repository.ListingRepository;
import org.kirya343.main.services.ListingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class ListingServiceImpl implements ListingService {

    private final ListingRepository listingRepository;

    private final ConversationRepository conversationRepository;

    @Autowired
    public ListingServiceImpl(ListingRepository listingRepository, ConversationRepository conversationRepository) {
        this.listingRepository = listingRepository;
        this.conversationRepository = conversationRepository;
    }

    @Override
    public Page<Listing> findByCategory(String category, Pageable pageable) {
        return listingRepository.findByCategory(category, pageable);
    }

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
    public Page<Listing> findActiveByCategory(String category, Pageable pageable) {
        return listingRepository.findActiveByCategory(category, pageable);
    }

    public List<Listing> findByUserEmail(String email) {
        return listingRepository.findByAuthorEmail(email);
    }
    public List<Listing> getAllActiveListings() {
        return listingRepository.findByActiveTrue(); // Предполагая, что у вас есть поле `active` в сущности
    }
    public List<Listing> getAllListings() {
        return listingRepository.findAll(); // Просто получаем все объявления
    }
    // Новый метод с JOIN FETCH (оптимизированный)
    public Listing getListingByIdWithAuthorAndReviews(Long id) {
        return listingRepository.findByIdWithAuthorAndReviews(id).orElse(null);
    }

    // Оставляем стандартный тоже, если вдруг понадобится
    public Listing getListingById(Long id) {
        return listingRepository.findById(id).orElse(null);
    }
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

}

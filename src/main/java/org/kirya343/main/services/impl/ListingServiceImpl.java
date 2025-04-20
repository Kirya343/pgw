package org.kirya343.main.services.impl;

import org.kirya343.main.model.Listing;
import org.kirya343.main.model.User;
import org.kirya343.main.repository.ListingRepository;
import org.kirya343.main.services.ListingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListingServiceImpl implements ListingService {

    private final ListingRepository listingRepository;

    @Autowired
    public ListingServiceImpl(ListingRepository listingRepository) {
        this.listingRepository = listingRepository;
    }

    @Override
    public Page<Listing> findByCategory(String category, Pageable pageable) {
        return listingRepository.findByCategory(category, pageable);
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
    public Listing getListingById(Long id) {
        return listingRepository.findById(id).orElse(null);
    }
    public List<Listing> findSimilarListings(String category, Long excludeId) {
        return listingRepository.findByCategoryAndIdNot(category, excludeId, PageRequest.of(0, 4));
    }
    public void deleteListing(Long id) {
        if (!listingRepository.existsById(id)) {
            throw new IllegalArgumentException("Объявление с ID " + id + " не найдено");
        }
        try {
            listingRepository.deleteById(id);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при удалении объявления: " + e.getMessage(), e);
        }
    }
}

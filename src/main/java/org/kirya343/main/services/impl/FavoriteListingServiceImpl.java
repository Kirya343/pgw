package org.kirya343.main.services.impl;

import org.kirya343.main.model.FavoriteListing;
import org.kirya343.main.model.Listing;
import org.kirya343.main.model.User;
import org.kirya343.main.repository.FavoriteListingRepository;
import org.kirya343.main.services.FavoriteListingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FavoriteListingServiceImpl implements FavoriteListingService {

    private final FavoriteListingRepository favoriteListingRepository;

    @Override
    @Transactional
    public void toggleFavorite(User user, Listing listing) {
        if (favoriteListingRepository.existsByUserAndListing(user, listing)) {
            favoriteListingRepository.deleteByUserAndListing(user, listing);
        } else {
            FavoriteListing favoritelisting = new FavoriteListing();
            favoritelisting.setUser(user);
            favoritelisting.setListing(listing);
            favoriteListingRepository.save(favoritelisting);
        }
    }

    @Override
    public List<Listing> getFavoritesForUser(User user) {
        return favoriteListingRepository.findByUser(user).stream()
                .map(FavoriteListing::getListing)
                .collect(Collectors.toList());
    }

    @Override
    public void removeFromFavorites(User user, Listing listing) {
        if (favoriteListingRepository.existsByUserAndListing(user, listing)) {
            favoriteListingRepository.deleteByUserAndListing(user, listing);
        }
    }

    @Override
    public List<FavoriteListing> findByUser(User user) {
        return favoriteListingRepository.findByUser(user);
    }

    @Override
    public boolean isFavorite(User user, Listing listing) {
        return favoriteListingRepository.existsByUserAndListing(user, listing);
    }
}


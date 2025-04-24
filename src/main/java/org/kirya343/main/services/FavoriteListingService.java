package org.kirya343.main.services;

import org.kirya343.main.model.FavoriteListing;
import org.kirya343.main.model.Listing;
import org.kirya343.main.model.User;
import org.kirya343.main.repository.FavoriteListingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FavoriteListingService {

    @Autowired
    private FavoriteListingRepository favoriteListingRepository;

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

    public List<Listing> getFavoritesForUser(User user) {
        return favoriteListingRepository.findByUser(user).stream()
                .map(FavoriteListing::getListing)
                .collect(Collectors.toList());
    }

    public void removeFromFavorites(User user, Listing listing) {
        if (favoriteListingRepository.existsByUserAndListing(user, listing)) {
            favoriteListingRepository.deleteByUserAndListing(user, listing);
        }
    }

    public List<FavoriteListing> findByUser(User user) {
        return favoriteListingRepository.findByUser(user);
    }

    public boolean isFavorite(User user, Listing listing) {
        return favoriteListingRepository.existsByUserAndListing(user, listing);
    }
}


package org.kirya343.main.services;

import org.kirya343.main.model.FavoriteListing;
import org.kirya343.main.model.Listing;
import org.kirya343.main.model.User;

import java.util.List;

public interface FavoriteListingService {

    void toggleFavorite(User user, Listing listing);

    List<Listing> getFavoritesForUser(User user);

    void removeFromFavorites(User user, Listing listing);

    List<FavoriteListing> findByUser(User user);

    boolean isFavorite(User user, Listing listing);
}

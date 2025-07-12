package org.workswap.main.services;

import java.util.List;

import org.workswap.datasource.main.model.FavoriteListing;
import org.workswap.datasource.main.model.Listing;
import org.workswap.datasource.main.model.User;

public interface FavoriteListingService {

    void toggleFavorite(User user, Listing listing);

    List<Listing> getFavoritesForUser(User user);

    void removeFromFavorites(User user, Listing listing);

    List<FavoriteListing> findByUser(User user);

    boolean isFavorite(User user, Listing listing);
}

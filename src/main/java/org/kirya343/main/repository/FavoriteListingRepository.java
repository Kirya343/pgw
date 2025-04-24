package org.kirya343.main.repository;

import org.kirya343.main.model.FavoriteListing;
import org.kirya343.main.model.Listing;
import org.kirya343.main.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FavoriteListingRepository extends JpaRepository<FavoriteListing, Long> {
    List<FavoriteListing> findByUser(User user);
    boolean existsByUserAndListing(User user, Listing listing);
    void deleteByUserAndListing(User user, Listing listing);
}

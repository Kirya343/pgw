package org.kirya343.main.repository;

import org.kirya343.main.model.Listing;
import org.kirya343.main.model.Review;
import org.kirya343.main.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    // Пример кастомного метода:
    List<Review> findByListingIdOrderByCreatedAtDesc(Long listingId);
    boolean existsByAuthorAndListing(User author, Listing listing);

    @Query("SELECT r FROM Review r JOIN FETCH r.author WHERE r.listing.id = :listingId")
    List<Review> findByListingIdWithAuthors(@Param("listingId") Long listingId);
}
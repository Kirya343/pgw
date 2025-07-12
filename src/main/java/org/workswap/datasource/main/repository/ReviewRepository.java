package org.workswap.datasource.main.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.workswap.datasource.main.model.Listing;
import org.workswap.datasource.main.model.Review;
import org.workswap.datasource.main.model.User;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    // Пример кастомного метода:
    List<Review> findByListingIdOrderByCreatedAtDesc(Long listingId);
    List<Review> findByProfileIdOrderByCreatedAtDesc(Long profileId);

    boolean existsByAuthorAndListing(User author, Listing listing);
    boolean existsByAuthorAndProfile(User author, User profile);

    @Query("SELECT r FROM Review r JOIN FETCH r.author WHERE r.listing.id = :listingId")
    List<Review> findByListingIdWithAuthors(@Param("listingId") Long listingId);

    @Query("SELECT r FROM Review r JOIN FETCH r.author WHERE r.profile.id = :profileId")
    List<Review> findByProfileIdWithAuthors(@Param("profileId") Long profileId);
}
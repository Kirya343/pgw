package org.kirya343.main.repository;

import org.kirya343.main.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    // Пример кастомного метода:
    List<Review> findByListingIdOrderByCreatedAtDesc(Long listingId);
}
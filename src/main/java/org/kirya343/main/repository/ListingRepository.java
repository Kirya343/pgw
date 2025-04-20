package org.kirya343.main.repository;

import org.kirya343.main.model.Listing;
import org.kirya343.main.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ListingRepository extends JpaRepository<Listing, Long> {
    Page<Listing> findByCategory(String category, Pageable pageable);
    List<Listing> findByAuthor(User author);
    @Query("SELECT l FROM Listing l WHERE l.author.email = :email")
    List<Listing> findByAuthorEmail(@Param("email") String email);
    List<Listing> findByActiveTrue();
    Page<Listing> findByCategoryAndActiveTrue(String category, Pageable pageable);
    @Query("SELECT l FROM Listing l WHERE l.active = true AND l.category = :category")
    Page<Listing> findActiveByCategory(@Param("category") String category, Pageable pageable);
    @Query("SELECT l FROM Listing l WHERE l.category = :category AND l.id != :excludeId AND l.active = true ORDER BY l.createdAt DESC")
    List<Listing> findByCategoryAndIdNot(
            @Param("category") String category,
            @Param("excludeId") Long excludeId,
            Pageable pageable);
}

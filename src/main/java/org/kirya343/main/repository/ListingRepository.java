package org.kirya343.main.repository;

import org.kirya343.main.model.Listing;
import org.kirya343.main.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

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

    List<Listing> findByCommunityRuTrueAndActiveTrue(); // Для русскоязычного комьюнити
    List<Listing> findByCommunityFiTrueAndActiveTrue(); // Для финскоязычного комьюнити
    List<Listing> findByCommunityEnTrueAndActiveTrue(); // Для англоязычного комьюнити

    Page<Listing> findByCategoryAndCommunityRuTrueAndActiveTrue(String category, Pageable pageable);
    Page<Listing> findByCategoryAndCommunityFiTrueAndActiveTrue(String category, Pageable pageable);
    Page<Listing> findByCategoryAndCommunityEnTrueAndActiveTrue(String category, Pageable pageable);

    // 🔥 Новый метод для оптимизированной загрузки
    @Query("SELECT DISTINCT l FROM Listing l " +
            "LEFT JOIN FETCH l.author " +
            "LEFT JOIN FETCH l.reviews " +
            "WHERE l.id = :id")
    Optional<Listing> findByIdWithAuthorAndReviews(@Param("id") Long id);

    @Query("SELECT SUM(l.views) FROM Listing l")
    Integer sumViews();

    @Query("SELECT COUNT(l) FROM Listing l WHERE l.active = true")
    long countActiveListings();

    Page<Listing> findAllByOrderByCreatedAtDesc(Pageable pageable);
}

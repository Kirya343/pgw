package org.kirya343.main.repository;

import java.util.List;
import java.util.Optional;

import org.kirya343.main.model.Listing;
import org.kirya343.main.model.User;
import org.kirya343.main.model.listingModels.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;


@Repository
public interface ListingRepository extends JpaRepository<Listing, Long> {
    Page<Listing> findByCategory(String category, Pageable pageable);
    List<Listing> findByAuthor(User author);

    List<Listing> findByAuthorAndActiveTrue(User author);

    @Query("SELECT l FROM Listing l WHERE l.author.email = :email")
    List<Listing> findByAuthorEmail(@Param("email") String email);

    // Для Page
    @Query("SELECT l FROM Listing l WHERE l.active = true")
    Page<Listing> findPageByActiveTrue(Pageable pageable);
    // Для List
    @Query("SELECT l FROM Listing l WHERE l.active = true")
    List<Listing> findListByActiveTrue();

    List<Listing> findListByActiveTrueAndTestModeFalse();

    Page<Listing> findByCategoryAndActiveTrue(String category, Pageable pageable);
    @Query("SELECT l FROM Listing l WHERE l.active = true AND l.category = :category")
    Page<Listing> findActiveByCategory(@Param("category") String category, Pageable pageable);
    @Query("SELECT l FROM Listing l WHERE l.category = :category AND l.id != :excludeId AND l.active = true ORDER BY l.createdAt DESC")
    List<Listing> findByCategoryAndIdNot(@Param("category") Category category, @Param("excludeId") Long excludeId, Pageable pageable);

    @Query("SELECT l FROM Listing l WHERE l.category IN :categories")
    List<Listing> findByCategory(@Param("categories") List<Category> categories);

    @Query("SELECT l FROM Listing l JOIN l.communities c WHERE c = :language AND l.active = true ")
    List<Listing> findByCommunityAndActiveTrue(@Param("language") String language);

    // Новый метод для оптимизированной загрузки
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

    @Query("""
        SELECT DISTINCT l FROM Listing l
        JOIN l.translations t
        WHERE (
            LOWER(t.title) LIKE LOWER(CONCAT('%', :query, '%')) OR
            LOWER(t.description) LIKE LOWER(CONCAT('%', :query, '%'))
        )
        AND l.active = true
    """)
    List<Listing> searchAllFields(@Param("query") String query);

    @Query("""
        SELECT l FROM Listing l
        JOIN l.communities c
        WHERE l.category = :category
        AND l.id <> :excludeId
        AND c = :language 
        """)
    List<Listing> findTop4ByCategoryAndIdNotAndCommunity(
        @Param("category") Category category,
        @Param("excludeId") Long excludeId,
        @Param("language") String language,
        Pageable pageable);

        
    @EntityGraph(attributePaths = "translations")
    Optional<Listing> findWithTranslationsById(@NonNull Long id);
}

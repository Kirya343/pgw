package org.workswap.datasource.main.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.workswap.datasource.main.model.News;

@Repository
public interface NewsRepository extends JpaRepository<News, Long> {
    // Для списка новостей
    Page<News> findByPublishedTrueOrderByPublishDateDesc(Pageable pageable);

    List<News> findByPublishedTrue();
    
    // Для похожих новостей (используем JPQL для надежности)
    @Query("SELECT n FROM News n WHERE n.published = true AND n.id <> :excludeId ORDER BY n.publishDate DESC")
    Page<News> findSimilarNews(@Param("excludeId") Long excludeId, Pageable pageable);
    
    // Для последних новостей
    List<News> findTop3ByPublishedTrueOrderByPublishDateDesc();

    // Для админ-панели
    Page<News> findAllByOrderByPublishDateDesc(Pageable pageable);
    
    //Page<News> findByTitleRuContainingIgnoreCase(String titleRu, Pageable pageable);
    
    /* @Query("SELECT n FROM News n WHERE LOWER(n.title) LIKE LOWER(concat('%', :query, '%')) OR " +
           "LOWER(n.content) LIKE LOWER(concat('%', :query, '%')) ORDER BY n.publishDate DESC")
    Page<News> searchNews(@Param("query") String query, Pageable pageable); */

    // Статистика
    long countByPublishedTrue();
    long countByPublishedFalse();
    
    @Query("SELECT COUNT(n) FROM News n WHERE n.publishDate BETWEEN :startDate AND :endDate")
    long countByPublishDateBetween(@Param("startDate") LocalDateTime startDate, 
                                 @Param("endDate") LocalDateTime endDate);
}
package org.kirya343.main.repository;

import org.kirya343.main.model.News;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NewsRepository extends JpaRepository<News, Long> {
    Page<News> findByPublishedTrueOrderByPublishDateDesc(Pageable pageable);
    List<News> findTop3ByPublishedTrueOrderByPublishDateDesc();
}

package org.kirya343.main.repository;

import org.kirya343.main.model.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {
    // Дополнительные методы, если нужны:
    List<Image> findByListingId(Long listingId);
}
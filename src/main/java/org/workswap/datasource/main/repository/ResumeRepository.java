package org.workswap.datasource.main.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.workswap.datasource.main.model.Resume;
import org.workswap.datasource.main.model.User;

import java.util.Optional;

public interface ResumeRepository extends JpaRepository<Resume, Long> {
    Optional<Resume> findByUser(User user);
    Page<Resume> findByPublishedTrue(Pageable pageable);
    void deleteByUser(User user);

    @Query("SELECT r FROM Resume r LEFT JOIN FETCH r.user WHERE r.id = :id")
    Optional<Resume> findByIdWithUser(@Param("id") Long id);
    long countByPublishedTrue();
}
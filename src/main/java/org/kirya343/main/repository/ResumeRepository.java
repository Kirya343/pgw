package org.kirya343.main.repository;

import org.kirya343.main.model.Resume;
import org.kirya343.main.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ResumeRepository extends JpaRepository<Resume, Long> {
    Optional<Resume> findByUser(User user);
    void deleteByUser(User user);
}

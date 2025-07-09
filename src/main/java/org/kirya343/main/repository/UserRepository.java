package org.kirya343.main.repository;

import org.kirya343.main.model.User;
import org.kirya343.main.model.User.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByName(String name);

    // Для OAuth2 пользователей
    Optional<User> findBySub(String sub);

    boolean existsByEmail(String email);

    @Query("SELECT COUNT(u) FROM User u WHERE u.enabled = true")
    long countActiveUsers();

    Page<User> findAllByOrderByCreatedAtDesc(Pageable pageable);

    List<User> findByRole(Role role);
}

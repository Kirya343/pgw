package org.workswap.datasource.main.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.workswap.datasource.main.model.User;
import org.workswap.datasource.main.model.User.Role;

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

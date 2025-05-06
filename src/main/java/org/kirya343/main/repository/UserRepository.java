package org.kirya343.main.repository;

import org.kirya343.main.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    // Используйте email вместо username, если нужно
    Optional<User> findByEmail(String email);

    // Или добавьте обратно поиск по username
    Optional<User> findByUsername(String username);

    // Для OAuth2 пользователей
    Optional<User> findBySub(String sub);

    Optional<User> findById(Long id);

    boolean existsByEmail(String email);

    @Query("SELECT COUNT(u) FROM User u WHERE u.enabled = true")
    long countActiveUsers();
}

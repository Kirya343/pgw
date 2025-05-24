package org.kirya343.main.services;

import org.kirya343.main.model.User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.List;

public interface UserService {
    // Удаляем дублирующийся метод
    void setAvatarUrl(Long userId, String avatarUrl);

    User findByEmail(String email);
    User findByUsername(String username);
    User findBySub(String sub);
    User findUserFromOAuth2(OAuth2User oauth2User);
    void registerUserFromOAuth2(OAuth2User oauth2User);

    // Добавляем новые методы
    User save(User user);
    void setRole(Long userId, String role);
    User findById(Long id);

    List<User> getRecentUsers(int count);
}
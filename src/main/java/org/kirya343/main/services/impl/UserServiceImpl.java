package org.kirya343.main.services.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.kirya343.main.model.User;
import org.kirya343.main.repository.UserRepository;
import org.kirya343.main.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    @Override
    public User findBySub(String sub) {
        return userRepository.findBySub(sub).orElse(null);
    }

    @Override
    @Transactional
    public User findOrCreateUserFromOAuth2(OAuth2User oauth2User) {
        String email = oauth2User.getAttribute("email");
        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            user = User.fromOAuth2(oauth2User);
        } else {
            // Обновляем только те поля, которые не были изменены пользователем
            if (user.getName() == null || user.getName().equals(oauth2User.getAttribute("name"))) {
                user.setName(oauth2User.getAttribute("name"));
            }
            if (user.getPicture() == null) {
                user.setPicture(oauth2User.getAttribute("picture"));
            }
            // Другие поля из Google, которые должны обновляться
        }

        return userRepository.save(user);
    }
    @Override
    @Transactional
    public void setAvatarUrl(Long userId, String avatarUrl) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setAvatarUrl(avatarUrl);
    }

    @Override
    @Transactional
    public User save(User user) {
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public void setRole(Long userId, String role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setRole(role);
        // userRepository.save(user); // Не нужно - изменения сохранятся благодаря @Transactional
    }


    @Override
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }

        if (authentication.getPrincipal() instanceof OAuth2User oauth2User) {
            return findOrCreateUserFromOAuth2(oauth2User);
        }

        String email = authentication.getName();
        return findByEmail(email);
    }
}


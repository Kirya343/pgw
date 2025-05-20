package org.kirya343.main.services.impl;

import jakarta.transaction.Transactional;
import org.kirya343.main.model.User;
import org.kirya343.main.repository.UserRepository;
import org.kirya343.main.services.UserService;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

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
    public void registerUserFromOAuth2(OAuth2User oauth2User) {
        String email = oauth2User.getAttribute("email");
        String sub = oauth2User.getAttribute("sub");

        // Проверка наличия email
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email пользователя не найден.");
        }

        // Проверяем, существует ли пользователь с таким email
        User existingUser = userRepository.findByEmail(email).orElse(null);

        if (existingUser != null) {
            throw new RuntimeException("Пользователь с таким email уже зарегистрирован.");
        }

        // Создаем нового пользователя
        User newUser = new User();
        newUser.setEmail(email);
        newUser.setSub(sub);
        newUser.setEnabled(true);
        newUser.setRole("USER");
        newUser.setTermsAccepted(true); // Устанавливаем значение по умолчанию
        newUser.setTermsAcceptanceDate(LocalDateTime.now()); // Устанавливаем значение по умолчанию

        String oauthName = oauth2User.getAttribute("name");
        if (oauthName != null && !oauthName.isBlank()) {
            newUser.setName(oauthName);
        }

        String oauthPicture = oauth2User.getAttribute("picture");
        if (oauthPicture != null && !oauthPicture.isBlank()) {
            newUser.setPicture(oauthPicture);
        }

        // Дополнительно можно проверить наличие других обязательных атрибутов

        // Сохраняем нового пользователя
        newUser = userRepository.save(newUser);

    }


    @Override
    @Transactional
    public User findUserFromOAuth2(OAuth2User oauth2User) {
        String email = oauth2User.getAttribute("email");
        User user = userRepository.findByEmail(email).orElse(null);

        // Возвращаем null вместо исключения, если пользователь не найден
        if (user == null) {
            System.out.println("Пытались найти пользователя, не нашли");
            return null;
        }

        // Обновляем информацию, если пользователь найден
        String oauthName = oauth2User.getAttribute("name");
        if ((user.getName() == null || user.getName().isBlank())
                && oauthName != null && !oauthName.isBlank()) {
            user.setName(oauthName);
        }

        String oauthPicture = oauth2User.getAttribute("picture");
        if (user.getPicture() == null && oauthPicture != null && !oauthPicture.isBlank()) {
            user.setPicture(oauthPicture);
        }

        return user;
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
            String email = oauth2User.getAttribute("email");
            User user = findByEmail(email);
            if (user == null) {
                // Возвращаем null или пробрасываем флаг регистрации
                throw new RuntimeException("Пользователь не найден. Пожалуйста, зарегистрируйтесь.");
            }
            return user;
        }

        String email = authentication.getName();
        return findByEmail(email);
    }
    @Override
    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }
    @Override
    public List<User> getRecentUsers(int count) {
        return userRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, count)).getContent();
    }
}


package org.kirya343.main.services.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.kirya343.main.model.User;
import org.kirya343.main.model.User.Role;
import org.kirya343.main.repository.UserRepository;
import org.kirya343.main.services.UserService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public List<User> findAll() {
        return userRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
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
        newUser.setEmail(email); // Устанавливаем email пользователя
        newUser.setSub(sub); // Устанавливаем sub для идентификации пользователя в OAuth2
        newUser.setEnabled(true); // Устанавливаем пользователя как активного
        newUser.setRole(Role.USER); // Устанавливаем роль по умолчанию
        newUser.setTermsAccepted(true); // Устанавливаем значение по умолчанию
        newUser.setTermsAcceptanceDate(LocalDateTime.now()); // Устанавливаем значение по умолчанию
        newUser.setName(oauth2User.getAttribute("name")); // Устанавливаем имя пользователя
        newUser.setAvatarUrl(oauth2User.getAttribute("picture")); // Устанавливаем URL аватара
        newUser.setAvatarType("google"); // Устанавливаем тип аватара

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
        user.setRole(Role.USER);
    }

    @Override
    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }
    @Override
    public List<User> getRecentUsers(int count) {
        return userRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, count)).getContent();
    }

    @Override
    public void deleteById(Long id) {
        User user = findById(id);
        userRepository.delete(user);
    }
}


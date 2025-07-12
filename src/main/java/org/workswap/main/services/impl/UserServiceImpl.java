package org.workswap.main.services.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.workswap.datasource.main.model.User;
import org.workswap.datasource.main.model.ModelsSettings.SearchParamType;
import org.workswap.datasource.main.model.User.Role;
import org.workswap.datasource.main.repository.UserRepository;
import org.workswap.main.services.UserService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override 
    public User findUser(String param, SearchParamType paramType) {
        switch (paramType) {
            case ID:
                return userRepository.findById(Long.parseLong(param)).orElse(null);
            case EMAIL:
                return userRepository.findByEmail(param).orElse(null);
            case NAME:
                return userRepository.findByName(param).orElse(null); // если есть такой метод
            case SUB:
                return userRepository.findBySub(param).orElse(null);
            default:
                throw new IllegalArgumentException("Unknown param type: " + paramType);
        }
    }

    @Override
    public User findUserFromOAuth2(OAuth2User oauth2User) {
        User user = findUser(oauth2User.getAttribute("email"), SearchParamType.EMAIL);
        return user;
    }

    @Override
    @Transactional
    public void registerUserFromOAuth2(OAuth2User oauth2User) {

        // Проверяем, существует ли пользователь с таким email
        User existingUser = findUser(oauth2User.getAttribute("email"), SearchParamType.EMAIL);

        if (existingUser != null) {
            throw new RuntimeException("Пользователь с таким email уже зарегистрирован.");
        }

        // Создаем нового пользователя
        User newUser = new User(); 
        newUser.setEmail(oauth2User.getAttribute("email")); // Устанавливаем email пользователя
        newUser.setSub(oauth2User.getAttribute("sub")); // Устанавливаем sub для идентификации пользователя в OAuth2
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
    public List<User> findAll() {
        return userRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    @Override
    @Transactional
    public User save(User user) {
        return userRepository.save(user);
    }

    @Override
    public List<User> getRecentUsers(int count) {
        return userRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, count)).getContent();
    }

    @Override
    public void deleteById(Long id) {
        User user = findUser(id.toString(), SearchParamType.ID);
        userRepository.delete(user);
    }
}


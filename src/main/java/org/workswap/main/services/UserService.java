package org.workswap.main.services;

import org.springframework.security.oauth2.core.user.OAuth2User;
import org.workswap.datasource.main.model.User;
import org.workswap.datasource.main.model.ModelsSettings.SearchParamType;

import java.util.List;

public interface UserService {

    // Получение списка пользователей по пораметрам
    List<User> findAll();
    List<User> getRecentUsers(int count);

    // Поиск пользователя по пораметрам
    /* User findById(Long id);
    User findByEmail(String email);
    User findBySub(String sub); */
    User findUserFromOAuth2(OAuth2User oauth2User);

    User findUser(String param, SearchParamType paramType);

    // Регистрирация пользователя из OAuth2
    void registerUserFromOAuth2(OAuth2User oauth2User);

    // Управление пользователями
    User save(User user);
    void deleteById(Long id);
}
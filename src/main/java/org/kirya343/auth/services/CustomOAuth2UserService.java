package org.kirya343.auth.services;

import org.kirya343.main.model.User;
import org.kirya343.main.repository.UserRepository;
import org.kirya343.main.services.UserService;
import org.kirya343.auth.services.impl.CustomOAuth2UserImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private UserService userService;
    // Карта для хранения соответствия email -> роли
    private static final Map<String, String> EMAIL_TO_ROLE = new HashMap<>();
    private static final Logger log = LoggerFactory.getLogger(CustomOAuth2UserService.class);

    @Autowired
    private UserRepository userRepository;

    static {
        EMAIL_TO_ROLE.put("another.user@example.com", "USER");
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String email = oAuth2User.getAttribute("email");

        // Получаем текущий путь (например: "/login" или "/register")
        String currentUri = userRequest.getAdditionalParameters().get("redirect_uri") != null
                ? userRequest.getAdditionalParameters().get("redirect_uri").toString()
                : "";

        log.info("OAuth2 login attempt for email: {} from URI: {}", email, currentUri);

        User user = userService.findByEmail(email);
        if (user == null) {
            if (currentUri.contains("/register")) {
                // Регистрируем нового пользователя
                User newUser = new User();
                newUser.setEmail(email);
                newUser.setSub(oAuth2User.getAttribute("sub"));
                newUser.setUsername(oAuth2User.getAttribute("name")); // Или другое поле
                newUser.setRole("ROLE_USER"); // Или другая логика
                userRepository.save(newUser);
                log.info("New user registered: {}", email);
            } else {
                throw new OAuth2AuthenticationException(
                        new OAuth2Error("user_not_registered", "User not registered", null)
                );
            }
        }

        String role = determineUserRole(oAuth2User);
        return new CustomOAuth2UserImpl(oAuth2User, role);
    }


    private String determineUserRole(OAuth2User user) {
        // Пример: выдаем роль ADMIN для конкретного email
        if ("admin@example.com".equals(user.getAttribute("email"))) {
            return "ADMIN";
        }
        return "USER";
    }
}
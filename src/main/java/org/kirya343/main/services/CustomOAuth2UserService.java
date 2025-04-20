package org.kirya343.main.services;

import org.kirya343.main.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    // Карта для хранения соответствия email -> роли
    private static final Map<String, String> EMAIL_TO_ROLE = new HashMap<>();
    private static final Logger log = LoggerFactory.getLogger(CustomOAuth2UserService.class);
    private User email;

    static {
        EMAIL_TO_ROLE.put("another.user@example.com", "USER");
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);

        Set<GrantedAuthority> authorities = new HashSet<>();

        log.info("Processed OAuth2 user with email: {}", (Object) oauth2User.getAttribute("email"));
        log.info("User authorities: {}", authorities);

        // Добавляем стандартные authorities из OAuth2
        authorities.addAll(oauth2User.getAuthorities());

        // Добавляем обязательную роль USER
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));

        // Для админа добавляем роль ADMIN
        if ("kkodolov40@gmail.com".equals(oauth2User.getAttribute("email"))) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        } else {
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        }

        System.out.println("OAuth2 email: " + email);
        authorities.forEach(auth -> System.out.println("Granted authority: " + auth.getAuthority()));


        return new DefaultOAuth2User(
                authorities,
                oauth2User.getAttributes(),
                "email" // name attribute key
        );
    }

    private String determineUserRole(OAuth2User user) {
        // Пример: выдаем роль ADMIN для конкретного email
        if ("admin@example.com".equals(user.getAttribute("email"))) {
            return "ADMIN";
        }
        return "USER";
    }
}
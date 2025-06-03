package org.kirya343.auth.services;

import org.kirya343.main.model.User;
import org.kirya343.main.repository.UserRepository;
import org.kirya343.main.services.UserService;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
@Primary
public class ForcedOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserService userService;
    private final UserRepository userRepository;
    private final DefaultOAuth2UserService defaultService = new DefaultOAuth2UserService();

    public ForcedOAuth2UserService(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        System.out.println("=== FORCED OAUTH2 SERVICE LOADUSER CALLED ===");

        // 1. Загружаем стандартного пользователя
        OAuth2User oAuth2User = defaultService.loadUser(userRequest);
        System.out.println("Original user attributes: " + oAuth2User.getAttributes());

        // 2. Получаем email (обязательно)
        String email = oAuth2User.getAttribute("email");
        if (email == null) {
            throw new OAuth2AuthenticationException("Email not found in OAuth2 response");
        }

        // 3. Находим или создаем пользователя
        User user = userService.findByEmail(email);
        if (user == null) {
            System.out.println("Registering new user for email: " + email);
            user = registerNewUser(oAuth2User, email);
        }

        // 4. Создаем authorities
        Set<GrantedAuthority> authorities = new HashSet<>(oAuth2User.getAuthorities());
        authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole()));
        System.out.println("Final authorities: " + authorities);

        // 5. Возвращаем кастомного пользователя
        return new DefaultOAuth2User(
                authorities,
                oAuth2User.getAttributes(),
                "email" // name attribute key
        );
    }

    private User registerNewUser(OAuth2User oAuth2User, String email) {
        User newUser = new User();
        newUser.setEmail(email);
        newUser.setSub(oAuth2User.getAttribute("sub"));
        newUser.setRole("USER"); // Default role
        return userRepository.save(newUser);
    }
}
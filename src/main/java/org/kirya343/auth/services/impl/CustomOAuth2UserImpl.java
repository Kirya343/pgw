package org.kirya343.auth.services.impl;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.*;

public class CustomOAuth2UserImpl implements OAuth2User {

    private final OAuth2User oauth2User;
    private final String role;
    private final Set<GrantedAuthority> authorities;

    public CustomOAuth2UserImpl(OAuth2User oauth2User, String role) {
        this.oauth2User = oauth2User;
        this.role = role;
        this.authorities = new HashSet<>();

        System.out.println("Роль: " + role);

        // Добавляем роль (если она есть)
        if (role != null && !role.isEmpty()) {
            String roleWithPrefix = "ROLE_" + role.toUpperCase();
            this.authorities.add(new SimpleGrantedAuthority(roleWithPrefix));
            System.out.println("Added authority: " + roleWithPrefix); // Логируем
        }

        // Добавляем OIDC authorities (если нужны)
        this.authorities.addAll(oauth2User.getAuthorities());
        System.out.println("Final authorities: " + authorities); // Логируем
    }

    @Override
    public Map<String, Object> getAttributes() {
        return oauth2User.getAttributes();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.unmodifiableSet(authorities);
    }

    @Override
    public String getName() {
        return oauth2User.getAttribute("name");
    }

    public String getEmail() {
        return oauth2User.getAttribute("email");
    }
}
package org.kirya343.auth.services.impl;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class CustomOAuth2UserImpl implements OAuth2User {

    private final OAuth2User oauth2User;
    private final String role;

    // Исправлен конструктор (было CustomOAuth2User, должно быть CustomOAuth2UserImpl)
    public CustomOAuth2UserImpl(OAuth2User oauth2User, String role) {
        this.oauth2User = oauth2User;
        this.role = role;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return oauth2User.getAttributes();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + role) // Префикс ROLE_ обязателен
        );
    }

    @Override
    public String getName() {
        return oauth2User.getAttribute("name");
    }

    public String getEmail() {
        return oauth2User.getAttribute("email");
    }
}
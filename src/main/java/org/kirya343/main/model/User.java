package org.kirya343.main.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Data
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String sub; // Уникальный идентификатор от Google

    @Column(unique = true)
    private String username;

    private String bio;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    private String givenName;
    private String familyName;
    private String picture;
    private String avatarUrl;
    private boolean emailVerified;
    private String locale;

    @Enumerated(EnumType.STRING)
    private AuthProvider provider; // GOOGLE, LOCAL и т.д.

    // Для локальных пользователей (если нужно)
    private String password;

    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Listing> listings = new ArrayList<>();

    private String role;

    private boolean locked;

    private boolean accountNonLocked;
    private boolean accountNonExpired;

    private boolean credentialsNonExpired;
    private boolean accountNonLockingProtected;

    private boolean enabled;
    private boolean disabled;

    private boolean accountNonExpiredOrCredentialsNonExpired;

    // Геттер и сеттер
    @Setter
    @Getter
    @Column(name = "avatar_type")
    private String avatarType; // "uploaded", "google", "default"

    public enum AuthProvider {
        LOCAL,
        GOOGLE
    }

    private Double rating;
    private String phone;
    private Integer completedJobs;

    // Метод для создания пользователя из OAuth2 данных
    public static User fromOAuth2(OAuth2User oauth2User) {
        User user = new User();
        user.setSub(oauth2User.getAttribute("sub"));
        // Не устанавливаем имя, если оно уже есть
        user.setEmail(oauth2User.getAttribute("email"));
        user.setPicture(oauth2User.getAttribute("picture"));
        user.setProvider(AuthProvider.GOOGLE);
        user.setEnabled(true);
        user.setRole("USER");
        return user;
    }
}
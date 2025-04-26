package org.kirya343.main.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.kirya343.main.model.chat.Conversation;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@JsonIgnoreProperties({"listings"})
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
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

    @Column(name = "phone_visible")
    private boolean phoneVisible = true;  // Скрывать или отображать телефон

    @Column(name = "email_visible")
    private boolean emailVisible = true;  // Скрывать или отображать email

    @Enumerated(EnumType.STRING)
    private AuthProvider provider; // GOOGLE, LOCAL и т.д.

    // Для локальных пользователей (если нужно)
    private String password;

    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Listing> listings = new ArrayList<>();

    @OneToMany(mappedBy = "user1", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Conversation> conversationsStarted = new ArrayList<>();

    @OneToMany(mappedBy = "user2", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Conversation> conversationsReceived = new ArrayList<>();

    private String role;

    private boolean locked;
    private boolean accountNonLocked;
    private boolean accountNonExpired;
    private boolean credentialsNonExpired;
    private boolean accountNonLockingProtected;
    private boolean enabled;
    private boolean disabled;
    private boolean accountNonExpiredOrCredentialsNonExpired;

    @Column(name = "avatar_type")
    private String avatarType; // "uploaded", "google", "default"

    private Double averageRating;
    private String phone;
    private Integer completedJobs;

    public enum AuthProvider {
        LOCAL,
        GOOGLE
    }

    // Метод для создания пользователя из OAuth2 данных
    public static User fromOAuth2(OAuth2User oauth2User) {
        User user = new User();
        user.setSub(oauth2User.getAttribute("sub"));
        user.setEmail(oauth2User.getAttribute("email"));

        String name = oauth2User.getAttribute("name");
        if (name == null || name.isBlank()) {
            name = oauth2User.getAttribute("email"); // запасной вариант
        }
        user.setName(name);

        user.setPicture(oauth2User.getAttribute("picture"));
        user.setProvider(AuthProvider.GOOGLE);
        user.setEnabled(true);
        user.setRole("USER");
        return user;
    }
}

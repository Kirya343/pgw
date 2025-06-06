package org.kirya343.main.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.kirya343.main.model.chat.Conversation;

import java.time.LocalDateTime;
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


    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    private String bio;

    private String picture;
    private String avatarUrl;

    private boolean phoneVisible = true;  // Скрывать или отображать телефон

    private boolean emailVisible = true;  // Скрывать или отображать email

    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Listing> listings = new ArrayList<>();

    @OneToMany(mappedBy = "user1", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Conversation> conversationsStarted = new ArrayList<>();

    @OneToMany(mappedBy = "user2", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Conversation> conversationsReceived = new ArrayList<>();

    private String role;

    private boolean locked;
    private boolean enabled;

    private String avatarType; // "uploaded", "google", "default"

    private Double averageRating = 0.0; // Средний рейтинг пользователя
    private String phone;

    private Integer completedJobs;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private boolean termsAccepted = false; // Приняты ли условия использования

    @Column(nullable = false)
    private LocalDateTime termsAcceptanceDate;

    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL)
    private List<Review> reviews;

    // Метод для создания пользователя из OAuth2 данных
    /* public static User fromOAuth2(OAuth2User oauth2User) {
        User user = new User();
        user.setSub(oauth2User.getAttribute("sub"));
        user.setEmail(oauth2User.getAttribute("email"));

        String name = oauth2User.getAttribute("name");
        if (name == null || name.isBlank()) {
            name = oauth2User.getAttribute("email"); // запасной вариант
        }
        user.setName(name);

        user.setAvatarUrl(oauth2User.getAttribute("picture"));
        user.setEnabled(true);
        user.setRole("USER");
        return user;
    } */
}

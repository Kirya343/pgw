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

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_languages", joinColumns = @JoinColumn(name = "user_id"))
    private List<String> languages = new ArrayList<>();

    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Listing> listings = new ArrayList<>();

    @OneToMany(mappedBy = "executor")
    private List<Task> tasks = new ArrayList<>();

    @OneToMany(mappedBy = "user1", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Conversation> conversationsStarted = new ArrayList<>();

    @OneToMany(mappedBy = "user2", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Conversation> conversationsReceived = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private Role role;

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

    // соц сети

    private boolean telegramConnected = false; // Подключен ли Telegram

    // Енумы 
    
    public enum Role {
        USER(1),
        PREMIUM(2),
        BUSINESS(3),
        ADMIN(4);

        private final int level;

        Role(int level) {
            this.level = level;
        }

        public int getLevel() {
            return level;
        }

        public boolean isAtLeast(Role other) {
            return this.level >= other.level;
        }
    }
}

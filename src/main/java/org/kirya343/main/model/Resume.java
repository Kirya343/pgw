package org.kirya343.main.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Setter
@Getter
@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id"})
})
public class Resume {
    // Геттеры и сеттеры
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    private String profession;
    private Double experience;
    private String education;
    private String skills;
    private String about;
    private String contacts;
    private String filePath;
    private boolean published; // Новое поле для статуса публикации

    @ElementCollection
    @CollectionTable(name = "resume_languages", joinColumns = @JoinColumn(name = "resume_id"))
    @MapKeyColumn(name = "language")
    @Column(name = "level")
    private Map<String, String> languages = new HashMap<>();

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Transient
    private List<String> languagesForm;

    @Transient
    private List<String> languageLevelsForm;

    @Transient
    private Map<String, String> languagesFormMap = new HashMap<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
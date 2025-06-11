package org.kirya343.main.model;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Entity
public class Listing {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String community;

    // Заголовки на разных языках
    private String titleRu;
    private String titleFi;
    private String titleEn;

    // Описания на разных языках
    @Column(length = 2000)
    private String descriptionRu;

    @Column(length = 2000)
    private String descriptionFi;

    @Column(length = 2000)
    private String descriptionEn;

    @PositiveOrZero
    private double price;
    private String priceType; // "час", "фиксированная" и т.д.
    private String category;
    private String location;
    private double rating;
    private int views;
    
    @PastOrPresent
    private LocalDateTime createdAt;

    @Setter
    @Getter
    private boolean active = true;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User author;

    @ElementCollection
    private List<String> features;

    @OneToMany(mappedBy = "listing", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Image> images;

    @OneToMany(mappedBy = "listing", cascade = CascadeType.ALL)
    private List<Review> reviews;

    private double averageRating;

    private boolean availableNow;

    @Setter
    @Getter
    private String imagePath;

    // Новые флаги для целевой аудитории
    private boolean communityRu;
    private boolean communityFi;
    private boolean communityEn;

    public boolean getCommunityFi() {
        return communityFi;
    }
    public boolean getCommunityRu() {
        return communityRu;
    }
    public boolean getCommunityEn() {
        return communityEn;
    }

    @Transient
    private String localizedTitle;

    @Transient
    private String localizedDescription;
}
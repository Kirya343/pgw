package org.kirya343.main.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kirya343.main.model.listingModels.Category;
import org.kirya343.main.model.listingModels.Image;
import org.kirya343.main.model.listingModels.ListingTranslation;
import org.kirya343.main.model.listingModels.Location;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapKey;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
@Data
@Entity
public class Listing {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "listing", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @MapKey(name = "language") // ключ — это язык (например, "ru")
    private Map<String, ListingTranslation> translations = new HashMap<>();

    @PositiveOrZero
    private double price;
    private String priceType; // "час", "фиксированная" и т.д.
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    private double rating;
    private int views;
    
    @PastOrPresent
    private LocalDateTime createdAt;

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

    private String imagePath;

    // Новые флаги для целевой аудитории
    @ElementCollection
    @CollectionTable(name = "listing_communities", joinColumns = @JoinColumn(name = "listing_id"))
    @Column(name = "language")
    private List<String> communities = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "location")
    private Location location;

    private boolean testMode;

    @Transient
    private String localizedTitle;

    @Transient
    private String localizedDescription;
}
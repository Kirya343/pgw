package org.workswap.datasource.main.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.workswap.datasource.main.model.listingModels.Category;
import org.workswap.datasource.main.model.listingModels.Image;
import org.workswap.datasource.main.model.listingModels.ListingTranslation;
import org.workswap.datasource.main.model.listingModels.Location;

import jakarta.persistence.*;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
@Data
@Entity
public class Listing {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "listing", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @MapKey(name = "language") 
    private Map<String, ListingTranslation> translations = new HashMap<>();

    @PositiveOrZero
    private double price;
    private String priceType;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id")
    private Category category;

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

    @OneToMany(mappedBy = "listing", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<FavoriteListing> favorites;

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
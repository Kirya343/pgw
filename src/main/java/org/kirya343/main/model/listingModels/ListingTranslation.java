package org.kirya343.main.model.listingModels;

import org.kirya343.main.model.Listing;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class ListingTranslation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String language;

    private String title;
    @Column(length = 2000)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "listing_id")
    private Listing listing;
}